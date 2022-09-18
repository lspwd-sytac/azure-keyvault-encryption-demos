import {KeyAndIV} from "./KeyAndIV";
import {CipherGCM, CipherGCMOptions, DecipherGCM} from "crypto";
import crypto = require('crypto')
import {AuthenticatedCiphertext} from "./AuthenticatedCiphertext";
import {Ciphertext} from "./Ciphertext";
import {AuthenticationTag} from "./AuthenticationTag";
import {AdditionalAuthenticationData} from "./AdditionalAuthenticationData";
import {BinaryPlaintext, Plaintext} from "./BinaryPlaintext";

export class AESGCM {
    static encrypt(kiv: KeyAndIV, plainText: Plaintext): AuthenticatedCiphertext {
        return this.encryptWithAD(kiv, plainText, undefined)
    }

    static encryptWithAD(kiv: KeyAndIV, plainText: Plaintext, ad?: AdditionalAuthenticationData): AuthenticatedCiphertext {
        if (kiv.key && kiv.IV && kiv.tagLength && kiv.cipherName) {
            const opts: CipherGCMOptions = {
                authTagLength: kiv.tagLength / 8
            }

            const ch = crypto.createCipheriv(kiv.cipherName, kiv.key.value, kiv.IV.value, opts) as CipherGCM
            if (ad) {
                ch.setAAD(ad.value)
            }

            return new AuthenticatedCiphertext(
                new Ciphertext(Buffer.concat([ch.update(plainText.asBuffer()), ch.final()])),
                new AuthenticationTag(ch.getAuthTag())
            );
        } else {
            throw new Error('key and IV is not initialized correctly')
        }
    }

    static decrypt(kiv: KeyAndIV, ct: AuthenticatedCiphertext): BinaryPlaintext {
        return this.decryptWithAD(kiv, ct, undefined)
    }

    static decryptWithAD(kiv: KeyAndIV, ct: AuthenticatedCiphertext, ad?: AdditionalAuthenticationData): BinaryPlaintext {
        if (kiv.key && kiv.IV && kiv.tagLength && kiv.cipherName) {
            const opts: CipherGCMOptions = {
                authTagLength: kiv.tagLength / 8
            }

            const ch = crypto.createDecipheriv(kiv.cipherName, kiv.key.value, kiv.IV.value, opts) as DecipherGCM
            ch.setAuthTag(ct.tag.value)
            if (ad) {
                ch.setAAD(ad.value)
            }

            return new BinaryPlaintext(Buffer.concat([ch.update(ct.ciphertext.value), ch.final()]))
        } else {
            throw new Error('key and IV is not initialized correctly')
        }
    }
}