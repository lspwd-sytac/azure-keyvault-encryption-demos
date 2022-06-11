package io.sytac.azure.demos.persistence.wrapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reference to the KeyVault key that was used to encrypt/decrypt operations. This allows the application to use multiple
 * keys and versions at the same time. This makes key rotation and re-encryption much easier.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class KeyReference {

    @JsonProperty("kn")
    private String name;

    @JsonProperty("kv")
    private String version;
}
