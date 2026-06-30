package com.hongs.hongs_erp.posheet.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateFolderRequest(@NotBlank String name, Long parentId) {}
