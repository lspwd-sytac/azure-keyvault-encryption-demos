package io.sytac.encryption;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES {

    private static final String AES_ALGORITHM = "AES";

    private AES() {
        // This class should not be instantiated directly.
    }

    public static AuthenticatedCiphertext encrypt(KeyAndIV kiv, Plaintext... sources) {
        return encrypt(kiv, null, sources);
    }

    public static AuthenticatedCiphertext encrypt(KeyAndIV kiv, AdditionalAuthenticationData aad, Plaintext... source) {
        try {
            Cipher ch = Cipher.getInstance(kiv.getCipherName());

            SecretKeySpec sks = new SecretKeySpec(kiv.getKey().getValue(), AES_ALGORITHM);
            ch.init(Cipher.ENCRYPT_MODE, sks, kiv.createAlgorithmSpec());
            if (aad != null) ch.updateAAD(aad.getValue());

            return encrypt(ch, source, kiv.getTagLength() / 8);
        } catch  (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException("The JVM does not support encryption parameters required by the key", e);
        }
    }

    private static AuthenticatedCiphertext encrypt(Cipher ch, Plaintext[] source, int tagLengthInBytes) throws IllegalBlockSizeException, BadPaddingException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            var rv = AuthenticatedCiphertext.builder();
            for (Plaintext s : source) baos.write(ch.update(s.getValue()));
            byte[] finalOp = ch.doFinal();

            int tagOffset = finalOp.length - tagLengthInBytes;

            baos.write(finalOp, 0, tagOffset);

            return rv.ciphertext(new Ciphertext(baos.toByteArray()))
                    .tag(new AuthenticationTag(finalOp, tagOffset, tagLengthInBytes))
                    .build();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Encrypts the plain text and outputs the ciphertext with the authentication tag stripped.
     * @param kiv Key and initialization vector
     * @param plain plain-test stream
     * @param out output stream where to store encrypted test
     * @return authentication tag
     * @throws IOException if I/O operations would fail
     */
    public static AuthenticationTag encrypt(KeyAndIV kiv, InputStream plain, OutputStream out) throws IOException {
        return encrypt(kiv, null, plain, out);
    }
    public static AuthenticationTag encrypt(KeyAndIV kiv, AdditionalAuthenticationData aad, InputStream plain, OutputStream out) throws IOException {
        TrailingBytesExtractingOutputStream tbs = new TrailingBytesExtractingOutputStream(kiv, out);
        encryptStream(kiv, aad, plain, tbs);
        return new AuthenticationTag(tbs.tag());
    }

    /**
     * Create a cipher from the supplied key and initialization vector and then encrypt the plain-text stream.
     * @param kiv key and initialization vector
     * @param plain plain input stream
     * @param out output stream where to store the ciphertext
     * @throws IOException if the output into the target stream will fail .
     */
    public static void encryptStream(KeyAndIV kiv, AdditionalAuthenticationData aad, InputStream plain, OutputStream out) throws IOException {
        try {
            Cipher ch = Cipher.getInstance(kiv.getCipherName());

            SecretKeySpec sks = new SecretKeySpec(kiv.getKey().getValue(), AES_ALGORITHM);
            if (aad != null) {
                ch.updateAAD(aad.getValue());
            }
            ch.init(Cipher.ENCRYPT_MODE, sks, kiv.createAlgorithmSpec());

            try (CipherOutputStream cos = new CipherOutputStream(out, ch)) {
                byte[] buf = new byte[10240];
                int k;
                while ((k = plain.read(buf)) > 0) {
                    cos.write(buf, 0, k);
                }
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | InvalidKeyException ex) {
            throw new IllegalStateException("Invalid AES key parameter, or these parameters are not supported by this JVM", ex);
        }
    }

    public static BinaryPlaintext decrypt(KeyAndIV kiv, AuthenticatedCiphertext text) throws BadPaddingException {
        return decrypt(kiv, null, text);
    }

    public static BinaryPlaintext decrypt(KeyAndIV kiv, AdditionalAuthenticationData aad, AuthenticatedCiphertext text) throws BadPaddingException {
        try {
            Cipher ch = Cipher.getInstance(kiv.getCipherName());
            SecretKeySpec sks = new SecretKeySpec(kiv.getKey().getValue(), AES_ALGORITHM);
            ch.init(Cipher.DECRYPT_MODE, sks, kiv.createAlgorithmSpec());
            if (aad != null) {
                ch.updateAAD(aad.getValue());
            }
            ch.update(text.getCiphertext().getValue());

            var decryptedBinary = ch.doFinal(text.getTag().getValue());
            return new BinaryPlaintext(decryptedBinary);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException  ex) {
            throw new IllegalStateException("The JVM does not support required key", ex);
        }
    }

    public static InputStream decrypt(KeyAndIV kiv, InputStream cipherText, AuthenticationTag tag) {
        try {
            Cipher ch = Cipher.getInstance(kiv.getCipherName());
            SecretKeySpec sks = new SecretKeySpec(kiv.getKey().getValue(), AES_ALGORITHM);
            ch.init(Cipher.DECRYPT_MODE, sks, kiv.createAlgorithmSpec());

            return new CipherInputStream(new SequenceInputStream(cipherText, new ByteArrayInputStream(tag.getValue())), ch);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | InvalidKeyException ex) {
            throw new IllegalStateException("Invalid AES key parameter, or these parameters are not supported by this JVM", ex);
        }
    }

    /**
     * A flavour of the output stream that withholds last N bytes written in the outptu
     */
    static class TrailingBytesExtractingOutputStream extends OutputStream {
        private final int tailLength;
        private final int[] tailBuffer;

        final OutputStream target;

        private int bufPtr;
        private boolean minimalFill;

        TrailingBytesExtractingOutputStream(KeyAndIV kiv, OutputStream target) {
            this.tailLength = kiv.getTagLength() / 8;
            tailBuffer = new int[this.tailLength];

            this.target = target;
            this.bufPtr = 0;
            this.minimalFill = false;
        }

        // Ideally we may want to provide the implementation of the write(array, int, int) method
        // to gain performance. This demo code does not contain this code as it deemed to be
        // over-complication of the basic encryption demonstration.

        @Override
        public void write(int b) throws IOException {
            if (minimalFill) {
                target.write(tailBuffer[bufPtr]);
                tailBuffer[bufPtr] = b;
            } else {
                tailBuffer[bufPtr] = b;
            }

            bufPtr++;
            if (bufPtr == tailLength) {
                bufPtr = 0;
                minimalFill = true;
            }
        }

        public byte[] tag() {
            byte[] retVal = new byte[tailLength];
            int rvPtr = 0;

            for (int k = bufPtr; k< tailLength; k++) {
                retVal[rvPtr] = (byte)this.tailBuffer[k];
                rvPtr++;
            }
            for (int k=0; k<bufPtr; k++) {
                retVal[rvPtr] = (byte)this.tailBuffer[k];
                rvPtr++;
            }

            return retVal;
        }

        @Override
        public void flush() throws IOException {
            target.flush();
        }

        @Override
        public void close() throws IOException {
            target.close();
        }
    }
}
