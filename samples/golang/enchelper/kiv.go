package enchelper

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

func (kiv *KeyAndIV) serialize() SerializedKeyAndIV {

	return SerializedKeyAndIV{
		Cipher:    kiv.Cipher,
		Key:       base64.StdEncoding.EncodeToString(kiv.Key),
		IV:        base64.StdEncoding.EncodeToString(kiv.IV),
		TagLength: kiv.tagLength,
	}
}

func (kiv *KeyAndIV) deserialize(kvw SerializedKeyAndIV) error {
	kiv.Cipher = kvw.Cipher
	var err error

	kiv.Key, err = base64.StdEncoding.DecodeString(kvw.Key)
	if err != nil {
		return err
	}

	kiv.IV, err = base64.StdEncoding.DecodeString(kvw.IV)
	if err != nil {
		return err
	}

	kiv.tagLength = kvw.TagLength

	return nil
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
