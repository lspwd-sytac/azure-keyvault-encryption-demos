import {CryptographyClient, KeyWrapAlgorithm} from "@azure/keyvault-keys";
import {EncryptedObjectFragment} from "./EncryptedObjectFragment";
import {AESGCM} from "../encryption/AESGCM";
import {BinaryPlaintext} from "../encryption/BinaryPlaintext";
import {AuthenticationTag} from "../encryption/AuthenticationTag";
import {Ciphertext} from "../encryption/Ciphertext";
import {KeyAndIV} from "../encryption/KeyAndIV";
import {AdditionalAuthenticationData} from "../encryption/AdditionalAuthenticationData";
import {SerializedKeyAndIV} from "../encryption/SerializedKeyAndIV";

type KIVDeserializer = (skiv: SerializedKeyAndIV) => KeyAndIV

export class KeyVaultWrapper {
    private readonly kvClient: CryptographyClient
    private readonly kvAlgorithm: KeyWrapAlgorithm

    constructor(kvClient: CryptographyClient, alg: KeyWrapAlgorithm) {
        this.kvClient = kvClient
        this.kvAlgorithm = alg
    }

    public async encrypt<T>(obj: T, kiv: KeyAndIV, aad?: AdditionalAuthenticationData): Promise<EncryptedObjectFragment> {
        const wrapPromise = this.kvClient.wrapKey(this.kvAlgorithm, Buffer.from(JSON.stringify(kiv.serialize())))

        const plainJSON = Buffer.from(JSON.stringify(obj))
        const plainText = new BinaryPlaintext(plainJSON);
        const encr = aad ? AESGCM.encryptWithAD(kiv, plainText, aad) : AESGCM.encrypt(kiv, plainText)

        const wrappedSKiv = await wrapPromise

        return {
            v: encr.ciphertext.value.toString('base64'),
            t: encr.tag.value.toString('base64'),
            k: Buffer.from(wrappedSKiv.result).toString('base64'),
            d: new Date()
        }
    }

    public async decrypt<T>(f: EncryptedObjectFragment, keyDeserializer: KIVDeserializer, aad?: AdditionalAuthenticationData): Promise<T> {
        if (f.k && f.v && f.t) {
            const unwrappedSKiv = await this.kvClient.unwrapKey(this.kvAlgorithm, Buffer.from(f.k, 'base64'))
            const skiv = JSON.parse(unwrappedSKiv.result.toString()) as SerializedKeyAndIV
            const kiv = keyDeserializer(skiv)

            const authenticatedCiphertext = {
                ciphertext: new Ciphertext(Buffer.from(f.v, 'base64')),
                tag: new AuthenticationTag(Buffer.from(f.t, 'base64'))
            }

            const binaryPlainText = aad ? AESGCM.decryptWithAD(kiv, authenticatedCiphertext, aad) : AESGCM.decrypt(kiv, authenticatedCiphertext)
            return JSON.parse(binaryPlainText.asStringPlaintext().value) as T
        } else {
            throw new Error("Incompletely initialized encrypted object fragment")
        }
    }
}