import {AZHelper} from "../src/azure/AZHelper";
import {Collection, MongoClient} from "mongodb";
import {IApplicationPersistence} from "../src/persistence/IApplicationPersistence";
import {SampleObject} from "../src/SampleObject";
import {expect} from "chai";
import {CosmosDBPersistence} from "../src/persistence/CosmosDBPersistence";

let mongoCl: MongoClient
let mongoCollection: Collection
let app: IApplicationPersistence<SampleObject>

before(async () => {
    mongoCl = await AZHelper.initializeMongoClient()
    mongoCollection = AZHelper.initializeMongoCollection(mongoCl, "insecure")

    app = new CosmosDBPersistence(mongoCollection)
})

after( async () => {
    await mongoCl.close()
})

export async function shouldDoReadWrite(app: IApplicationPersistence<SampleObject>) {
    const objIn: SampleObject = {
        guid: "a-b-c/ts",
        value: "12345/ts",
        secretValue: "This is something you should only see on the console/typescript",
        anotherSecretValue: 45,
    }

    const storedId = await app.store(objIn, {guid: objIn.guid})
    expect(storedId).to.not.be.null

    const read = await app.get({guid: objIn.guid})
    expect(read).to.not.be.null
    expect(objIn.secretValue).to.eq(read?.secretValue)

    console.log(JSON.stringify(read))
}

describe("Direct CosmosDB Persistence", () => {
    it('should run set and get cycle', async () => {
       await shouldDoReadWrite(app)
    }).timeout(5000)
})