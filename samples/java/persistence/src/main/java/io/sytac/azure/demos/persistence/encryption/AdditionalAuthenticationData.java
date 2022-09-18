package io.sytac.azure.demos.persistence.encryption;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.nio.charset.StandardCharsets;

@Data @AllArgsConstructor
public class AdditionalAuthenticationData {
    private byte[] value;

    public static AdditionalAuthenticationData fromString(@NonNull String str) {
        return new AdditionalAuthenticationData(str.getBytes(StandardCharsets.UTF_8));
    }
}
