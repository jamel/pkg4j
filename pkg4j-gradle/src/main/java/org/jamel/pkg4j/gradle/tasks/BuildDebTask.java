package org.jamel.pkg4j.gradle.tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import groovy.text.GStringTemplateEngine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.taskdefs.Tar;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.jamel.pkg4j.gradle.PackageInfo;
import org.jamel.pkg4j.gradle.convention.CommonPkgConvention;
import org.jamel.pkg4j.gradle.convention.PkgConvention;
import org.jamel.pkg4j.gradle.utils.PassphraseProvider;
import org.jamel.pkg4j.SigningUtils;
import org.vafer.jdeb.Compression;
import org.vafer.jdeb.Console;
import org.vafer.jdeb.DataProducer;
import org.vafer.jdeb.Processor;
import org.vafer.jdeb.changes.TextfileChangesProvider;
import org.vafer.jdeb.descriptors.PackageDescriptor;
import org.vafer.jdeb.producers.DataProducerFileSet;

/**
 * @author Sergey Polovko
 */
public class BuildDebTask extends DefaultTask {

    private final GStringTemplateEngine engine = new GStringTemplateEngine();


    @TaskAction
    public void run() throws Exception {
        PkgConvention pkg = (PkgConvention) getProject().getConvention().getPlugins().get("pkg");
        CommonPkgConvention commonPkg = (CommonPkgConvention) getProject().getRootProject().getConvention()
                .getPlugins().get("commonPkg");

        for (PackageInfo packageInfo : pkg.getPackages()) {
            packageInfo.mergeWithCommon(commonPkg.getPackage());
            buildPackage(packageInfo);
        }
    }

    private void buildPackage(PackageInfo pkg) throws IOException, ClassNotFoundException {
        if (!pkg.isNeedBuild()) return;

        getLogger().info("Build package {} with such configuration: ", pkg.getName());
        getLogger().info(pkg.toString());

        File debianDir = new File(getProject().getBuildDir(), "debian");
        FileUtils.deleteDirectory(debianDir);
        debianDir.mkdirs();

        Map<String,Object> context = pkg.toContext();
        generateFile("control", context);
        generateFile("postinst", context);
        generateFile("prerm", context);

        List<DataProducer> dataProducers = new ArrayList<DataProducer>();
        for (Map<String, String> dir : pkg.getDirsToPack()) {
            Tar.TarFileSet fileSet = new Tar.TarFileSet();
            fileSet.setDir(getProject().file(dir.get("dir")));
            fileSet.setPrefix(dir.get("prefix"));
            String mode = dir.get("mode");
            if (StringUtils.isNotEmpty(mode)) {
                fileSet.setFileMode(mode);
            }
            fileSet.setProject(getProject().getAnt().getProject());
            dataProducers.add(new DataProducerFileSet(fileSet));
        }

        String packagePath = getProject().getBuildDir().getPath() + File.separatorChar
                + pkg.getName() + '_' + getProject().getVersion();
        File debFile = new File(packagePath + "_all.deb");
        File changesFile = new File(packagePath + "_all.changes");

        Processor processor = new Processor(new Console() {
            @Override
            public void info(String msg) {
                getLogger().info(msg);
            }

            @Override
            public void warn(String msg) {
                getLogger().warn(msg);
            }
        }, null);

        PackageDescriptor descriptor = createDeb(debianDir, debFile, processor, dataProducers);
        createChanges(pkg, changesFile, descriptor, processor);
    }

    private void generateFile(String fileName, Map<String, Object> context) throws IOException, ClassNotFoundException {
        getLogger().info("Generating {} file...", fileName);
        InputStream inputStream = getClass().getResourceAsStream("/deb/" + fileName + ".ftl");
        BufferedReader templateReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        File outputFile = new File(getProject().getBuildDir(), "debian" + File.separatorChar + fileName);
        engine.createTemplate(templateReader).make(context).writeTo(new BufferedWriter(new FileWriter(outputFile, false)));
    }

    private PackageDescriptor createDeb(File controlDir, File debFile, Processor processor,
            List<DataProducer> dataProducers)
    {
        try {
            getLogger().info("Creating debian package: {}", debFile);
            DataProducer[] data = dataProducers.toArray(new DataProducer[dataProducers.size()]);
            return processor.createDeb(controlDir.listFiles(), data, debFile, Compression.GZIP);
        } catch (Exception e) {
            throw new GradleException("Can't build debian package " + debFile, e);
        }
    }

    private void createChanges(PackageInfo pkg, File changesFile, PackageDescriptor packageDescriptor,
            Processor processor)
    {
        try {
            getLogger().info("Creating changes file: " + changesFile);
            FileInputStream changesIn = new FileInputStream(getProject().file(pkg.getChanges()));
            TextfileChangesProvider provider = new TextfileChangesProvider(changesIn, packageDescriptor);
            processor.createChanges(packageDescriptor, provider, null, null, null, new FileOutputStream(changesFile));
        } catch (Exception e) {
            throw new GradleException("Can't create changes file " + changesFile, e);
        }

        File secRing = new File(pkg.getSecureRing().replaceFirst("^~", System.getProperty("user.home")));
        if (secRing.exists()) {
            try {
                getLogger().info("Signing changes file " + changesFile + " with key " + secRing);
                PGPSecretKey secretKey = SigningUtils.readSecretKey(new FileInputStream(secRing), pkg.getKey());

                if (secretKey != null) {
                    PGPPrivateKey privateKey = getPrivateKey(secretKey);
                    if (privateKey != null) {
                        InputStream fIn = new ByteArrayInputStream(FileUtils.readFileToString(changesFile, "UTF-8").getBytes());
                        OutputStream out = new FileOutputStream(changesFile, false);
                        SigningUtils.sign(fIn, out, secretKey, privateKey, PGPUtil.SHA256);
                        out.close();
                    }
                }
            } catch (Exception e) {
                throw new GradleException("Can't sign changes file " + changesFile, e);
            }
        }
    }

    private static PGPPrivateKey getPrivateKey(PGPSecretKey secretKey) {
        while (true) {
            try {
                String pass = PassphraseProvider.provide();
                if (pass.length() == 0) {
                    PassphraseProvider.remember(pass);
                    return null;
                }

                PGPPrivateKey key = SigningUtils.readPrivateKey(secretKey, pass);
                PassphraseProvider.remember(pass);
                return key;
            } catch (PGPException e) {
                System.err.println("Invalid passphrase. Please try again");
            }
        }
    }
}
