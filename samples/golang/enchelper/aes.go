package enchelper

import (
	"crypto/aes"
	"crypto/cipher"
	"errors"
	"fmt"
)

// --------------------------------------------------------------------------------
// AES Encryption with GCM

// AesEncrypt encrypt a plain text into
func AesEncrypt(kiv *KeyAndIV, aad *AdditionalAuthenticationData, plainText Plaintext) *AuthenticatedCiphertext {
	block, _ := aes.NewCipher(kiv.Key)
	aesGCM, _ := cipher.NewGCMWithTagSize(block, kiv.tagLength/8)

	src := *plainText.GetSource()

	cipherBytes := aesGCM.Seal(nil, kiv.IV, src, aadToUse(aad))

	tagOffset := len(cipherBytes) - kiv.tagLength/8

	rv := AuthenticatedCiphertext{
		Ciphertext: Ciphertext{
			value: cipherBytes[:tagOffset],
		},
		Tag: AuthenticationTag{
			value: cipherBytes[tagOffset:],
		},
	}

	return &rv
}

func AesDecrypt(kiv *KeyAndIV, aad *AdditionalAuthenticationData, ciphertext *AuthenticatedCiphertext) (*BinaryPlaintext, error) {
	block, _ := aes.NewCipher(kiv.Key)
	// Don't use NewGCM as it will produce incompatible outputs.
	aesGCM, _ := cipher.NewGCMWithTagSize(block, len(ciphertext.Tag.value))

	if aesGCM.NonceSize() != len(ciphertext.Tag.value) {
		return nil, errors.New(fmt.Sprintf("Mismatching tag size: GCM expects %d bytes, but %d were supplied", aesGCM.NonceSize(), len(ciphertext.Tag.value)))
	}

	decrSlice := append(ciphertext.Ciphertext.value, ciphertext.Tag.value...)

	if decrBuffer, err := aesGCM.Open(nil, kiv.IV, decrSlice, aadToUse(aad)); err == nil {
		rv := BinaryPlaintext{
			value: &decrBuffer,
		}
		return &rv, nil
	} else {
		return nil, err
	}
}

// -------------------------------------------------------------------------------------------
// Private functions

//aadToUse AAD slice to use in the encryption/decryption operations.
func aadToUse(aad *AdditionalAuthenticationData) []byte {
	if aad != nil {
		return aad.GetData()
	} else {
		return []byte{}
	}
}
