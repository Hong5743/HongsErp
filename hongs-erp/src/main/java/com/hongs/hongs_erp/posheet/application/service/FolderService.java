package com.hongs.hongs_erp.posheet.application.service;

import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.dto.response.FolderResponse;
import com.hongs.hongs_erp.posheet.application.port.in.FolderUseCase;
import com.hongs.hongs_erp.posheet.application.port.out.PoFolderRepository;
import com.hongs.hongs_erp.posheet.domain.PoFolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FolderService implements FolderUseCase {

    private final PoFolderRepository folderRepository;
    private final PoSheetPermissionChecker permissionChecker;

    public FolderService(PoFolderRepository folderRepository, PoSheetPermissionChecker permissionChecker) {
        this.folderRepository = folderRepository;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public FolderResponse createFolder(Long requesterId, User.Role role, String name, Long parentId) {
        permissionChecker.assertCanWrite(requesterId, role);
        PoFolder folder;
        if (parentId == null) {
            folder = PoFolder.createRoot(name, requesterId);
        } else {
            PoFolder parent = folderRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다: " + parentId));
            folder = PoFolder.createSub(name, parentId, parent.getPath(), requesterId);
        }
        return FolderResponse.from(folderRepository.save(folder));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> listFolders(Long parentId) {
        return folderRepository.findActiveByParentId(parentId)
                .stream().map(FolderResponse::from).toList();
    }

    @Override
    public void deleteFolder(Long folderId, Long requesterId, User.Role role) {
        permissionChecker.assertCanWrite(requesterId, role);
        PoFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다: " + folderId));
        String batchId = java.util.UUID.randomUUID().toString();
        java.time.LocalDateTime purgeAt = java.time.LocalDateTime.now().plusDays(30);
        folderRepository.softDeleteTree(folder.getPath(), requesterId, batchId, purgeAt);
    }
}
