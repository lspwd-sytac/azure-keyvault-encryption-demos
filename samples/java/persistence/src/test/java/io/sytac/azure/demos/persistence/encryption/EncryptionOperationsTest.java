package io.sytac.azure.demos.persistence.encryption;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sytac.encryption.*;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public class EncryptionOperationsTest {

    @Test
    public void testWillEncryptUsingRandom() throws BadPaddingException {
        var kiv = GCMKeyAndIVFactory.defaultRandom();
        var plainInput = StringPlaintext.builder().string("12345").build();

        AuthenticatedCiphertext encr = AES.encrypt(kiv, plainInput);
        BinaryPlaintext decr = AES.decrypt(kiv, encr);

        assertEquals(plainInput.getString(), decr.asString());
    }

    @Test()
    public void testWillRequireAADToDecrypt() {
        var kiv = GCMKeyAndIVFactory.defaultRandom();
        var plainInput = StringPlaintext.builder().string("12345").build();

        AuthenticatedCiphertext encr = AES.encrypt(kiv, AdditionalAuthenticationData.fromString("aad-string"), plainInput);

        assertThrows(BadPaddingException.class, () -> AES.decrypt(kiv, encr));

        assertThrows(BadPaddingException.class, () -> AES.decrypt(kiv, AdditionalAuthenticationData.fromString("wrong-aad"), encr));
    }

    @Test()
    public void testExportInteroperableExchangeWithAD() throws IOException {
        var kiv = GCMKeyAndIVFactory.defaultRandom();
        var plainInput = StringPlaintext.builder().string(String.format("authenticated-12345-%s", Instant.now().toString())).build();

        AuthenticatedCiphertext encr = AES.encrypt(kiv, AdditionalAuthenticationData.fromString("aad-string"), plainInput);

        var dto = KeyAndCiphertextTransferDTO.builder()
                .msg(encr)
                .kiv(kiv)
                .build();

        Files.write(Path.of("../../java-output-with-ad.json"), new ObjectMapper().writeValueAsBytes(dto));
    }

    @Test()
    public void testExportInteroperableExchange() throws IOException {
        var kiv = GCMKeyAndIVFactory.defaultRandom();
        var plainInput = StringPlaintext.builder().string(String.format("non-authenticated-12345-%s", Instant.now().toString())).build();

        AuthenticatedCiphertext encr = AES.encrypt(kiv, plainInput);

        var dto = KeyAndCiphertextTransferDTO.builder()
                .msg(encr)
                .kiv(kiv)
                .build();

        System.out.println(Hex.encodeHexString(encr.getCiphertext().getValue()));
        System.out.println(Hex.encodeHexString(encr.getTag().getValue()));

        Files.write(Path.of("../../java-output.json"), new ObjectMapper().writeValueAsBytes(dto));
    }

    @Test()
    public void testWillRequireAADToDecryptAndWillSucceed() throws BadPaddingException {
        var kiv = GCMKeyAndIVFactory.defaultRandom();
        var plainInput = StringPlaintext.builder().string("12345").build();

        AuthenticatedCiphertext encr = AES.encrypt(kiv, AdditionalAuthenticationData.fromString("aad-string"), plainInput);

        var decr = AES.decrypt(kiv, AdditionalAuthenticationData.fromString("aad-string"), encr);
        assertEquals("12345", decr.asString());
    }

    @Test
    public void testWillEncryptInStreamMode() throws IOException {
        var kiv = GCMKeyAndIVFactory.defaultRandom();

        String plainInput = "ABCDEFGH";
        ByteArrayOutputStream cipherText = new ByteArrayOutputStream();

        var tag = AES.encrypt(kiv, new ByteArrayInputStream(plainInput.getBytes(UTF_8)), cipherText);

        InputStream decryptedStream = AES.decrypt(kiv, new ByteArrayInputStream(cipherText.toByteArray()), tag);

        ByteArrayOutputStream dec = new ByteArrayOutputStream();
        decryptedStream.transferTo(dec);

        assertEquals(plainInput, dec.toString(UTF_8));
    }
}
