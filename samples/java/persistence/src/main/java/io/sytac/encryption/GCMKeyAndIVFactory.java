package io.sytac.encryption;

import java.security.SecureRandom;

public class GCMKeyAndIVFactory {
    static final SecureRandom random = new SecureRandom();

    static final int DEFAULT_AES_KEY_BYTE_LENGTH = 128/8;
    static final int DEFAULT_AES_IV_BYTE_LENGTH = 96/8;
    static final int DEFAULT_AES_TAG_BIT_LENGTH = 96;


    public static KeyAndIV defaultRandom() {
        return newRandomFromValidatedParams(DEFAULT_AES_KEY_BYTE_LENGTH, DEFAULT_AES_IV_BYTE_LENGTH, DEFAULT_AES_TAG_BIT_LENGTH);
    }

    public static KeyAndIV newRandom(int keyBits, int ivBits, int tagLengthBits) {
        if (keyBits % 8 != 0) {
            throw new IllegalArgumentException("Key size must be modulo 8");
        } else if (ivBits % 8 != 0) {
            throw new IllegalArgumentException("Initialization vector size must be modulo 8");
        } else if (tagLengthBits % 8 != 0) {
            throw new IllegalArgumentException("Tag length size must be modulo 8");
        }

        return newRandomFromValidatedParams(keyBits/8, ivBits/8, tagLengthBits);
    }

    private static KeyAndIV newRandomFromValidatedParams(int keyBytes, int ivBytes, int tagLengthBits) {
        byte[] iv = new byte[ivBytes];
        random.nextBytes(iv);

        byte[] key = new byte[keyBytes];
        random.nextBytes(key);

        return GCMKeyAndIV.builder().IV(new InitializationVector(iv)).key(new EncryptionKey(key)).tagLength(tagLengthBits).build();
    }
}
