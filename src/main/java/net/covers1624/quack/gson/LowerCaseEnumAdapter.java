/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A {@link Gson} {@link TypeAdapter} for Serializing/Deserializing enum constants
 * in lower case.
 * <p>
 * Created by covers1624 on 19/11/21.
 */
public class LowerCaseEnumAdapter<E extends Enum<E>> extends TypeAdapter<E> {

    private final Map<String, E> table;

    public LowerCaseEnumAdapter(Class<? super E> clazz) {
        Map<String, E> table = new HashMap<>();
        //noinspection unchecked
        for (E e : (E[]) clazz.getEnumConstants()) {
            table.put(e.name().toLowerCase(Locale.ROOT), e);
        }
        this.table = table;
    }

    @Override
    public void write(JsonWriter out, @Nullable E value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.name().toLowerCase(Locale.ROOT));
        }
    }

    @Nullable
    @Override
    public E read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String name = in.nextString();
        if (name == null) return null;
        return table.get(name.toLowerCase(Locale.ROOT));
    }
}
