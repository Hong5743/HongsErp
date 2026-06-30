package com.hongs.hongs_erp.posheet;

import com.hongs.hongs_erp.auth.application.port.out.RefreshTokenPort;
import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import com.hongs.hongs_erp.employee.application.port.out.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.port.out.FileVersionRepository;
import com.hongs.hongs_erp.posheet.application.port.out.PoFileRepository;
import com.hongs.hongs_erp.posheet.application.port.out.PoFolderRepository;
import com.hongs.hongs_erp.posheet.application.port.out.StoragePort;
import com.hongs.hongs_erp.posheet.domain.FileVersion;
import com.hongs.hongs_erp.posheet.domain.PoFile;
import com.hongs.hongs_erp.posheet.domain.PoFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PoFilePersistenceTest {

    @Autowired PoFileRepository fileRepository;
    @Autowired FileVersionRepository versionRepository;
    @Autowired PoFolderRepository folderRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @MockitoBean TokenBlacklistPort tokenBlacklistPort;
    @MockitoBean RefreshTokenPort refreshTokenPort;
    @MockitoBean StoragePort storagePort;

    private Long adminId;
    private Long folderId;

    @BeforeEach
    void setUp() {
        when(tokenBlacklistPort.isBlacklisted(anyString())).thenReturn(false);
        when(refreshTokenPort.findByUserId(anyLong())).thenReturn(Optional.empty());
        if (!userRepository.existsByEmail("admin@hongs.com")) {
            userRepository.save(User.create("admin@hongs.com",
                    passwordEncoder.encode("Admin1234!"), "관리자", User.Role.ADMIN));
        }
        adminId = userRepository.findByEmail("admin@hongs.com").orElseThrow().getId();
        folderId = folderRepository.save(PoFolder.createRoot("테스트", adminId)).getId();
    }

    @Test
    void 파일_저장_후_조회된다() {
        PoFile saved = fileRepository.save(PoFile.create(folderId, "계약서.pdf", adminId));
        assertThat(fileRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void 버전_저장_후_현재버전으로_조회된다() {
        PoFile file = fileRepository.save(PoFile.create(folderId, "계약서.pdf", adminId));
        versionRepository.save(FileVersion.create(file.getId(), 1, "posheet/1/v1.pdf", 1024L, adminId));
        assertThat(versionRepository.findCurrentByFileId(file.getId())).isPresent()
                .get().extracting(FileVersion::getVersionNumber).isEqualTo(1);
    }

    @Test
    void 버전_교체시_이전_버전이_비활성화된다() {
        PoFile file = fileRepository.save(PoFile.create(folderId, "계약서.pdf", adminId));
        versionRepository.save(FileVersion.create(file.getId(), 1, "key/v1.pdf", 100L, adminId));
        versionRepository.deactivateAllByFileId(file.getId());
        versionRepository.save(FileVersion.create(file.getId(), 2, "key/v2.pdf", 200L, adminId));
        assertThat(versionRepository.findCurrentByFileId(file.getId()))
                .get().extracting(FileVersion::getVersionNumber).isEqualTo(2);
    }
}
