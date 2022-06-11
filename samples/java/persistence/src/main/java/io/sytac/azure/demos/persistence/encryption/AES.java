package io.sytac.azure.demos.persistence.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES {

    public static CiphertextAndTag encrypt(KeyAndIV kiv, byte[]... source) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher ch = Cipher.getInstance(kiv.getCipher());
        SecretKeySpec sks = new SecretKeySpec(kiv.getKey(), "AES");
        ch.init(Cipher.ENCRYPT_MODE, sks, kiv.createAlgorithmSpec());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            var rv = CiphertextAndTag.builder();
            for (byte[] s : source) {
                baos.write(ch.update(s));
            }
            return rv.ciphertext(baos.toByteArray()).tag(ch.doFinal()).build();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static byte[] decrypt(KeyAndIV kiv, byte[] body, byte[] tag) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher ch = Cipher.getInstance(kiv.getCipher());
        SecretKeySpec sks = new SecretKeySpec(kiv.getKey(), "AES");
        ch.init(Cipher.DECRYPT_MODE, sks, kiv.createAlgorithmSpec());
        ch.update(body);

        return ch.doFinal(tag);
    }
}
