package com.hongs.hongs_erp.global.dto;

import java.util.List;

public record ErrorResponse(List<FieldError> errors) {

    public record FieldError(String field, String message) {}

    public static ErrorResponse of(String field, String message) {
        return new ErrorResponse(List.of(new FieldError(field, message)));
    }

    public static ErrorResponse ofMessage(String message) {
        return new ErrorResponse(List.of(new FieldError("", message)));
    }
}
