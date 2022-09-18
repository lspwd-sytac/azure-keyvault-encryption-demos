package io.sytac.azure.demos.persistence.wrapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sytac.encryption.AdditionalAuthenticationData;

/**
 * A base class for storing the encrypted object fragment and calculating the additional authentication data,
 * if the encryption strategy requires this.
 */
public abstract class StoredEncryptedObject {

    @JsonProperty("enc")
    private EncryptedObjectFragment encryptedFragment;

    public EncryptedObjectFragment getEncryptedFragment() {
        return encryptedFragment;
    }

    public void setEncryptedFragment(EncryptedObjectFragment encryptedFragment) {
        this.encryptedFragment = encryptedFragment;
    }

    public AdditionalAuthenticationData getAdditionalAuthenticationData() {
        return null;
    }
}
