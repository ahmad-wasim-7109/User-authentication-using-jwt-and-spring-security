package com.github.splitbuddy.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SplitUtil {

    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "").substring(32);
    }

    public static Map<String, Object> buildErrorMap(Throwable ex, String customMessage) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("exceptionClass", ex.getClass().getName());
        errorDetails.put("message", StringUtils.isNotBlank(customMessage) ? customMessage : ex.getMessage());

        // Add cause recursively up to 5 levels deep
        Throwable cause = ex.getCause();
        int depth = 0;
        while (cause != null && depth < 5) {
            errorDetails.put("causeLevel" + depth, Map.of(
                    "class", cause.getClass().getName(),
                    "message", cause.getMessage()
            ));
            cause = cause.getCause();
            depth++;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String[] stackTraceLines = sw.toString().split("\\r?\\n");
        StringBuilder shortStackTrace = new StringBuilder();
        for (String stackTraceLine : stackTraceLines) {
            shortStackTrace.append(stackTraceLine).append("\n");
        }
        errorDetails.put("stackTrace", shortStackTrace.toString());

        return errorDetails;
    }
}