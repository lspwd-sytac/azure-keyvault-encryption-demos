import {EncryptionKey} from "./EncryptionKey";
import {InitializationVector} from "./InitializationVector";
import {SerializedKeyAndIV} from "./SerializedKeyAndIV";

export interface KeyAndIV {
    cipherName?: string
    key?: EncryptionKey
    IV?: InitializationVector

    tagLength?: number

    serialize(): SerializedKeyAndIV;
}