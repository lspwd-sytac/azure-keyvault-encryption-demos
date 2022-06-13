/**
 * Interface for the javascript-based persistence layer.
 */
export interface IPersistence {
    store<T>(val: T, key: Partial<T>): Promise<void>
    get<T>(key: Partial<T>): Promise<T | undefined>
}