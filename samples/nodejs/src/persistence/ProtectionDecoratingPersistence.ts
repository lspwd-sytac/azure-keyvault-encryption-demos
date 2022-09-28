import {IApplicationPersistence} from "./IApplicationPersistence";
import {CosmosDBPersistence} from "./CosmosDBPersistence";
import {WithId} from "mongodb";
import Dict = NodeJS.Dict;
import {KeyVaultWrapper} from "../wrapping/KeyVaultWrapper";
import {GCMKeyAndIVFactory} from "../encryption/GCMKeyAndIVFactory";
import {AdditionalAuthenticationData} from "../encryption/AdditionalAuthenticationData";
import {EncryptedObjectFragment} from "../wrapping/EncryptedObjectFragment";

/**
 * Stored partially encrypted object.
 */
export type StoredPartiallyEncryptedObject = Dict<any> & {
    _eof?: EncryptedObjectFragment
}

export class ProtectionDecoratingPersistence<T> implements IApplicationPersistence<T> {

    private readonly directPersistence: CosmosDBPersistence<StoredPartiallyEncryptedObject>
    private readonly publicKeys: Set<string>
    private readonly wrapper: KeyVaultWrapper


    constructor(wrapper: KeyVaultWrapper,
                directPersistence: CosmosDBPersistence<StoredPartiallyEncryptedObject>,
                ...publicKeys: Array<keyof T>) {
        this.wrapper = wrapper
        this.directPersistence = directPersistence;
        this.publicKeys = new Set();

        // Remeber the keys in a type-safe way.
        publicKeys.forEach(k => {
            if (typeof k === 'string') {
                this.publicKeys.add(k)
            }
        })
    }

    async get(key: Partial<T>): Promise<WithId<T> | null> {
        const obj = await this.directPersistence.get(key)
        if (obj) {
            const rv = {} as any

            for (const objKey in obj) {
                if (objKey === '_eof') {
                    const eof = obj[objKey] as EncryptedObjectFragment
                    const privatePart = await this.wrapper.decrypt<Dict<any>>(eof,
                        (k) => GCMKeyAndIVFactory.deserialize(k),
                        AdditionalAuthenticationData.fromString(obj._id.toHexString()))

                    for (const privatePartKey in privatePart) {
                        rv[privatePartKey] = privatePart[privatePartKey]
                    }
                } else {
                    rv[objKey] = obj[objKey]
                }
            }

            return rv as WithId<T>
        } else {
            return null
        }
    }

    idOf(key: Partial<T>): Promise<string | null> {
        return this.directPersistence.idOf(key as Dict<any>)
    }

    async store(val: T, key: Partial<T>): Promise<string | null> {
        let aad: AdditionalAuthenticationData

        const existingObj = await this.idOf(key)
        if (existingObj) {
            aad = AdditionalAuthenticationData.fromString(existingObj)
        } else {
            let dbId = await this.directPersistence.store(key as Dict<any>, key)
            if (!dbId) dbId = await this.directPersistence.idOf(key)
            if (dbId) aad = AdditionalAuthenticationData.fromString(dbId)
        }
        // @ts-ignore
        if (!aad) throw new Error("Cannot establish object id")

        const publicPart: Dict<any> = {}
        const privatePart = {} as any

        for (const objKey in val) {
            if (this.publicKeys.has(objKey)) publicPart[objKey] = val[objKey]
            else privatePart[objKey] = val[objKey]
        }

        const storedObject: StoredPartiallyEncryptedObject = {
            _eof: await this.wrapper.encrypt(privatePart, GCMKeyAndIVFactory.defaultRandom(), aad)
        }
        for (const key in publicPart) storedObject[key] = publicPart[key]

        return this.directPersistence.store(storedObject, key)
    }
}