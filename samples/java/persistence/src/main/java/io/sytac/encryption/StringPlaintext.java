package io.sytac.encryption;

import lombok.Builder;
import lombok.Data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Data @Builder
public class StringPlaintext implements Plaintext {
    private String string;
    @Builder.Default
    private Charset charset = StandardCharsets.UTF_8;

    @Override
    public byte[] getValue() {
        return string.getBytes(this.charset);
    }
}
