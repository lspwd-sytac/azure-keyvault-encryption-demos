import { Buffer } from 'node:buffer';

export interface Plaintext {
    asBuffer(): Buffer
}

export class BinaryPlaintext implements Plaintext {
    public readonly value: Buffer

    constructor(value: Buffer) {
        this.value = value;
    }

    asBuffer(): Buffer {
        return this.value;
    }

    asStringPlaintext(enc?: BufferEncoding): StringPlaintext {
        return new StringPlaintext(this.value.toString(enc))
    }
}

export class StringPlaintext implements Plaintext {
    public readonly value: string

    constructor(value: string) {
        this.value = value;
    }


    asBuffer(): Buffer {
        return Buffer.from(this.value)
    }


}