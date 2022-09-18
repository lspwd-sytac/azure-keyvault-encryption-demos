package io.sytac.encryption;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Data @AllArgsConstructor
public class BinaryPlaintext implements Plaintext {
    private byte[] value;

    public String asString() {
        return asString(StandardCharsets.UTF_8);
    }
    public String asString(Charset ch) {
        return new String(value, ch);
    }
}
