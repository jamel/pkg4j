package org.jamel.pkg4j.gradle.tasks

import groovy.text.GStringTemplateEngine
import org.apache.tools.ant.taskdefs.Tar
import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPPrivateKey
import org.bouncycastle.openpgp.PGPSecretKey
import org.bouncycastle.openpgp.PGPUtil
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.jamel.pkg4j.gradle.PackageInfo
import org.jamel.pkg4j.gradle.convention.CommonPkgConvention
import org.jamel.pkg4j.gradle.convention.PkgConvention
import org.jamel.pkg4j.gradle.utils.PassphraseProvider
import org.jamel.pkg4j.signing.SigningUtils
import org.vafer.jdeb.Compression
import org.vafer.jdeb.Console
import org.vafer.jdeb.DataProducer
import org.vafer.jdeb.Processor
import org.vafer.jdeb.changes.TextfileChangesProvider
import org.vafer.jdeb.descriptors.PackageDescriptor
import org.vafer.jdeb.producers.DataProducerFileSet

/**
 * @author Sergey Polovko
 */
class BuildDebTask extends DefaultTask {

    private final GStringTemplateEngine engine = new GStringTemplateEngine()


    @TaskAction
    def void run() throws Exception {
        def pkg = project.convention.plugins.pkg as PkgConvention
        def commonPkg = project.rootProject.convention.plugins.commonPkg as CommonPkgConvention

        if (commonPkg != null) {
            logger.info("Common package config is: ")
            logger.info(commonPkg.packageInfo.toString())

            // merge pkg configuration with commonPkg
            // pkg have a priority to commonPkg
            for (PackageInfo packageInfo : pkg.packages) {
                packageInfo.mergeWithCommon(commonPkg.packageInfo)
            }
        } else {
          for (PackageInfo packageInfo : pkg.packages) {
            packageInfo.mergeWithCommon(null)
          }
        }

        for (PackageInfo packageInfo : pkg.packages) {
            buildPackage(packageInfo)
        }
    }

    private void buildPackage(PackageInfo pkg) {
        if (!pkg.needBuild) return

        logger.info("Build package ${pkg.name} with such configuration: ")
        logger.info(pkg.toString())

        def debianDir = new File(project.buildDir, "debian")
        debianDir.deleteDir()
        debianDir.mkdirs()

        def context = pkg.toContext()
        generateFile("control", context)
        generateFile("conffiles", context)
        generateFile("postinst", context)
        if (pkg.prermCommands) {
          generateFile("prerm", context)
        }
        generateFile("postrm", context)

        DataProducer[] dataProducers = pkg.dirsToPack.collect {dir ->
            def fileSet = new Tar.TarFileSet()
            fileSet.dir = project.file(dir["dir"])
            fileSet.prefix = dir["prefix"]
            if (dir["mode"]) {
                fileSet.fileMode = dir["mode"]
            }
            fileSet.project = project.ant.project
            new DataProducerFileSet(fileSet)
        }.toArray() as DataProducer[]

        def packagePath = project.buildDir.path + File.separatorChar + pkg.name + '_' + project.version
        def debFile = new File("${packagePath}_all.deb")
        def changesFile = new File("${packagePath}_all.changes")

        def processor = new Processor([
                info: {msg -> logger.info(msg) },
                warn: {msg -> logger.warn(msg) }] as Console, null)

        def descriptor = createDeb(debianDir, debFile, processor, dataProducers)
        createChanges(pkg, changesFile, descriptor, processor)
    }

    def generateFile(String fileName, Map context) {
        logger.info("Generating ${fileName} file...")
        def template = getClass().getResourceAsStream("/deb/${fileName}.ftl").newReader()
        def content = engine.createTemplate(template).make(context).toString()
        new File(project.buildDir, "debian/${fileName}").text = content
    }

    private PackageDescriptor createDeb(File controlDir, File debFile, Processor processor, DataProducer[] data) {
        try {
            logger.info("Creating debian package: ${debFile}")
            return processor.createDeb(controlDir.listFiles(), data, debFile, Compression.GZIP)
        } catch (Exception e) {
            throw new GradleException("Can't build debian package ${debFile}", e)
        }
    }

    private void createChanges(PackageInfo pkg, File changesFile, PackageDescriptor descriptor, Processor processor) {
        try {
            logger.info("Creating changes file ${changesFile}")
            def changesIn = project.file(pkg.changes).newInputStream()
            def provider = new TextfileChangesProvider(changesIn, descriptor)
            processor.createChanges(descriptor, provider, null, null, null, changesFile.newOutputStream())
        } catch (Exception e) {
            throw new GradleException("Can't create changes file " + changesFile, e)
        }

        def secRing = new File(pkg.secureRing.replaceFirst("^~", System.getProperty("user.home")))
        if (secRing.exists()) {
            signChangesFile(changesFile, secRing, pkg.key)
        } else {
            logger.info("Secure keyring file does not exists. Changes will not be signed")
        }
    }

    private void signChangesFile(File changesFile, File secRing, String keyId) {
        try {
            logger.info("Signing changes file ${changesFile} with ${secRing} and keyId ${keyId}")
            def secretKey = SigningUtils.readSecretKey(secRing.newInputStream(), keyId)

            if (secretKey) {
                def privateKey = getPrivateKey(secretKey)
                if (privateKey) {
                    InputStream fIn = new ByteArrayInputStream(changesFile.getText("UTF-8").bytes)
                    OutputStream fOut = new FileOutputStream(changesFile, false) // override previous file
                    SigningUtils.sign(fIn, fOut, secretKey, privateKey, PGPUtil.SHA256)
                    fOut.close()
                }
            }
        } catch (Exception e) {
            throw new GradleException("Can't sign changes file " + changesFile, e)
        }
    }

    private static PGPPrivateKey getPrivateKey(PGPSecretKey secretKey) {
        while (true) {
            try {
                String pass = PassphraseProvider.provide()
                if (pass.length() == 0) {
                    PassphraseProvider.remember(pass)
                    return null
                }

                PGPPrivateKey key = SigningUtils.readPrivateKey(secretKey, pass)
                PassphraseProvider.remember(pass)
                return key
            } catch (PGPException e) {
                System.err.println("Invalid passphrase. Please try again")
            }
        }
    }
}
