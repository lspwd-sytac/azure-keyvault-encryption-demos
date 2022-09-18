import {KeyAndIV} from "./KeyAndIV";
import {SerializedKeyAndIV} from "./SerializedKeyAndIV";
import crypto = require('crypto');
import {EncryptionKey} from "./EncryptionKey";
import {InitializationVector} from "./InitializationVector";

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

    static serialize(kiv: KeyAndIV): SerializedKeyAndIV {
        return {
            key: kiv.key ? kiv.key.value.toString('base64') : undefined,
            IV: kiv.IV ? kiv.IV.value.toString('base64') : undefined,
            cipher: kiv.cipherName,
            tagLength: kiv.tagLength
        }
    }

    static deserialize(skiv: SerializedKeyAndIV): KeyAndIV {
        if (skiv.cipher && skiv.key && skiv.IV && skiv.tagLength) {
            return new GCMKeyAndIV(
                skiv.cipher,
                new EncryptionKey(Buffer.from(skiv.key, 'base64')),
                new InitializationVector(Buffer.from(skiv.IV, 'base64')),
                skiv.tagLength
            );
        } else {
            throw new Error('missing required field to deserialize GCM key and IV')
        }
    }
}