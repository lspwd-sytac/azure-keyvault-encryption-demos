import {EncryptionKey} from "./EncryptionKey";
import {InitializationVector} from "./InitializationVector";

export interface KeyAndIV {
    cipherName?: string
    key?: EncryptionKey
    IV?: InitializationVector

    tagLength?: number
}