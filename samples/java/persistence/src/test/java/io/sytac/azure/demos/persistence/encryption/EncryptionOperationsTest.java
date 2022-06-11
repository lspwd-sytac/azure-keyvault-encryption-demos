package io.sytac.azure.demos.persistence.encryption;

import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EncryptionOperationsTest {

    @Test
    public void testWillEncryptUsingRandom() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        var kiv =  KeyAndIVFactory.defaultRandom();
        String plainInput = "12345";

        CiphertextAndTag encr = AES.encrypt(kiv, plainInput.getBytes());
        byte[] decr = AES.decrypt(kiv, encr.getCiphertext(), encr.getTag());
        assertEquals(plainInput, new String(decr));
    }
}
