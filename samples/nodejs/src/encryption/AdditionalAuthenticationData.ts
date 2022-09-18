export class AdditionalAuthenticationData {
    public readonly value: Buffer

    constructor(value: Buffer) {
        this.value = value;
    }

    public static fromString(str: string) {
        return new AdditionalAuthenticationData(Buffer.from(str))
    }
}