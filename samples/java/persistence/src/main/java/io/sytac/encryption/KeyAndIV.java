package io.sytac.encryption;

import java.security.spec.AlgorithmParameterSpec;

/**
 * Key and IV (initialization vector) for AEAD (authenticated encryption with associated data).
 */
public interface KeyAndIV {
    AlgorithmParameterSpec createAlgorithmSpec();

    String getCipherName();

    InitializationVector getIV();

    EncryptionKey getKey();

    int getTagLength();

    SerializedKeyAndIV serialize();
}
