import {CryptographyClient, KnownEncryptionAlgorithms} from "@azure/keyvault-keys";
import {AZHelper} from "../src/azure/AZHelper";
import {Collection, MongoClient} from "mongodb";
import {IApplicationPersistence} from "../src/persistence/IApplicationPersistence";
import {SampleObject} from "../src/SampleObject";
import {CosmosDBPersistence} from "../src/persistence/CosmosDBPersistence";
import {shouldDoReadWrite} from "./directPersistence.spec";
import {ProtectionDecoratingPersistence} from "../src/persistence/ProtectionDecoratingPersistence";
import {KeyVaultWrapper} from "../src/wrapping/KeyVaultWrapper";

let clCrypto: CryptographyClient
let mongoCl: MongoClient
let mongoCollection: Collection
let app: IApplicationPersistence<SampleObject>

before(async () => {
    clCrypto = AZHelper.initializeKVCryptographyClient()
    mongoCl = await AZHelper.initializeMongoClient()
    mongoCollection = AZHelper.initializeMongoCollection(mongoCl, "secure")

    const cosmosDBPersistence = new CosmosDBPersistence(mongoCollection)
    app = new ProtectionDecoratingPersistence(new KeyVaultWrapper(clCrypto, KnownEncryptionAlgorithms.RSAOaep256),
        cosmosDBPersistence,
        'guid', 'value'
        )
})

after( async () => {
    await mongoCl.close()
})

describe("Wrapped Persistence", () => {
    it('should run set and get cycle', async () => {
        await shouldDoReadWrite(app)
    }).timeout(5000)
})