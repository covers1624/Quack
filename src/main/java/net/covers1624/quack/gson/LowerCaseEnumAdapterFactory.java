/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.covers1624.quack.annotation.Requires;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A {@link Gson} {@link TypeAdapterFactory} capable of Serializing/Deserializing Enum constants as lower case.
 * <p>
 * Created by covers1624 on 6/01/19.
 */
@Requires ("com.google.code.gson:gson")
public class LowerCaseEnumAdapterFactory implements TypeAdapterFactory {

    @Nullable
    @Override
    @SuppressWarnings ("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!type.getRawType().isEnum()) {
            return null;
        }
        Map<String, T> lookup = new HashMap<>();
        for (T e : (T[]) type.getRawType().getEnumConstants()) {
            lookup.put(e.toString().toLowerCase(Locale.ROOT), e);
        }
        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, @Nullable T value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    out.value(value.toString().toLowerCase());
                }
            }

            @Nullable
            @Override
            public T read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                String name = in.nextString();
                return name == null ? null : lookup.get(name.toLowerCase(Locale.ROOT));
            }
        };
    }
}
