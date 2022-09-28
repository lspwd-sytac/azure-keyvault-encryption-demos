import "mocha";
import {expect} from "chai";
import {CryptographyClient} from "@azure/keyvault-keys";
import {ClientCertificateCredential} from "@azure/identity";
import {KeyVaultWrapper} from "../src/wrapping/KeyVaultWrapper";
import {GCMKeyAndIVFactory} from "../src/encryption/GCMKeyAndIVFactory";
import {AZHelper} from "../src/azure/AZHelper";


let clCrypto: CryptographyClient

before(() => {
    clCrypto = AZHelper.initializeKVCryptographyClient()
})

describe('KeyVault wrapping', () => {
    it('should wrap and unwrap', async () => {
        const objToEncrypt: any = {secretValue: '123455', income: 12355}

        const kiv = GCMKeyAndIVFactory.defaultRandom()
        const kvw = new KeyVaultWrapper(clCrypto, "RSA-OAEP-256")
        const eof = await kvw.encrypt(objToEncrypt, kiv)
        // Uncomment to see the encrypted object.
        console.log(JSON.stringify(eof))
        const decrObject = await kvw.decrypt(eof, GCMKeyAndIVFactory.deserialize)

        expect(decrObject).to.not.be.undefined
        expect(decrObject).to.not.be.null

        // @ts-ignore
        expect(objToEncrypt.secretValue).to.be.eq(decrObject.secretValue)
        // @ts-ignore
        expect(objToEncrypt.income).to.be.eq(decrObject.income)
    }).timeout(15000)
})