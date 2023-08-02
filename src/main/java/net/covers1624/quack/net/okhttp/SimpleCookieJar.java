/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.okhttp;

import net.covers1624.quack.annotation.Requires;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by covers1624 on 1/8/23.
 */
@Requires ("com.squareup.okhttp3:okhttp")
public class SimpleCookieJar implements CookieJar {

    private final List<Cookie> cookies = new LinkedList<>();

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
        cookies.addAll(list);
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        List<Cookie> ret = new LinkedList<>();
        for (Cookie cookie : cookies) {
            if (cookie.matches(httpUrl)) {
                ret.add(cookie);
            }
        }
        return ret;
    }
}
