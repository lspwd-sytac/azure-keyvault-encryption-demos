import {KeyAndIV} from "./KeyAndIV";
import {SerializedKeyAndIV} from "./SerializedKeyAndIV";
import {EncryptionKey} from "./EncryptionKey";
import {InitializationVector} from "./InitializationVector";
import crypto = require('crypto');

class GCMKeyAndIV implements KeyAndIV {
    IV: InitializationVector;
    cipherName: string;
    key: EncryptionKey;
    tagLength: number;


    constructor(cipherName: string, key: EncryptionKey, IV: InitializationVector, tagLength: number) {
        this.IV = IV;
        this.cipherName = cipherName;
        this.key = key;
        this.tagLength = tagLength;
    }

    serialize(): SerializedKeyAndIV {
        return {
            key: this.key.value.toString('base64'),
            iv: this.IV.value.toString('base64'),
            cipherName: this.cipherName,
            tagLength: this.tagLength
        }
    }
}

export class GCMKeyAndIVFactory {

    static readonly DEFAULT_AES_KEY_LENGTH = 128;
    static readonly DEFAULT_AES_IV_LENGTH = 96;
    static readonly DEFAULT_AES_TAG_LENGTH = 128;

    static defaultRandom(): KeyAndIV {
        return GCMKeyAndIVFactory.newRandom(GCMKeyAndIVFactory.DEFAULT_AES_KEY_LENGTH / 8,
            GCMKeyAndIVFactory.DEFAULT_AES_IV_LENGTH / 8,
            GCMKeyAndIVFactory.DEFAULT_AES_TAG_LENGTH)
    }

    static newRandom(keyBytes: number, ivBytes: number, tagLength: number): KeyAndIV {
        return new GCMKeyAndIV(
            'aes-128-gcm',
            new EncryptionKey(crypto.randomBytes(keyBytes)),
            new InitializationVector(crypto.randomBytes(ivBytes)),
            tagLength
        );
    }

    static deserialize(skiv: SerializedKeyAndIV): KeyAndIV {
        if (skiv.cipherName && skiv.key && skiv.iv && skiv.tagLength) {
            return new GCMKeyAndIV(
                skiv.cipherName,
                new EncryptionKey(Buffer.from(skiv.key, 'base64')),
                new InitializationVector(Buffer.from(skiv.iv, 'base64')),
                skiv.tagLength
            );
        } else {
            throw new Error('missing required field to deserialize GCM key and IV')
        }
    }
}