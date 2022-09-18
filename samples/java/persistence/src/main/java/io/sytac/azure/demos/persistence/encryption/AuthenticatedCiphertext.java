package io.sytac.azure.demos.persistence.encryption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data @Builder
@AllArgsConstructor
public class AuthenticatedCiphertext {
    private Ciphertext ciphertext;
    private AuthenticationTag tag;
}
