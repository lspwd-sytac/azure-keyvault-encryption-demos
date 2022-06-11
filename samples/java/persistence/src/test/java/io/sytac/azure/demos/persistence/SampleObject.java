package io.sytac.azure.demos.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SampleObject {
    private String guid;
    private String value;
    private String secretValue;
    private int anotherSecretValue;

    public static Map<String,Object> identify(SampleObject obj) {
        assertNotNull(obj.getGuid(), () -> "Assign ID ot this object before storing it in the database");
        return Collections.singletonMap("guid", obj.getGuid());
    }
}
