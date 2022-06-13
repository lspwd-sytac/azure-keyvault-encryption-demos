import {SampleObject} from "../SampleObject";

export type SafeSampleObject = Pick<SampleObject, 'guid' | 'value'>
export type ProtectedSampleObject = Pick<SampleObject, 'secretValue' | 'anotherSecretValue'>
