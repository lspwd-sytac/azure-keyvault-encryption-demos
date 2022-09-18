package io.sytac.azure.demos.persistence.encryption;

import io.sytac.encryption.AuthenticatedCiphertext;
import io.sytac.encryption.KeyAndIV;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class KeyAndCiphertextTransferDTO {

    private KeyAndIV kiv;
    private AuthenticatedCiphertext msg;
}
