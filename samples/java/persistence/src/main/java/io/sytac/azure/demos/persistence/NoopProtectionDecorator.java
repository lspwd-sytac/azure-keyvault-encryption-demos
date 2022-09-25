package io.sytac.azure.demos.persistence;

import java.io.IOException;

public class NoopProtectionDecorator<T> implements ProtectionDecorator<T, T> {

    @Override
    public T apply(T upon, String dbId) throws IOException {
        return upon;
    }

    @Override
    public T restore(T fromProtected, String dbId) throws IOException {
        return fromProtected;
    }
}
