package com.hongs.hongs_erp.posheet.application.port.in;

import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.dto.response.FileResponse;
import com.hongs.hongs_erp.posheet.application.dto.response.PresignedUrlResponse;

import java.io.InputStream;
import java.util.List;

public interface FileUseCase {
    FileResponse uploadFile(Long folderId, String fileName, InputStream data,
                            long size, Long requesterId, User.Role role);
    FileResponse replaceFile(Long fileId, String fileName, InputStream data,
                             long size, Long requesterId, User.Role role);
    PresignedUrlResponse getPreviewUrl(Long fileId);
    List<FileResponse> listFiles(Long folderId);
    void deleteFile(Long fileId, Long requesterId, User.Role role);
}
