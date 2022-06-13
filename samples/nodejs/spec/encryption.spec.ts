import "mocha";
import {expect} from "chai";
import {KeyAndIVFactory} from "../src/encryption/KeyAndIVFactory";
import {AES} from "../src/encryption/AES";

describe("AES encryption", () => {
    it ('it should correctly encrypt/decrypt a piece of text', () => {
        const kiv = KeyAndIVFactory.defaultRandom()
        const text = "this is a very long and secret text"

        const encr = AES.encrypt(kiv, Buffer.from(text))
        const desc = AES.decrypt(kiv, encr)

        expect(desc.toString()).to.be.eq(text)
    })

    it ('it should correctly encrypt/decrypt a piece of text with deserialized KeyAndIV', () => {
        const kiv = KeyAndIVFactory.defaultRandom()
        const text = "this is a very long and secret text"

        const encr = AES.encrypt(kiv, Buffer.from(text))

        const skiv = KeyAndIVFactory.serialize(kiv)
        const desc = AES.decrypt(KeyAndIVFactory.deserialize(skiv), encr)

        expect(desc.toString()).to.be.eq(text)
    })
})