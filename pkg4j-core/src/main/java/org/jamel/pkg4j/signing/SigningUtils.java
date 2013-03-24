package org.jamel.pkg4j.signing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

/**
 * @author Sergey Polovko
 */
public class SigningUtils {

    private static int readInputLine(ByteArrayOutputStream bOut, InputStream fIn) throws IOException {
        bOut.reset();

        int lookAhead = -1;
        int ch;

        while ((ch = fIn.read()) >= 0) {
            bOut.write(ch);
            if (ch == '\r' || ch == '\n') {
                lookAhead = readPassedEOL(bOut, ch, fIn);
                break;
            }
        }

        return lookAhead;
    }

    private static int readInputLine(ByteArrayOutputStream bOut, int lookAhead, InputStream fIn) throws IOException {
        bOut.reset();

        int ch = lookAhead;

        while (true) {
            bOut.write(ch);
            if (ch == '\r' || ch == '\n') {
                lookAhead = readPassedEOL(bOut, ch, fIn);
                break;
            }

            if ((ch = fIn.read()) < 0) break;
        }

        if (ch < 0) lookAhead = -1;

        return lookAhead;
    }

    private static int readPassedEOL(ByteArrayOutputStream bOut, int lastCh, InputStream fIn) throws IOException {
        int lookAhead = fIn.read();

        if (lastCh == '\r' && lookAhead == '\n') {
            bOut.write(lookAhead);
            lookAhead = fIn.read();
        }

        return lookAhead;
    }

    public static PGPSecretKey readSecretKey(InputStream input, String keyId) throws IOException, PGPException {
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(input));

        Iterator keyRingIter = pgpSec.getKeyRings();

        while (keyRingIter.hasNext()) {
            PGPSecretKeyRing kRing = (PGPSecretKeyRing) keyRingIter.next();
            Iterator keyIter = kRing.getSecretKeys();

            while (keyIter.hasNext()) {
                PGPSecretKey key = (PGPSecretKey) keyIter.next();

                if (keyId != null) {
                    if (key.isSigningKey() && Long.toHexString(key.getKeyID() & 0xFFFFFFFFL).equals(keyId.toLowerCase())) {
                        return key;
                    }
                } else if (key.isSigningKey()) {
                    return key;
                }
            }
        }

        return null;
    }

    /**
     * create a clear text signed file.
     */
    public static void sign(InputStream fIn, OutputStream out, PGPSecretKey pgpSecKey, PGPPrivateKey pgpPrivKey, int digest)
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, PGPException, SignatureException
    {
        PGPSignatureGenerator sGen = new PGPSignatureGenerator(new BcPGPContentSignerBuilder(pgpSecKey.getPublicKey().getAlgorithm(), digest));
        PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();

        sGen.init(PGPSignature.CANONICAL_TEXT_DOCUMENT, pgpPrivKey);

        Iterator it = pgpSecKey.getPublicKey().getUserIDs();
        if (it.hasNext()) {
            spGen.setSignerUserID(false, (String) it.next());
            sGen.setHashedSubpackets(spGen.generate());
        }

        ArmoredOutputStream aOut = new ArmoredOutputStream(out);
        aOut.beginClearText(digest);

        //
        // note the last \n/\r/\r\n in the file is ignored
        //
        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
        int lookAhead = readInputLine(lineOut, fIn);

        processLine(aOut, sGen, lineOut.toByteArray());

        if (lookAhead != -1) {
            while (true) {
                lookAhead = readInputLine(lineOut, lookAhead, fIn);

                sGen.update((byte) '\r');
                sGen.update((byte) '\n');

                processLine(aOut, sGen, lineOut.toByteArray());
                if (lookAhead == -1) break;
            }
        }

        fIn.close();

        aOut.endClearText();

        BCPGOutputStream bOut = new BCPGOutputStream(aOut);

        sGen.generate().encode(bOut);

        aOut.close();
    }

    public static PGPPrivateKey readPrivateKey(PGPSecretKey pgpSecKey, String pass) throws PGPException {
        return pgpSecKey.extractPrivateKey(new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider())
                .build(pass.toCharArray()));
    }

    private static void processLine(OutputStream aOut, PGPSignatureGenerator sGen, byte[] line)
            throws SignatureException, IOException
    {
        // note: trailing white space needs to be removed from the end of
        // each line for signature calculation RFC 4880 Section 7.1
        int length = getLengthWithoutWhiteSpace(line);
        if (length > 0) {
            sGen.update(line, 0, length);
        }

        aOut.write(line, 0, line.length);
    }

    private static boolean isLineEnding(byte b) {
        return b == '\r' || b == '\n';
    }

    private static int getLengthWithoutWhiteSpace(byte[] line) {
        int end = line.length - 1;

        while (end >= 0 && isWhiteSpace(line[end])) {
            end--;
        }

        return end + 1;
    }

    private static boolean isWhiteSpace(byte b) {
        return isLineEnding(b) || b == '\t' || b == ' ';
    }
}
