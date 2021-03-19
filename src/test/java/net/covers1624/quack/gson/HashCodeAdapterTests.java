/*
 * MIT License
 *
 * Copyright (c) 2018-2021 covers1624
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
