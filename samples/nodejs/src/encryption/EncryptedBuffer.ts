export class EncryptedBuffer {
    public readonly buffer: Buffer
    public readonly tag: Buffer


    constructor(buffer: Buffer, tag: Buffer) {
        this.buffer = buffer;
        this.tag = tag;
    }
}