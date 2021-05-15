/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.gson;

import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.covers1624.quack.annotation.Requires;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A {@link Gson} {@link TypeAdapter} capable of Serializing/Deserializing a Guava {@link HashCode}
 * <p>
 * Created by covers1624 on 6/01/19.
 */
@Requires ("com.google.guava:guava")
@Requires ("com.google.code.gson:gson")
@SuppressWarnings ("UnstableApiUsage")
public class HashCodeAdapter extends TypeAdapter<HashCode> {

    @Override
    public void write(JsonWriter out, @Nullable HashCode value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toString());
    }

    @Nullable
    @Override
    public HashCode read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        //Empty HashCode strings are invalid, there is no other way to represent this data, null it is!
        String s = in.nextString();
        if (s.isEmpty()) {
            return null;
        }
        return HashCode.fromString(s);
    }
}
