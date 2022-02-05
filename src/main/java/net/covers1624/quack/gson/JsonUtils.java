/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.covers1624.quack.annotation.Requires;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Contains utilities for reading/writing and interacting with Json.
 * <p>
 * Many of the parse/write methods will take a {@link Type} object instead of a {@link Class} object.
 * This is to allow deserializing objects which have direct generic parameters through
 * the use of {@link TypeToken}. You can use {@link TypeToken} as follows:
 * <code>private static final Type STRING_LIST_TYPE = new com.google.gson.reflect.TypeToken&lt;List&lt;String&gt;&gt;(){ }.getType();</code>.
 * <p>
 * Created by covers1624 on 11/11/21.
 */
@Requires ("com.google.code.gson:gson")
public class JsonUtils {

    private static final Gson GSON = new Gson();

    //region Deserialize

    /**
     * Deserialize Json from the given {@link Path} as the given {@link Type}.
     *
     * @param gson The {@link Gson} instance to use.
     * @param path The {@link Path} to read from.
     * @param t    The {@link Type} to deserialize from.
     * @return The Object deserialized from Json.
     * @throws JsonParseException Propagated from {@link Gson#fromJson(Reader, Type)},
     *                            thrown when Gson encounters an error deserializing the object.
     * @throws IOException        Thrown when an IO error occurs.
     */
    public static <T> T parse(Gson gson, Path path, Type t) throws IOException, JsonParseException {
        return parse(gson, Files.newInputStream(path), t);
    }

    /**
     * Deserialize Json from the given {@link String} as the given {@link Type}.
     *
     * @param gson The {@link Gson} instance to use.
     * @param str  The {@link String} representing the json to parse.
     * @param t    The {@link Type} to deserialize from.
     * @return The Object deserialized from Json.
     * @throws JsonParseException Propagated from {@link Gson#fromJson(Reader, Type)},
     *                            thrown when Gson encounters an error deserializing the object.
     * @throws IOException        Thrown when an IO error occurs.
     */
    public static <T> T parse(Gson gson, String str, Type t) throws IOException, JsonParseException {
        return parse(gson, new StringReader(str), t);
    }

    /**
     * Deserialize Json from the given {@link InputStream} as the given {@link Type}.
     *
     * @param gson The {@link Gson} instance to use.
     * @param is   The {@link InputStream} to read from.
     * @param t    The {@link Type} to deserialize from.
     * @return The Object deserialized from Json.
     * @throws JsonParseException Propagated from {@link Gson#fromJson(Reader, Type)},
     *                            thrown when Gson encounters an error deserializing the object.
     * @throws IOException        Thrown when an IO error occurs.
     */
    public static <T> T parse(Gson gson, InputStream is, Type t) throws IOException, JsonParseException {
        return parse(gson, new InputStreamReader(is), t);
    }

    /**
     * Deserialize Json from the given {@link Reader} as the given {@link Type}.
     *
     * @param gson   The {@link Gson} instance to use.
     * @param reader The {@link Reader} to read from.
     * @param t      The {@link Type} to deserialize from.
     * @return The Object deserialized from Json.
     * @throws JsonParseException Propagated from {@link Gson#fromJson(Reader, Type)},
     *                            thrown when Gson encounters an error deserializing the object.
     * @throws IOException        Thrown when an IO error occurs.
     */
    public static <T> T parse(Gson gson, Reader reader, Type t) throws IOException, JsonParseException {
        try (Reader r = reader) {
            return gson.fromJson(r, t);
        }
    }
    //endregion

    //region Deserialize raw

    /**
     * Deserialize Json from the given {@link Path} as a raw {@link JsonElement}.
     *
     * @param path The {@link Path} to read from.
     * @return The {@link JsonElement}.
     * @throws JsonParseException Propagated from {@link Gson#fromJson(Reader, Type)},
     *                            thrown when Gson encounters an error deserializing the object.
     * @throws IOException        Thrown when an IO error occurs.
     */
    public static JsonElement parseRaw(Path path) throws IOException, JsonParseException {
        return parseRaw(Files.newInputStream(path));
    }

    /**
     * Deserialize Json from the given {@link String} as a raw {@link JsonElement}.
     *
     * @param str The {@link String} representing the json to parse.
     * @return The {@link JsonElement}.
     * @throws JsonParseException Propagated from {@link Gson#fromJson(Reader, Type)},
     *                            thrown when Gson encounters an error deserializing the object.
     * @throws IOException        Thrown when an IO error occurs.
     */
    public static JsonElement parseRaw(String str) throws IOException, JsonParseException {
        return parseRaw(new StringReader(str));
    }

    /**
     * Deserialize Json from the given {@link InputStream} as a raw {@link JsonElement}.
     *
     * @param is The {@link InputStream} to read from.
     * @return The {@link JsonElement}.
     * @throws JsonParseException Propagated from {@link Gson#fromJson(Reader, Type)},
     *                            thrown when Gson encounters an error deserializing the object.
     * @throws IOException        Thrown when an IO error occurs.
     */
    public static JsonElement parseRaw(InputStream is) throws IOException, JsonParseException {
        return parseRaw(new InputStreamReader(is));
    }

    /**
     * Deserialize Json from the given {@link Reader} as a raw {@link JsonElement}.
     *
     * @param reader The {@link Reader} to read from.
     * @return The {@link JsonElement}.
     * @throws JsonParseException Propagated from {@link Gson#fromJson(Reader, Type)},
     *                            thrown when Gson encounters an error deserializing the object.
     * @throws IOException        Thrown when an IO error occurs.
     */
    public static JsonElement parseRaw(Reader reader) throws IOException, JsonParseException {
        try (Reader r = reader) {
            return GSON.fromJson(r, JsonElement.class);
        }
    }
    //endregion

    //region Serialize.

    /**
     * Serialize the provided Object to Json and write to the given {@link Path}.
     * <p>
     * Use this method when your <code>instance</code> doesn't have any direct generic parameters.
     *
     * @param gson     The {@link Gson} instance to use.
     * @param path     The {@link Path} to write to.
     * @param instance The Object instance to serialize.
     * @throws JsonIOException Propagated from {@link Gson#toJson(Object, Type, Appendable)},
     *                         thrown when Gson encounters an error serializing the instance.
     * @throws IOException     Thrown when an IO error occurs.
     */
    public static void write(Gson gson, Path path, Object instance) throws IOException, JsonIOException {
        write(gson, path, instance, instance.getClass());
    }

    /**
     * Serialize the provided Object to json and write it to the given {@link Path}.
     * <p>
     * Use this method directly if your <code>instance</code> has direct generic parameters.
     *
     * @param gson     The {@link Gson} instance to use.
     * @param path     The {@link Path} to write to.
     * @param instance The Object instance to serialize.
     * @param t        The {@link Type} of the Object instance.
     * @throws JsonIOException Propagated from {@link Gson#toJson(Object, Type, Appendable)},
     *                         thrown when Gson encounters an error serializing the instance.
     * @throws IOException     Thrown when an IO error occurs.
     */
    public static void write(Gson gson, Path path, Object instance, Type t) throws IOException, JsonIOException {
        write(gson, Files.newOutputStream(path), instance, t);
    }

    /**
     * Serialize the provided Object to Json and write to the given {@link OutputStream}.
     * <p>
     * Use this method when your <code>instance</code> doesn't have any direct generic parameters.
     *
     * @param gson     The {@link Gson} instance to use.
     * @param os       The {@link OutputStream} to write to.
     * @param instance The Object instance to serialize.
     * @throws JsonIOException Propagated from {@link Gson#toJson(Object, Type, Appendable)},
     *                         thrown when Gson encounters an error serializing the instance.
     * @throws IOException     Thrown when an IO error occurs.
     */
    public static void write(Gson gson, OutputStream os, Object instance) throws IOException, JsonIOException {
        write(gson, os, instance, instance.getClass());
    }

    /**
     * Serialize the provided Object to Json and write to the given OutputStream.
     * <p>
     * Use this method directly if your <code>instance</code> has direct generic parameters.
     *
     * @param gson     The {@link Gson} instance to use.
     * @param os       The {@link OutputStream} to write to.
     * @param instance The Object instance to write.
     * @param t        The {@link Type} of the Object instance.
     * @throws JsonIOException Propagated from {@link Gson#toJson(Object, Type, Appendable)},
     *                         thrown when Gson encounters an error serializing the instance.
     * @throws IOException     Thrown when an IO error occurs.
     */
    public static void write(Gson gson, OutputStream os, Object instance, Type t) throws IOException, JsonIOException {
        try (Writer writer = new OutputStreamWriter(os)) {
            gson.toJson(instance, t, writer);
        }
    }
    //endregion

    //region Getter helpers.

    /**
     * Try and get a {@link JsonPrimitive} child from the given {@link JsonObject}.
     *
     * @param obj The {@link JsonObject} to get the child from.
     * @param key The name of the child.
     * @return The {@link JsonPrimitive}.
     * @throws JsonParseException If the given child did not exist, or was not a {@link JsonPrimitive}.
     */
    public static JsonPrimitive getAsPrimitive(JsonObject obj, String key) throws JsonParseException {
        JsonElement element = obj.get(key);
        if (element == null) throw new JsonParseException("Did not find child '" + key + "'. '" + obj + "'");
        if (!element.isJsonPrimitive()) throw new JsonParseException("Expected Json Primitive. Got: '" + element + "'");
        return element.getAsJsonPrimitive();
    }

    /**
     * Try and get a {@link JsonPrimitive} child from the given {@link JsonObject}.
     *
     * @param obj The {@link JsonObject} to get the child from.
     * @param key The name of the child.
     * @return The {@link JsonPrimitive}, or <code>null</code> if the child
     * does not exist, or is not a {@link JsonPrimitive}.
     */
    @Nullable
    public static JsonPrimitive getAsPrimitiveOrNull(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element == null) return null;
        if (!element.isJsonPrimitive()) return null;
        return element.getAsJsonPrimitive();
    }

    /**
     * Try and get an <code>int</code> child from the given {@link JsonObject}.
     *
     * @param obj The {@link JsonObject} to get the child from.
     * @param key The name of the child.
     * @return The integer.
     * @throws JsonParseException If the given child did not exist, was not a {@link JsonPrimitive}, or was not a Number.
     */
    public static int getInt(JsonObject obj, String key) throws JsonParseException {
        JsonPrimitive prim = getAsPrimitive(obj, key);
        if (!prim.isNumber()) throw new JsonParseException("Expected Number. Got: '" + prim + "'");
        return prim.getAsInt();
    }

    /**
     * Try and get an <code>int</code> child from the given {@link JsonObject}.
     *
     * @param obj      The {@link JsonObject} to get the child from.
     * @param key      The name of the child.
     * @param default_ The default value to return, in the event that the child does not exist,
     *                 is not a {@link JsonPrimitive}, or is not a Number.
     * @return The integer. Otherwise, <code>default_</code>.
     */
    public static int getInt(JsonObject obj, String key, int default_) {
        JsonPrimitive prim = getAsPrimitiveOrNull(obj, key);
        if (prim == null) return default_;
        if (!prim.isNumber()) return default_;
        return prim.getAsInt();
    }

    /**
     * Try and get a {@link String} child from the given {@link JsonObject}.
     *
     * @param obj The {@link JsonObject} to get the child from.
     * @param key The name of the child.
     * @return The {@link String}.
     * @throws JsonParseException If the given child did not exist, was not a {@link JsonPrimitive}, or was not a {@link String}.
     */
    public static String getString(JsonObject obj, String key) throws JsonParseException {
        JsonPrimitive prim = getAsPrimitive(obj, key);
        if (!prim.isString()) throw new JsonParseException("Expected String. Got: '" + prim + "'");
        return prim.getAsString();
    }

    /**
     * Try and get an {@link String} child from the given {@link JsonObject}.
     *
     * @param obj      The {@link JsonObject} to get the child from.
     * @param key      The name of the child.
     * @param default_ The default value to return, in the event that the child does not exist,
     *                 is not a {@link JsonPrimitive}, or is not a Number.
     * @return The {@link String}. Otherwise, <code>default_</code>.
     */
    @Nullable
    @Contract ("_,_,null->null")
    public static String getString(JsonObject obj, String key, @Nullable String default_) {
        JsonPrimitive prim = getAsPrimitiveOrNull(obj, key);
        if (prim == null) return default_;
        if (!prim.isString()) return default_;
        return prim.getAsString();
    }
    //endregion
}
