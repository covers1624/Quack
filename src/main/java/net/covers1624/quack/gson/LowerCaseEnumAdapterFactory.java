/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import net.covers1624.quack.annotation.Requires;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Gson} {@link TypeAdapterFactory} capable of Serializing/Deserializing Enum constants as lower case.
 * <p>
 * Created by covers1624 on 6/01/19.
 */
@Requires ("com.google.code.gson:gson")
public class LowerCaseEnumAdapterFactory implements TypeAdapterFactory {

    @Nullable
    @Override
    @SuppressWarnings ({ "unchecked", "rawtypes" })
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!type.getRawType().isEnum()) {
            return null;
        }
        return new LowerCaseEnumAdapter(type.getRawType());
    }
}
