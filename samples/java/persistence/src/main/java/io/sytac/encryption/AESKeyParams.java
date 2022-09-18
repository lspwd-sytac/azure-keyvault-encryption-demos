package io.sytac.encryption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder @AllArgsConstructor
public class AESKeyParams {
    @Builder.Default
    int keyBits = 128;
    @Builder.Default
    int ivBits = 96;
    @Builder.Default
    int tagLengthBits = 96;
}
