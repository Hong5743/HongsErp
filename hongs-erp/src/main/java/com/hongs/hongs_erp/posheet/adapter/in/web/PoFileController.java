package com.hongs.hongs_erp.posheet.adapter.in.web;

import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.dto.response.FileResponse;
import com.hongs.hongs_erp.posheet.application.dto.response.PresignedUrlResponse;
import com.hongs.hongs_erp.posheet.application.port.in.FileUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class PoFileController {

    private final FileUseCase fileUseCase;

    public PoFileController(FileUseCase fileUseCase) {
        this.fileUseCase = fileUseCase;
    }

    @GetMapping("/api/posheet/folders/{folderId}/files")
    public List<FileResponse> list(@PathVariable Long folderId) {
        return fileUseCase.listFiles(folderId);
    }

    @PostMapping("/api/posheet/folders/{folderId}/files")
    @ResponseStatus(HttpStatus.CREATED)
    public FileResponse upload(@PathVariable Long folderId,
                               @RequestParam("file") MultipartFile multipartFile,
                               Authentication auth) throws IOException {
        return fileUseCase.uploadFile(folderId, multipartFile.getOriginalFilename(),
                multipartFile.getInputStream(), multipartFile.getSize(),
                extractUserId(auth), extractRole(auth));
    }

    @PutMapping("/api/posheet/files/{fileId}")
    public FileResponse replace(@PathVariable Long fileId,
                                @RequestParam("file") MultipartFile multipartFile,
                                Authentication auth) throws IOException {
        return fileUseCase.replaceFile(fileId, multipartFile.getOriginalFilename(),
                multipartFile.getInputStream(), multipartFile.getSize(),
                extractUserId(auth), extractRole(auth));
    }

    @GetMapping("/api/posheet/files/{fileId}/preview")
    public PresignedUrlResponse preview(@PathVariable Long fileId) {
        return fileUseCase.getPreviewUrl(fileId);
    }

    @DeleteMapping("/api/posheet/files/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long fileId, Authentication auth) {
        fileUseCase.deleteFile(fileId, extractUserId(auth), extractRole(auth));
    }

    private Long extractUserId(Authentication auth) {
        return Long.parseLong((String) auth.getPrincipal());
    }

    private User.Role extractRole(Authentication auth) {
        return auth.getAuthorities().stream().findFirst()
                .map(a -> User.Role.valueOf(a.getAuthority().replace("ROLE_", "")))
                .orElse(User.Role.EMPLOYEE);
    }
}
