/*
 * This file is part of Quack and is Licensed under the MIT License.
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
