import {CryptographyClient, KeyWrapAlgorithm} from "@azure/keyvault-keys";
import {EncryptedObjectFragment} from "./EncryptedObjectFragment";
import {GCMKeyAndIVFactory} from "../encryption/GCMKeyAndIVFactory";
import {AESGCM} from "../encryption/AESGCM";

export class KeyVaultWrapper {
    private readonly kvClient: CryptographyClient
    private readonly kvAlgorithm: KeyWrapAlgorithm

    constructor(kvClient: CryptographyClient, alg: KeyWrapAlgorithm) {
        this.kvClient = kvClient
        this.kvAlgorithm = alg
    }


    public async encrypt<T>(obj: T): Promise<EncryptedObjectFragment> {
        const plainJSON = Buffer.from(JSON.stringify(obj))
        const kiv = GCMKeyAndIVFactory.defaultRandom()
        const encr = AESGCM.encrypt(kiv, plainJSON)

        const skiv = JSON.stringify(GCMKeyAndIVFactory.serialize(kiv))
        const wrappedSKiv = await this.kvClient.wrapKey(this.kvAlgorithm, Buffer.from(skiv))

        return {
            v: encr.buffer.toString('base64'),
            t: encr.tag.toString('base64'),
            k: Buffer.from(wrappedSKiv.result).toString('base64'),
            d: new Date()
        }
    }

    public async decrypt<T>(f: EncryptedObjectFragment): Promise<T> {
        if (f.k && f.v && f.t) {
            const unwrappedSKiv = await this.kvClient.unwrapKey(this.kvAlgorithm, Buffer.from(f.k, 'base64'))
            const skiv = JSON.parse(unwrappedSKiv.result.toString())
            const kiv = GCMKeyAndIVFactory.deserialize(skiv)

            const objText = AESGCM.decrypt(kiv, {
                buffer: Buffer.from(f.v, 'base64'),
                tag: Buffer.from(f.t, 'base64')
            })

            return JSON.parse(objText.toString()) as T
        } else {
            throw new Error("Incompletely initialized encrypted object fragment")
        }
    }
}