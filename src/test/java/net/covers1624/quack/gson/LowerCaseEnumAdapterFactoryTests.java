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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by covers1624 on 19/3/21.
 */
public class LowerCaseEnumAdapterFactoryTests {

    @Test
    public void test() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new LowerCaseEnumAdapterFactory())
                .create();
        JsonObject object = new JsonObject();
        object.addProperty("a", "HELLO");
        object.addProperty("b", "world");
        object.addProperty("c", "doesnt_exist");
        object.addProperty("d", "WEIRD_NAME");

        Data data = gson.fromJson(object, Data.class);
        assertEquals(TestEnum.HELLO, data.a);
        assertEquals(TestEnum.WORLD, data.b);
        assertNull(data.c);
        assertEquals(TestEnum.weird_name, data.d);
    }

    @Test
    public void testRoundTrip() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new LowerCaseEnumAdapterFactory())
                .create();

        Data expected = new Data();
        expected.a = TestEnum.weird_name;
        expected.b = TestEnum.WORLD;
        expected.c = null;
        expected.d = TestEnum.HELLO;

        JsonElement json = gson.toJsonTree(expected);

        Data result = gson.fromJson(json, Data.class);
        assertEquals(expected.a, result.a);
        assertEquals(expected.b, result.b);
        assertEquals(expected.c, result.c);
        assertEquals(expected.d, result.d);
    }

    public static class Data {
        public TestEnum a;
        public TestEnum b;
        public TestEnum c;
        public TestEnum d;
    }


    public enum TestEnum {
        HELLO,
        WORLD,
        weird_name,
        ;
    }


}
