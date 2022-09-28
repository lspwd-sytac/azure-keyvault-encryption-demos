package enchelper_test

import (
	"azure-keyvault-encryption-demos/enchelper"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestAESEncryptionAndDecryption(t *testing.T) {
	kiv := enchelper.NewDefaultRandomKeyAndIV()

	secretString := "this is a secret string you should only see on the console"
	plainText := enchelper.StringPlaintext{Value: secretString}

	encStr := enchelper.AesEncrypt(kiv, nil, &plainText)
	decrPlainText, err := enchelper.AesDecrypt(kiv, nil, encStr)
	assert.Nil(t, err)

	decodedStr := decrPlainText.ToStringPlaintext().Value
	assert.Equal(t, secretString, decodedStr)
}

func TestAESEncryptionAndDecryptionWithAAD(t *testing.T) {
	kiv := enchelper.NewDefaultRandomKeyAndIV()
	aad := enchelper.MakeAADFromString("aad-string")

	secretString := "this is a secret string you should only see on the console"
	plainText := enchelper.StringPlaintext{Value: secretString}

	encStr := enchelper.AesEncrypt(kiv, aad, &plainText)
	decrPlainText, err := enchelper.AesDecrypt(kiv, aad, encStr)
	assert.Nil(t, err)

	decodedStr := decrPlainText.ToStringPlaintext().Value
	assert.Equal(t, secretString, decodedStr)
}

func TestAESEncryptionWillFailWithWrongAAD(t *testing.T) {
	kiv := enchelper.NewDefaultRandomKeyAndIV()
	aad := enchelper.MakeAADFromString("aad-string")

	secretString := "this is a secret string you should only see on the console"
	plainText := enchelper.StringPlaintext{Value: secretString}

	encStr := enchelper.AesEncrypt(kiv, aad, &plainText)
	decrPlainText, err := enchelper.AesDecrypt(kiv, nil, encStr)
	assert.Nil(t, decrPlainText)
	assert.NotNil(t, err)
}
