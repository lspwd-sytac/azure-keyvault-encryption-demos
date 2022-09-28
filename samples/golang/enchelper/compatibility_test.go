package enchelper_test

import (
	"azure-keyvault-encryption-demos/enchelper"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"github.com/stretchr/testify/assert"
	"io/ioutil"
	"testing"
)

type StringValue struct {
	Value string `json:"value"`
}

type InterOpKeyAndIV struct {
	Key StringValue `json:"key"`
	IV  StringValue `json:"iv"`
}

func (kiv *InterOpKeyAndIV) asSerializedKeyAndIV() enchelper.SerializedKeyAndIV {
	rv := enchelper.SerializedKeyAndIV{
		Cipher:    "aes-128-gcm", // Hard-coded for the demo purposes
		Key:       kiv.Key.Value,
		IV:        kiv.IV.Value,
		TagLength: 12 * 8, // Hard-coded for the demo purposes
	}

	return rv
}

type InterOpMsg struct {
	Ciphertext StringValue `json:"ciphertext"`
	Tag        StringValue `json:"tag"`
}

func (msg *InterOpMsg) asAuthenticatedCiphertext() enchelper.AuthenticatedCiphertext {
	cipherBytes, e1 := base64.RawStdEncoding.DecodeString(msg.Ciphertext.Value)
	tagBytes, e2 := base64.RawStdEncoding.DecodeString(msg.Tag.Value)

	fmt.Println(e1)
	fmt.Println(e2)

	rv := enchelper.AuthenticatedCiphertext{
		Ciphertext: enchelper.MakeCiphertext(cipherBytes),
		Tag:        enchelper.MakeAuthenticationTag(tagBytes),
	}

	return rv
}

type InterOpExchange struct {
	KeyAndIV InterOpKeyAndIV `json:"kiv"`
	Msg      InterOpMsg      `json:"msg"`
}

func TestWillDecryptJavaOutput(t *testing.T) {
	rawContent, err := ioutil.ReadFile("../../java-output.json")
	assert.Nil(t, err)
	assert.NotNil(t, rawContent)

	var msg InterOpExchange
	err = json.Unmarshal(rawContent, &msg)
	assert.Nil(t, err)

	kiv := enchelper.KeyAndIV{}
	kiv.Deserialize(msg.KeyAndIV.asSerializedKeyAndIV())

	refCipher := enchelper.AesEncrypt(&kiv, nil, &enchelper.StringPlaintext{Value: "non-authenticated-12345-2022-09-19T20:21:40.034934400Z"})
	fmt.Println(refCipher.Ciphertext.Hex())
	fmt.Println(refCipher.Tag.Hex())

	_, refErrCheck := enchelper.AesDecrypt(&kiv, nil, refCipher)
	assert.Nil(t, refErrCheck)

	fmt.Println("----------------------------------------------------")

	//aad := kvdemo.MakeAADFromString("aad-string")
	authCipherText := msg.Msg.asAuthenticatedCiphertext()

	fmt.Println(authCipherText.Ciphertext.Hex())
	fmt.Println(authCipherText.Tag.Hex())
	//fmt.Println(aad.Hex())

	decrVal, err := enchelper.AesDecrypt(&kiv, nil, &authCipherText)
	assert.Nil(t, err)
	assert.NotNil(t, decrVal)

	fmt.Println(decrVal.ToStringPlaintext().Value)
}
