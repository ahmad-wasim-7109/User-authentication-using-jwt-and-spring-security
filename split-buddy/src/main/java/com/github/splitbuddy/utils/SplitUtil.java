package com.github.splitbuddy.utils;

import java.util.UUID;

public class SplitUtil {

    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "").substring(32);
    }
}
