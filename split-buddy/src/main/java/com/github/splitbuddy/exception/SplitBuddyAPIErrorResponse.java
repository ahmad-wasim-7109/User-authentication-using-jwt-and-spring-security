package com.github.splitbuddy.exception;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SplitBuddyAPIErrorResponse {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;

   @Singular
   private List<ApiFieldErrorResponseEntry> fieldErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiFieldErrorResponseEntry  {
        private String field;
        private String message;
    }
}