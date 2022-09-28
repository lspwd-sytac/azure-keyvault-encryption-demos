import {WithId} from "mongodb"

/**
 * Interface for the javascript-based persistence layer.
 */
export interface IApplicationPersistence<T> {
    store(val: T, key: Partial<T>): Promise<string | null>
    idOf(key: Partial<T>): Promise<string | null>
    get(key: Partial<T>): Promise<WithId<T> | null>
}