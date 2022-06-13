import {KeyAndIV} from "./KeyAndIV";
import {SerializedKeyAndIV} from "./SerializedKeyAndIV";
import crypto = require('crypto');

export class KeyAndIVFactory {

    static readonly DEFAULT_AES_KEY_LENGTH = 128;
    static readonly DEFAULT_AES_IV_LENGTH = 96;
    static readonly DEFAULT_AES_TAG_LENGTH = 96;

    static defaultRandom(): KeyAndIV {
        return KeyAndIVFactory.newRandom(KeyAndIVFactory.DEFAULT_AES_KEY_LENGTH/8,
            KeyAndIVFactory.DEFAULT_AES_IV_LENGTH/8,
            KeyAndIVFactory.DEFAULT_AES_TAG_LENGTH)
    }

    static newRandom(keyBytes: number, ivBytes: number, tagLength: number): KeyAndIV {
        return {
            key: crypto.randomBytes(keyBytes),
            IV: crypto.randomBytes(ivBytes),
            cipher: 'aes-128-gcm',
            tagLength
        }
    }

    static serialize(kiv: KeyAndIV): SerializedKeyAndIV {
        return {
            key: kiv.key ? kiv.key.toString('base64') : undefined,
            IV: kiv.IV ? kiv.IV.toString('base64') : undefined,
            cipher: kiv.cipher,
            tagLength: kiv.tagLength
        }
    }

    static deserialize(skiv: SerializedKeyAndIV): KeyAndIV {
        return {
            key: skiv.key ? Buffer.from(skiv.key, 'base64'): undefined,
            IV: skiv.IV ? Buffer.from(skiv.IV, 'base64'): undefined,
            cipher: skiv.cipher,
            tagLength: skiv.tagLength
        }
    }
}