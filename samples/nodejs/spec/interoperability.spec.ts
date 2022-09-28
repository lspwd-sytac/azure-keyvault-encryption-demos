import fs = require('fs')
import {GCMKeyAndIVFactory} from "../src/encryption/GCMKeyAndIVFactory";
import {SerializedKeyAndIV} from "../src/encryption/SerializedKeyAndIV";
import {AuthenticatedCiphertext} from "../src/encryption/AuthenticatedCiphertext";
import {Ciphertext} from "../src/encryption/Ciphertext";
import {expect} from "chai";
import {AESGCM} from "../src/encryption/AESGCM";
import {AdditionalAuthenticationData} from "../src/encryption/AdditionalAuthenticationData";
import {BinaryPlaintext} from "../src/encryption/BinaryPlaintext";
import {AuthenticationTag} from "../src/encryption/AuthenticationTag";

class StringValue {
    public value?: string
}

class InterOpKeyAndIV {
    public key?: StringValue
    public iv?: StringValue
}

class InterOpMsg {
    public ciphertext?: StringValue
    public tag?: StringValue
}

class InterOpExchange {
    kiv?: InterOpKeyAndIV
    msg?: InterOpMsg
}

let javaMessage: InterOpExchange
let javaMessageWithAD: InterOpExchange
before(() => {
    let rawJS = fs.readFileSync("../java-output.json")
    javaMessage = JSON.parse(rawJS.toString()) as InterOpExchange

    rawJS = fs.readFileSync("../java-output-with-ad.json")
    javaMessageWithAD = JSON.parse(rawJS.toString()) as InterOpExchange
})

describe('AES decryption of Java-based outputs', () => {
    it('should correctly decrypt value', () => {
        const cipherTextBytes = javaMessage.msg?.ciphertext?.value
        const authTagBytes = javaMessage.msg?.tag?.value

        expect(cipherTextBytes).to.not.be.undefined
        expect(authTagBytes).to.not.be.undefined

        if (cipherTextBytes && authTagBytes) {
            const authenticatedCipherText = new AuthenticatedCiphertext(
                new Ciphertext(Buffer.from(cipherTextBytes, 'base64')),
                new AuthenticationTag(Buffer.from(authTagBytes, 'base64')),
            )

            const serializedKIV: SerializedKeyAndIV = {
                cipherName: 'aes-128-gcm',
                key: javaMessage.kiv?.key?.value,
                iv: javaMessage.kiv?.iv?.value,
                tagLength: authenticatedCipherText.tag.value.length * 8
            }

            const kiv = GCMKeyAndIVFactory.deserialize(serializedKIV)

            const plainBinary = AESGCM.decrypt(kiv, authenticatedCipherText)
            console.log(plainBinary.asStringPlaintext())
        }
    })
    it('should correctly decrypt value with AD', () => {
        const cipherTextBytes = javaMessageWithAD.msg?.ciphertext?.value
        const authTagBytes = javaMessageWithAD.msg?.tag?.value

        expect(cipherTextBytes).to.not.be.undefined
        expect(authTagBytes).to.not.be.undefined

        if (cipherTextBytes && authTagBytes) {
            const authenticatedCipherText = new AuthenticatedCiphertext(
                new Ciphertext(Buffer.from(cipherTextBytes, 'base64')),
                new AuthenticationTag(Buffer.from(authTagBytes, 'base64')),
            )

            const serializedKIV: SerializedKeyAndIV = {
                cipherName: 'aes-128-gcm',
                key: javaMessageWithAD.kiv?.key?.value,
                iv: javaMessageWithAD.kiv?.iv?.value,
                tagLength: authenticatedCipherText.tag.value.length * 8
            }

            const kiv = GCMKeyAndIVFactory.deserialize(serializedKIV)
            const aad = AdditionalAuthenticationData.fromString('aad-string')

            const plainBinary = AESGCM.decryptWithAD(kiv, authenticatedCipherText, aad)
            console.log(plainBinary.asStringPlaintext())
        }
    })
})