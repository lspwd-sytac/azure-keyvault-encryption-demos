package kvdemo

import (
	"context"
	"encoding/base64"
	"encoding/json"
	"github.com/Azure/azure-sdk-for-go/services/keyvault/v7.0/keyvault"
	"time"
)

type KeyReference struct {
	Name    string `json:"name"`
	Version string `json:"version"`
}

type EncryptedObjectFragment struct {
	MarshalledValue string       `json:"v"`
	WrappedKey      string       `json:"k"`
	Tag             string       `json:"t"`
	EncryptionDate  time.Time    `json:"d"`
	KeyReference    KeyReference `json:"kek"`
}

type KeyVaultWrapper struct {
	cryptoClient *keyvault.BaseClient
	vaultURL     string
	keyName      string
	keyVersion   string
}

func (kvw *KeyVaultWrapper) Unwrap(fragment *EncryptedObjectFragment, obj interface{}) error {
	if res, err := kvw.cryptoClient.UnwrapKey(context.TODO(), kvw.vaultURL, kvw.keyName, kvw.keyVersion, keyvault.KeyOperationsParameters{
		Algorithm: keyvault.RSAOAEP256,
		Value:     &fragment.WrappedKey,
	}); err != nil {
		return err
	} else {
		skivJSON, err := base64.RawStdEncoding.DecodeString(*res.Result)
		if err != nil {
			return err
		}

		skiv := SerializedKeyAndIV{}
		err = json.Unmarshal(skivJSON, &skiv)
		if err != nil {
			return err
		}

		kiv := KeyAndIV{}
		err = kiv.deserialize(skiv)
		if err != nil {
			return err
		}

		payloadCipherTextBytes, _ := base64.StdEncoding.DecodeString(fragment.MarshalledValue)
		payloadBytes, err := AesDecrypt(&kiv, &payloadCipherTextBytes)
		if err != nil {
			return err
		}

		return json.Unmarshal(*payloadBytes, &obj)
	}
}

func (kvw *KeyVaultWrapper) Wrap(obj interface{}) (*EncryptedObjectFragment, error) {
	kiv := NewDefaultRandomKeyAndIV()

	kivByte, _ := json.Marshal(kiv.serialize())
	kivBytesURLEnc := base64.URLEncoding.EncodeToString(kivByte)

	if res, err := kvw.cryptoClient.WrapKey(context.TODO(), kvw.vaultURL, kvw.keyName, kvw.keyVersion, keyvault.KeyOperationsParameters{
		Algorithm: keyvault.RSAOAEP256,
		Value:     &kivBytesURLEnc,
	}); err != nil {
		return nil, err
	} else {
		jsonBytes, _ := json.Marshal(obj)
		encBytes := AesEncrypt(kiv, &jsonBytes)

		rv := EncryptedObjectFragment{
			MarshalledValue: base64.StdEncoding.EncodeToString(*encBytes),
			WrappedKey:      *res.Result,
			EncryptionDate:  time.Now(),
		}

		return &rv, nil
	}
}
