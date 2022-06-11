package io.sytac.azure.demos.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.sytac.azure.demos.persistence.wrapping.EncryptedObjectFragment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProtectedSampleObject {
    private String guid;
    private String value;
    private EncryptedObjectFragment eof;
}
