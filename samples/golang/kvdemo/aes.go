package kvdemo

import (
	"crypto/aes"
	"crypto/cipher"
)

func AesEncrypt(kiv *KeyAndIV, source *[]byte) *[]byte {
	block, _ := aes.NewCipher(kiv.Key)
	aesGCM, _ := cipher.NewGCM(block)

	rv := aesGCM.Seal(nil, kiv.IV, *source, nil)
	return &rv
}

func AesDecrypt(kiv *KeyAndIV, cipherText *[]byte) (*[]byte, error) {
	block, _ := aes.NewCipher(kiv.Key)
	aesGCM, _ := cipher.NewGCM(block)

	if rv, err := aesGCM.Open(nil, kiv.IV, *cipherText, nil); err == nil {
		return &rv, nil
	} else {
		return nil, err
	}
}
