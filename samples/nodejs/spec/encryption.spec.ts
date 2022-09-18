import "mocha";
import {expect} from "chai";
import {GCMKeyAndIVFactory} from "../src/encryption/GCMKeyAndIVFactory";
import {AESGCM} from "../src/encryption/AESGCM";
import {StringPlaintext} from "../src/encryption/BinaryPlaintext";

describe("AES encryption", () => {
    it ('it should correctly encrypt/decrypt a piece of text', () => {
        const kiv = GCMKeyAndIVFactory.defaultRandom()
        const text = "this is a very long and secret text"

        const encr = AESGCM.encrypt(kiv, new StringPlaintext(text))
        const desc = AESGCM.decrypt(kiv, encr)

        expect(desc.toString()).to.be.eq(text)
    })

    it ('it should correctly encrypt/decrypt a piece of text with deserialized KeyAndIV', () => {
        const kiv = GCMKeyAndIVFactory.defaultRandom()
        const text = "this is a very long and secret text"

        const encr = AESGCM.encrypt(kiv, new StringPlaintext(text))

        const skiv = GCMKeyAndIVFactory.serialize(kiv)
        const desc = AESGCM.decrypt(GCMKeyAndIVFactory.deserialize(skiv), encr)

        expect(desc.toString()).to.be.eq(text)
    })
})