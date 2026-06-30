package com.hongs.hongs_erp.posheet;

import tools.jackson.databind.ObjectMapper;
import com.hongs.hongs_erp.auth.application.port.out.RefreshTokenPort;
import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import com.hongs.hongs_erp.employee.application.port.out.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.port.out.PoPermissionRepository;
import com.hongs.hongs_erp.posheet.application.port.out.StoragePort;
import com.hongs.hongs_erp.posheet.domain.PoPermission;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PoAdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired PoPermissionRepository permissionRepository;
    @MockitoBean TokenBlacklistPort tokenBlacklistPort;
    @MockitoBean RefreshTokenPort refreshTokenPort;
    @MockitoBean StoragePort storagePort;

    private String adminToken;
    private Long empId;

    @BeforeEach
    void setUp() throws Exception {
        when(tokenBlacklistPort.isBlacklisted(anyString())).thenReturn(false);
        when(refreshTokenPort.findByUserId(anyLong())).thenReturn(Optional.empty());

        if (!userRepository.existsByEmail("admin@hongs.com")) {
            userRepository.save(User.create("admin@hongs.com",
                    passwordEncoder.encode("Admin1234!"), "관리자", User.Role.ADMIN));
        }
        if (!userRepository.existsByEmail("emp@hongs.com")) {
            userRepository.save(User.create("emp@hongs.com",
                    passwordEncoder.encode("Emp1234!"), "직원", User.Role.EMPLOYEE));
        }
        empId = userRepository.findByEmail("emp@hongs.com").orElseThrow().getId();

        var res = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "admin@hongs.com", "password", "Admin1234!"))))
                .andExpect(status().isOk()).andReturn();
        adminToken = res.getResponse().getCookie("access_token").getValue();
    }

    @Test
    void ADMIN이_직원에게_권한을_부여하면_204를_반환한다() throws Exception {
        mockMvc.perform(post("/api/admin/posheet/permissions/" + empId + "/grant")
                        .cookie(new Cookie("access_token", adminToken)))
                .andExpect(status().isNoContent());
        assertThat(permissionRepository.hasActivePermission(empId)).isTrue();
    }

    @Test
    void ADMIN이_권한을_회수하면_204를_반환한다() throws Exception {
        User admin = userRepository.findByEmail("admin@hongs.com").orElseThrow();
        permissionRepository.save(PoPermission.grant(empId, admin.getId()));

        mockMvc.perform(delete("/api/admin/posheet/permissions/" + empId + "/revoke")
                        .cookie(new Cookie("access_token", adminToken)))
                .andExpect(status().isNoContent());
        assertThat(permissionRepository.hasActivePermission(empId)).isFalse();
    }

    @Test
    void 설정_변경이_200을_반환한다() throws Exception {
        mockMvc.perform(put("/api/admin/posheet/settings/trash.retention_days")
                        .cookie(new Cookie("access_token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("value", "14"))))
                .andExpect(status().isOk());
    }

    @Test
    void EMPLOYEE가_admin_API_호출하면_403을_반환한다() throws Exception {
        var empRes = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "emp@hongs.com", "password", "Emp1234!"))))
                .andExpect(status().isOk()).andReturn();
        String empToken = empRes.getResponse().getCookie("access_token").getValue();

        mockMvc.perform(get("/api/admin/posheet/permissions")
                        .cookie(new Cookie("access_token", empToken)))
                .andExpect(status().isForbidden());
    }
}
