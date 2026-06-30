package com.hongs.hongs_erp.posheet;

import com.hongs.hongs_erp.auth.application.port.out.RefreshTokenPort;
import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import com.hongs.hongs_erp.employee.application.port.out.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.port.out.PoPermissionRepository;
import com.hongs.hongs_erp.posheet.application.port.out.PoSettingRepository;
import com.hongs.hongs_erp.posheet.application.port.out.StoragePort;
import com.hongs.hongs_erp.posheet.domain.PoPermission;
import com.hongs.hongs_erp.posheet.domain.PoSetting;
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
class PoPermissionPersistenceTest {

    @Autowired PoPermissionRepository permissionRepository;
    @Autowired PoSettingRepository settingRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @MockitoBean TokenBlacklistPort tokenBlacklistPort;
    @MockitoBean RefreshTokenPort refreshTokenPort;
    @MockitoBean StoragePort storagePort;

    private Long adminId;
    private Long employeeId;

    @BeforeEach
    void setUp() {
        when(tokenBlacklistPort.isBlacklisted(anyString())).thenReturn(false);
        when(refreshTokenPort.findByUserId(anyLong())).thenReturn(Optional.empty());
        if (!userRepository.existsByEmail("admin@hongs.com")) {
            userRepository.save(User.create("admin@hongs.com",
                    passwordEncoder.encode("Admin1234!"), "관리자", User.Role.ADMIN));
        }
        adminId = userRepository.findByEmail("admin@hongs.com").orElseThrow().getId();
        employeeId = userRepository.save(User.create("emp@hongs.com",
                passwordEncoder.encode("Emp1234!"), "직원", User.Role.EMPLOYEE)).getId();
    }

    @Test
    void 권한_부여_후_hasActivePermission이_true를_반환한다() {
        permissionRepository.save(PoPermission.grant(employeeId, adminId));
        assertThat(permissionRepository.hasActivePermission(employeeId)).isTrue();
    }

    @Test
    void 권한_회수_후_hasActivePermission이_false를_반환한다() {
        PoPermission p = permissionRepository.save(PoPermission.grant(employeeId, adminId));
        permissionRepository.revoke(p.getId(), adminId);
        assertThat(permissionRepository.hasActivePermission(employeeId)).isFalse();
    }

    @Test
    void 설정값_저장_후_asInt로_조회된다() {
        settingRepository.save(PoSetting.of(PoSetting.TRASH_RETENTION_DAYS, "30", adminId));
        assertThat(settingRepository.findByKey(PoSetting.TRASH_RETENTION_DAYS))
                .isPresent().get().extracting(PoSetting::asInt).isEqualTo(30);
    }
}
