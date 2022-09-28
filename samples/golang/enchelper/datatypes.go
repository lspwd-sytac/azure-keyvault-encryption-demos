package enchelper

import "encoding/hex"

type Plaintext interface {
	GetSource() *[]byte
}

type Ciphertext struct {
	value []byte
}

func (c *Ciphertext) Hex() string {
	return hex.EncodeToString(c.value)
}

type AuthenticationTag struct {
	value []byte
}

func (c *AuthenticationTag) Hex() string {
	return hex.EncodeToString(c.value)
}

type AuthenticatedCiphertext struct {
	Ciphertext Ciphertext
	Tag        AuthenticationTag
}

type AdditionalAuthenticationData struct {
	value []byte
}

func (c *AdditionalAuthenticationData) Hex() string {
	return hex.EncodeToString(c.value)
}

func (aad *AdditionalAuthenticationData) GetData() []byte {
	return aad.value
}

func MakeCiphertext(str []byte) Ciphertext {
	return Ciphertext{str}
}

func MakeAuthenticationTag(str []byte) AuthenticationTag {
	return AuthenticationTag{str}
}

// MakeAADFrom make an AAD from an arbitrary data set
func MakeAADFrom(text []byte) *AdditionalAuthenticationData {
	copySlice := make([]byte, len(text))
	rv := AdditionalAuthenticationData{
		value: copySlice,
	}

	return &rv
}

// MakeAADFromString Makes an additional authentication data from the text
func MakeAADFromString(text string) *AdditionalAuthenticationData {
	dataSlice := []byte(text)
	rv := AdditionalAuthenticationData{
		value: dataSlice,
	}

	return &rv
}

//--------------------------------------------------------------
// Binary plain-text

type BinaryPlaintext struct {
	value *[]byte
}

func (b *BinaryPlaintext) GetSource() *[]byte {
	return b.value
}

func (b *BinaryPlaintext) ToStringPlaintext() *StringPlaintext {
	rv := StringPlaintext{
		Value: string(*b.value),
	}
	return &rv
}

//---------------------------------------------------------------
// String plain-text

type StringPlaintext struct {
	Value string
}

func (spt *StringPlaintext) GetSource() *[]byte {
	rv := []byte(spt.Value)
	return &rv
}
