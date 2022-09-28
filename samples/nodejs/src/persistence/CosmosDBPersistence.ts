import {IApplicationPersistence} from "./IApplicationPersistence";
import {MongoClient, Collection, WithId} from "mongodb"

export class CosmosDBPersistence<T> implements IApplicationPersistence<T> {

    private readonly collection: Collection<T>

    constructor(collection: Collection<T>) {
        this.collection = collection;
    }

    get(key: Partial<T>): Promise<WithId<T> | null> {
        return this.collection.findOne(key)
    }

    async idOf(key: Partial<T>): Promise<string | null> {
        const existingObj = await this.get(key)
        if (existingObj) {
            return existingObj._id.toHexString()
        } else {
            return null
        }
    }

    store(val: T, key: Partial<T>): Promise<string | null> {
        return this.collection.findOneAndReplace(key, val, {upsert: true})
            .then((result) => {
                if (result.value) {
                    return result.value._id.toHexString()
                } else {
                    return null
                }
            })
    }

}