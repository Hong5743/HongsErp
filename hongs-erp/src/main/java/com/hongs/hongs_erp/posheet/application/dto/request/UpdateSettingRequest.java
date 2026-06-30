package com.hongs.hongs_erp.posheet.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateSettingRequest(@NotBlank String value) {}
