import {CryptographyClient} from "@azure/keyvault-keys";
import {ClientCertificateCredential} from "@azure/identity";
import {Collection, MongoClient} from "mongodb";

export class AZHelper {

    public static async initializeMongoClient(): Promise<MongoClient> {
        const connUrl = process.env['MONGO_URL']
        if (connUrl) {
            const client: MongoClient = new MongoClient(connUrl, {
                ssl: true,
                tls: true,
                authSource: 'demo'
            });
            await client.connect();

            return client
        } else {
            throw new Error("MONGO_URL environment variable is not set")
        }
    }

    public static initializeMongoCollection<T>(cl: MongoClient, collName: string): Collection<T> {
        return cl.db('demo').collection(collName)
    }

    public static initializeKVCryptographyClient(): CryptographyClient {

        const tenantId = process.env['AZ_TENANT_ID']
        const clientId = process.env['AZ_CLIENT_ID']
        const pemPath = process.env['AZ_PEM_PATH']
        const keyId = process.env['AZ_KEY_ID']
        if (!tenantId || !clientId || !pemPath || !keyId) {

            throw new Error("Azure parameters are not initialized correctly")
        } else {
            const creds = new ClientCertificateCredential(tenantId, clientId, pemPath)
            const rv = new CryptographyClient(keyId, creds)
            return rv

        }
    }
}

