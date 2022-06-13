import {IPersistence} from "../IPersistence";
import {CryptographyClient} from "@azure/keyvault-keys";

export class WrappingPersistence implements IPersistence {

    private  kvClient: CryptographyClient
    // private mongoClient: Mongo

    constructor(kvClient: CryptographyClient) {
        this.kvClient = kvClient;
    }

    async get<T>(key: Partial<T>): Promise<T | undefined> {
        return undefined;
    }

    async store<T>(val: T, key: Partial<T>): Promise<void> {
        // return Promise.resolve(undefined);
        return;
    }

}