package kvdemo

import (
	"encoding/base64"
	"fmt"
	"github.com/Azure/azure-sdk-for-go/services/keyvault/v7.0/keyvault"
	"github.com/Azure/go-autorest/autorest/azure/auth"
	"github.com/stretchr/testify/assert"
	"os"
	"testing"
)

type SampleObject struct {
	Value    string `json:"v"`
	IntValue int    `json:"i"`
}

func TestBase64Decoding(t *testing.T) {
	str, err := base64.StdEncoding.DecodeString("eyJjaXBoZXIiOiJhZXMtMTI4LWdjbSIsImsiOiIrdXhjTS9LMis1WEo4QWFNNlJVemNnPT0iLCJpdiI6ImpNd0JKUjZhRUVjZTZXQ1oiLCJ0Ijo5Nn0====")
	assert.Nil(t, err)
	fmt.Println(string(str))
}

func TestVaultWrappingAndUnwrapping(t *testing.T) {
	tenantId := os.Getenv("AZ_TENANT_ID")
	clientId := os.Getenv("AZ_CLIENT_ID")
	certPath := os.Getenv("AZ_PFX_PATH")

	kvClient := keyvault.New()
	config := auth.NewClientCertificateConfig(certPath, os.Getenv("AZ_PFX_PWD"), clientId, tenantId)
	config.Resource = "https://vault.azure.net"
	authorizer, err := config.Authorizer()
	assert.Nil(t, err)

	kvClient.Authorizer = authorizer

	kvw := KeyVaultWrapper{
		cryptoClient: &kvClient,
		vaultURL:     os.Getenv("AZ_VAULT_URL"),
		keyName:      os.Getenv("AZ_KEY_NAME"),
		keyVersion:   os.Getenv("AZ_KEY_VERSION"),
	}

	plain := SampleObject{
		Value:    "this is a very secret field you need to see in the console",
		IntValue: 345,
	}

	eof, err := kvw.Wrap(&plain)
	assert.Nil(t, err)

	decr := SampleObject{}
	err = kvw.Unwrap(eof, &decr)
	assert.Nil(t, err)

	assert.EqualValues(t, plain, decr)
}
