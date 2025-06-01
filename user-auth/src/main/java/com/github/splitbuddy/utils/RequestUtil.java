package com.github.splitbuddy.utils;

import lombok.experimental.UtilityClass;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.function.Function;

@UtilityClass
public class RequestUtil {

    private static URI getCurrentRequestedURI() {
        return ServletUriComponentsBuilder.fromCurrentRequestUri()
                .build()
                .toUri();
    }

    public static String requestUriStringFromUri(Function<URI, String> modifier, URI input) {
        return modifier.apply(input);
    }

    public static String getCurrentRequestFullUriString() {
        return requestUriStringFromUri(URI::toString, getCurrentRequestedURI());
    }

    public static String getCurrentRequestUriPath() {
        return requestUriStringFromUri(URI::getPath, getCurrentRequestedURI());
    }
}
