package kvdemo

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestAESEncryptionAndDecryption(t *testing.T) {
	kiv := NewDefaultRandomKeyAndIV()

	secretString := "this is a secret string you should only see on the console"
	plainTextBytes := []byte(secretString)

	encStr := AesEncrypt(kiv, &plainTextBytes)
	decBytes, err := AesDecrypt(kiv, encStr)
	assert.Nil(t, err)

	decodedStr := string(*decBytes)
	assert.Equal(t, secretString, decodedStr)
}
