export class EncryptionKey {
    public readonly value: Buffer

    constructor(value: Buffer) {
        this.value = value;
    }
}