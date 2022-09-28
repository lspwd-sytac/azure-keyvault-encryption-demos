export class DBIdentifiedObject<T> {
    public readonly databaseId: string;
    public readonly value: T


    constructor(databaseId: string, value: T) {
        this.databaseId = databaseId;
        this.value = value;
    }

    public mapTo<V>(other: V): DBIdentifiedObject<V> {
        return new DBIdentifiedObject<V>(this.databaseId, other)
    }
}