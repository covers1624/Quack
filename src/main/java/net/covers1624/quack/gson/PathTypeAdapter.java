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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link Gson} {@link TypeAdapter} capable of Serializing/Deserializing Paths.
 * Only Paths which are owned by the default file system are supported.
 * <p>
 * Created by covers1624 on 14/1/21.
 */
public final class PathTypeAdapter extends TypeAdapter<Path> {

    @Override
    public void write(JsonWriter out, @Nullable Path value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        if (value.getFileSystem().provider() != FileSystems.getDefault().provider()) {
            throw new IOException("Only default FileSystem can be serialized.");
        }
        out.value(value.toAbsolutePath().toString());
    }

    @Nullable
    @Override
    public Path read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return Paths.get(in.nextString());
    }
}
