package io.sytac.azure.demos.persistence.encryption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public class EncryptionOperationsTest {

    @Test
    public void testWillEncryptUsingRandom() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        var kiv = KeyAndIVFactory.defaultRandom();
        var plainInput = StringPlaintext.builder().string("12345").build();

        AuthenticatedCiphertext encr = AES.encrypt(kiv, plainInput);
        BinaryPlaintext decr = AES.decrypt(kiv, encr);

        assertEquals(plainInput.getString(), decr.asString());
    }

    @Test()
    public void testWillRequireAADToDecrypt() throws IOException {
        var kiv = KeyAndIVFactory.defaultRandom();
        var plainInput = StringPlaintext.builder().string("12345").build();

        AuthenticatedCiphertext encr = AES.encrypt(kiv, AdditionalAuthenticationData.fromString("aad-string"), plainInput);

        var dto = KeyAndCiphertextTransferDTO.builder()
                .msg(encr)
                .kiv(kiv)
                .build();

        Files.write(Path.of("../java-output.json"), new ObjectMapper().writeValueAsBytes(dto));

        assertThrows(BadPaddingException.class, () -> {
            AES.decrypt(kiv, encr);
        });

        assertThrows(BadPaddingException.class, () -> {
            AES.decrypt(kiv, AdditionalAuthenticationData.fromString("wrong-aad"), encr);
        });
    }

    @Test()
    public void testWillRequireAADToDecryptAndWillSucceed() throws BadPaddingException {
        var kiv = KeyAndIVFactory.defaultRandom();
        var plainInput = StringPlaintext.builder().string("12345").build();

        AuthenticatedCiphertext encr = AES.encrypt(kiv, AdditionalAuthenticationData.fromString("aad-string"), plainInput);

        var decr = AES.decrypt(kiv, AdditionalAuthenticationData.fromString("aad-string"), encr);
        assertEquals("12345", decr.asString());
    }

    @Test
    public void testWillEncryptInStreamMode() throws IOException {
        var kiv = KeyAndIVFactory.defaultRandom();

        String plainInput = "ABCDEFGH";
        ByteArrayOutputStream cipherText = new ByteArrayOutputStream();

        var tag = AES.encrypt(kiv, new ByteArrayInputStream(plainInput.getBytes(UTF_8)), cipherText);

        InputStream decryptedStream = AES.decrypt(kiv, new ByteArrayInputStream(cipherText.toByteArray()), tag);

        ByteArrayOutputStream dec = new ByteArrayOutputStream();
        decryptedStream.transferTo(dec);

        assertEquals(plainInput, dec.toString(UTF_8));
    }
}
