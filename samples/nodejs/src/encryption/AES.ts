import {KeyAndIV} from "./KeyAndIV";
import {CipherCCM, CipherGCMOptions, DecipherGCM} from "crypto";
import crypto = require('crypto')
import {EncryptedBuffer} from "./EncryptedBuffer";

export class AES {
    static encrypt(kiv: KeyAndIV, buf: Buffer): EncryptedBuffer {
        if (kiv.key && kiv.IV && kiv.tagLength && kiv.cipher) {
            const opts: CipherGCMOptions = {
                authTagLength: kiv.tagLength / 8
            }

            const ch = crypto.createCipheriv(kiv.cipher, kiv.key, kiv.IV, opts) as CipherCCM
            return new EncryptedBuffer(Buffer.concat([ch.update(buf), ch.final()]), ch.getAuthTag())
        } else {
            throw new Error('key and IV is not initialized correctly')
        }
    }

    static decrypt(kiv: KeyAndIV, buf: EncryptedBuffer): Buffer {
        if (kiv.key && kiv.IV && kiv.tagLength && kiv.cipher) {
            const opts: CipherGCMOptions = {
                authTagLength: kiv.tagLength / 8
            }

            const ch = crypto.createDecipheriv(kiv.cipher, kiv.key, kiv.IV, opts) as DecipherGCM
            ch.setAuthTag(buf.tag)

            return Buffer.concat([ch.update(buf.buffer), ch.final()])
        } else {
            throw new Error('key and IV is not initialized correctly')
        }
    }
}