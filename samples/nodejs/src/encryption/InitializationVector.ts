export class InitializationVector {
    public readonly value: Buffer

    constructor(value: Buffer) {
        this.value = value;
    }
}