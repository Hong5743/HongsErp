package com.hongs.hongs_erp.posheet.adapter.in.web;

import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.dto.request.CreateFolderRequest;
import com.hongs.hongs_erp.posheet.application.dto.response.FolderResponse;
import com.hongs.hongs_erp.posheet.application.port.in.FolderUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posheet/folders")
public class PoFolderController {

    private final FolderUseCase folderUseCase;

    public PoFolderController(FolderUseCase folderUseCase) {
        this.folderUseCase = folderUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderResponse create(@Valid @RequestBody CreateFolderRequest req, Authentication auth) {
        Long userId = Long.parseLong((String) auth.getPrincipal());
        User.Role role = auth.getAuthorities().stream().findFirst()
                .map(a -> User.Role.valueOf(a.getAuthority().replace("ROLE_", "")))
                .orElse(User.Role.EMPLOYEE);
        return folderUseCase.createFolder(userId, role, req.name(), req.parentId());
    }

    @GetMapping
    public List<FolderResponse> list(@RequestParam(required = false) Long parentId, Authentication auth) {
        return folderUseCase.listFolders(parentId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        Long userId = Long.parseLong((String) auth.getPrincipal());
        User.Role role = auth.getAuthorities().stream().findFirst()
                .map(a -> User.Role.valueOf(a.getAuthority().replace("ROLE_", "")))
                .orElse(User.Role.EMPLOYEE);
        folderUseCase.deleteFolder(id, userId, role);
    }
}
