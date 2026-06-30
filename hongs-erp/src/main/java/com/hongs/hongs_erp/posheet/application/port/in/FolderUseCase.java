package com.hongs.hongs_erp.posheet.application.port.in;

import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.dto.response.FolderResponse;
import java.util.List;

public interface FolderUseCase {
    FolderResponse createFolder(Long requesterId, User.Role role, String name, Long parentId);
    List<FolderResponse> listFolders(Long parentId);
    void deleteFolder(Long folderId, Long requesterId, User.Role role);
}
