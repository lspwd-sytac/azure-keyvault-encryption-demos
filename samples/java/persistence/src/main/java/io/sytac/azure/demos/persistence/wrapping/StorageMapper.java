package io.sytac.azure.demos.persistence.wrapping;

public interface StorageMapper<ILogical, TStorage extends StoredEncryptedObject, TProtected> {

    TStorage mapStorageStruct(ILogical input);
    TProtected mapProtectedStruct(ILogical input);

    Class<? extends TProtected> getProtectedStructClass();

    ILogical restore(TStorage storage, TProtected protectedSection);
}
