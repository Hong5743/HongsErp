package com.hongs.hongs_erp.posheet;

import com.hongs.hongs_erp.auth.application.port.out.RefreshTokenPort;
import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import com.hongs.hongs_erp.employee.application.port.out.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.port.out.PoFolderRepository;
import com.hongs.hongs_erp.posheet.application.port.out.StoragePort;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PoFolderPersistenceTest {

    @Autowired PoFolderRepository folderRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @MockitoBean TokenBlacklistPort tokenBlacklistPort;
    @MockitoBean RefreshTokenPort refreshTokenPort;
    @MockitoBean StoragePort storagePort;

    private Long adminId;

    @BeforeEach
    void setUp() {
        when(tokenBlacklistPort.isBlacklisted(anyString())).thenReturn(false);
        when(refreshTokenPort.findByUserId(anyLong())).thenReturn(Optional.empty());
        // AdminInitializer가 test 컨텍스트에서도 실행되므로 중복 생성 방지
        if (!userRepository.existsByEmail("admin@hongs.com")) {
            userRepository.save(User.create("admin@hongs.com",
                    passwordEncoder.encode("Admin1234!"), "관리자", User.Role.ADMIN));
        }
        adminId = userRepository.findByEmail("admin@hongs.com").orElseThrow().getId();
    }

    @Test
    void 루트_폴더_생성시_path가_슬래시로_감싸진다() {
        PoFolder saved = folderRepository.save(PoFolder.createRoot("계약서", adminId));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPath()).isEqualTo("/계약서/");
    }

    @Test
    void 하위폴더_생성시_path가_부모_경로에_이어진다() {
        PoFolder root = folderRepository.save(PoFolder.createRoot("계약서", adminId));
        PoFolder sub = folderRepository.save(
                PoFolder.createSub("2024", root.getId(), root.getPath(), adminId));
        assertThat(sub.getPath()).isEqualTo("/계약서/2024/");
    }

    @Test
    void pathStartingWith로_하위_폴더_전체를_조회한다() {
        PoFolder root = folderRepository.save(PoFolder.createRoot("계약서", adminId));
        folderRepository.save(PoFolder.createSub("2024", root.getId(), root.getPath(), adminId));
        folderRepository.save(PoFolder.createSub("2023", root.getId(), root.getPath(), adminId));
        List<PoFolder> children = folderRepository.findActiveByPathStartingWith(root.getPath());
        assertThat(children).hasSize(2); // root 자신은 제외
    }
}
