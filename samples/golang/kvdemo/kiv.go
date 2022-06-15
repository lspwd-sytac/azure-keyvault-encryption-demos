package kvdemo

import (
	"crypto/rand"
	"encoding/base64"
)

const (
	DefaultAesKeyLength = 128
	DefaultAesIvLength  = 96
	DefaultAesTagLength = 96
)

type KeyAndIV struct {
	Cipher    string
	Key       []byte
	IV        []byte
	tagLength int
}

func (kiv KeyAndIV) serialize() SerializedKeyAndIV {

	return SerializedKeyAndIV{
		Cipher:    kiv.Cipher,
		Key:       base64.StdEncoding.EncodeToString(kiv.Key),
		IV:        base64.StdEncoding.EncodeToString(kiv.IV),
		TagLength: kiv.tagLength,
	}
}

func (kiv KeyAndIV) deserialize(kvw SerializedKeyAndIV) {
	kiv.Cipher = kvw.Cipher
	kiv.Key, _ = base64.StdEncoding.DecodeString(kvw.Key)
	kiv.IV, _ = base64.StdEncoding.DecodeString(kvw.IV)
	kiv.tagLength = kvw.TagLength
}

type SerializedKeyAndIV struct {
	Cipher    string `json:"cipher"`
	Key       string `json:"k"`
	IV        string `json:"iv"`
	TagLength int    `json:"t"`
}

func NewDefaultRandomKeyAndIV() *KeyAndIV {
	return NewRandomKeyAndIV(DefaultAesKeyLength/8, DefaultAesIvLength/8, DefaultAesTagLength)
}

func NewRandomKeyAndIV(keyBytes, ivBytes, tagLength int) *KeyAndIV {
	rv := KeyAndIV{
		Cipher:    "aes-128-gcm",
		Key:       make([]byte, keyBytes),
		IV:        make([]byte, ivBytes),
		tagLength: tagLength,
	}

	_, _ = rand.Read(rv.Key)
	_, _ = rand.Read(rv.IV)

	return &rv
}
