package io.sytac.azure.demos.persistence.encryption;

import java.security.SecureRandom;

public class KeyAndIVFactory {
    static final SecureRandom random = new SecureRandom();

    static final int DEFAULT_AES_KEY_LENGTH = 128;
    static final int DEFAULT_AES_IV_LENGTH = 96;
    static final int DEFAULT_AES_TAG_LENGTH = 96;


    public static KeyAndIV defaultRandom() {
        return newRandom(DEFAULT_AES_KEY_LENGTH/8, DEFAULT_AES_IV_LENGTH/8, DEFAULT_AES_TAG_LENGTH);
    }

    public static KeyAndIV newRandom(int keyBytes, int ivBytes, int tagLength) {
        byte[] iv = new byte[ivBytes];
        random.nextBytes(iv);

        byte[] key = new byte[keyBytes];
        random.nextBytes(key);

        return KeyAndIV.builder().IV(iv).key(key).tagLength(tagLength).build();
    }
}
