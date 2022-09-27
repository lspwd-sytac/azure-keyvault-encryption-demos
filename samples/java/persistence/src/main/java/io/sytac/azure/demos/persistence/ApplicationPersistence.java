package io.sytac.azure.demos.persistence;

import lombok.NonNull;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.Map;

/**
 * An over-simplified DAO-style interface encapsulating reading and writing of data to the database. The interface,
 * in this example, chooses to directly expose BSON types.
 * <p/>
 * The intention of this interface is to demonstrate that database objects encryption can be fully encapsulated
 * within an interface. So the calling code can be written without worrying about underlying encryption/decryption
 * <p/>
 * There are two implementations of this interface:
 * <ol>
 *     <li>{@link CosmosDBPersistence} providing no specific protection, and </li>
 *     <li>{@link ProtectionDecoratingPersistence} that provides a "decoration" of runtime objects for storing these
 *     in the database.</li>
 * </ol>
 * The decorator pattern is especially useful if your project is planning to use encrypting and signing.In this
 * example, the demonstration is limited to encryption of data using one-time keys that are also stored together
 * with the record.
 *
 * @param <S> concrete schema type stored in this collection
 */
public interface ApplicationPersistence<S> {
    String store(@NonNull S val, @NonNull ObjectIdSupplier<S> idSupplier) throws IOException;

    ObjectId idOf(Document key) throws IOException;

    DBIdentifiedObject<S> get(@NonNull Map<String, Object> reqKey) throws IOException;
}
