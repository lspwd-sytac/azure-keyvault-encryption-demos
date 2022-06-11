package io.sytac.azure.demos.persistence;

import java.io.IOException;

public interface ProtectionDecorator<TFrom, TTo> {

    TTo apply(TFrom upon) throws IOException;
    TFrom restore(TTo fromProtected) throws IOException;

}
