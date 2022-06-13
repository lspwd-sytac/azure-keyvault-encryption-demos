import "mocha";
import {expect} from "chai";
import {CryptographyClient} from "@azure/keyvault-keys";
import {ClientCertificateCredential} from "@azure/identity";
import {KeyVaultWrapper} from "../src/wrapping/KeyVaultWrapper";

const tenantId = process.env['AZ_TENANT_ID']
const clientId = process.env['AZ_CLIENT_ID']
const pemPath = process.env['AZ_PEM_PATH']
const keyId = process.env['AZ_KEY_ID']

let clCrypto: CryptographyClient

before(() => {
    if (!tenantId || !clientId || !pemPath || !keyId) {
        throw new Error("Azure parameters are not initialized correctly")
    } else {
        const creds = new ClientCertificateCredential(tenantId, clientId, pemPath)
        clCrypto = new CryptographyClient(keyId, creds)
    }
})

describe('KeyVault wrapping', () => {
    it('should wrap and unwrap', async () => {

        const objToEncrypt: any = {secretValue: '123455', income: 12355}

        const kvw = new KeyVaultWrapper(clCrypto, "RSA-OAEP-256")
        const eof = await kvw.encrypt(objToEncrypt)
        // Uncomment to see the encrypted object.
        console.log(JSON.stringify(eof))
        const decrObject = await kvw.decrypt(eof)

        expect(decrObject).to.not.be.undefined
        expect(decrObject).to.not.be.null

        // @ts-ignore
        expect(objToEncrypt.secretValue).to.be.eq(decrObject.secretValue)
        // @ts-ignore
        expect(objToEncrypt.income).to.be.eq(decrObject.income)
    }).timeout(15000)
})