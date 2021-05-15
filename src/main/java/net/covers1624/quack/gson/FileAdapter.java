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
import net.covers1624.quack.annotation.Requires;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * A {@link Gson} {@link TypeAdapter} capable of Serializing/Deserializing File paths.
 * <p>
 * Created by covers1624 on 6/01/19.
 */
@Requires ("com.google.code.gson:gson")
public class FileAdapter extends TypeAdapter<File> {

    @Override
    public void write(JsonWriter out, @Nullable File value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.getAbsolutePath());
    }

    @Nullable
    @Override
    public File read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return new File(in.nextString());
    }
}
