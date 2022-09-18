import {Ciphertext} from "./Ciphertext";
import {AuthenticationTag} from "./AuthenticationTag";

export class AuthenticatedCiphertext {
    public readonly ciphertext: Ciphertext
    public readonly tag: AuthenticationTag


    constructor(ciphertext: Ciphertext, tag: AuthenticationTag) {
        this.ciphertext = ciphertext;
        this.tag = tag;
    }
}