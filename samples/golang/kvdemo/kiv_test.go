package kvdemo

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestKivSerializationAndDeserialization(t *testing.T) {
	kiv := NewDefaultRandomKeyAndIV()
	skiv := kiv.serialize()

	restored := KeyAndIV{}
	err := restored.deserialize(skiv)
	assert.Nil(t, err)
}
