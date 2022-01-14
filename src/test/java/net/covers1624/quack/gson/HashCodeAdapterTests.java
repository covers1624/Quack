/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.gson;

import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by covers1624 on 19/3/21.
 */
@SuppressWarnings ("UnstableApiUsage")
public class HashCodeAdapterTests {

    @Test
    public void testNull() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(HashCode.class, new HashCodeAdapter())
                .create();
        JsonObject object = new JsonObject();
        object.add("hash", null);

        Data data = gson.fromJson(object, Data.class);
        assertNull(data.hash);
    }

    @Test
    public void testEmpty() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(HashCode.class, new HashCodeAdapter())
                .create();
        JsonObject object = new JsonObject();
        object.addProperty("hash", "");

        Data data = gson.fromJson(object, Data.class);
        assertNull(data.hash);
    }

    @Test
    public void testValue() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(HashCode.class, new HashCodeAdapter())
                .create();
        JsonObject object = new JsonObject();
        object.addProperty("hash", "883d139af5d6bf5b4f940d1611198c096654b45b0c1097a36d256ce209d11dfd");

        Data data = gson.fromJson(object, Data.class);
        HashCode expectedHash = HashCode.fromString("883d139af5d6bf5b4f940d1611198c096654b45b0c1097a36d256ce209d11dfd");
        assertEquals(expectedHash, data.hash);
    }

    public static class Data {

        public HashCode hash;
    }

}
