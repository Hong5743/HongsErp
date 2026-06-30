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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PoFolderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired PoPermissionRepository permissionRepository;
    @MockitoBean TokenBlacklistPort tokenBlacklistPort;
    @MockitoBean RefreshTokenPort refreshTokenPort;
    @MockitoBean StoragePort storagePort;

    private static final String ADMIN_EMAIL = "admin@hongs.com";
    private static final String EMP_EMAIL   = "emp@hongs.com";
    private static final String PASSWORD    = "Admin1234!";

    @BeforeEach
    void setUp() {
        when(tokenBlacklistPort.isBlacklisted(anyString())).thenReturn(false);
        when(refreshTokenPort.findByUserId(anyLong())).thenReturn(Optional.empty());
        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            userRepository.save(User.create(ADMIN_EMAIL, passwordEncoder.encode(PASSWORD), "관리자", User.Role.ADMIN));
        }
        if (!userRepository.existsByEmail(EMP_EMAIL)) {
            userRepository.save(User.create(EMP_EMAIL, passwordEncoder.encode(PASSWORD), "직원", User.Role.EMPLOYEE));
        }
    }

    private String login(String email) throws Exception {
        var res = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk()).andReturn();
        return res.getResponse().getCookie("access_token").getValue();
    }

    @Test
    void ADMIN이_루트_폴더를_생성하면_201과_경로를_반환한다() throws Exception {
        String token = login(ADMIN_EMAIL);
        mockMvc.perform(post("/api/posheet/folders")
                .cookie(new Cookie("access_token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "계약서"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("/계약서/"));
    }

    @Test
    void 권한_없는_직원이_폴더를_생성하면_403을_반환한다() throws Exception {
        String token = login(EMP_EMAIL);
        mockMvc.perform(post("/api/posheet/folders")
                .cookie(new Cookie("access_token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "계약서"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void 권한_부여된_직원이_폴더를_생성하면_201을_반환한다() throws Exception {
        User emp = userRepository.findByEmail(EMP_EMAIL).orElseThrow();
        User admin = userRepository.findByEmail(ADMIN_EMAIL).orElseThrow();
        permissionRepository.save(PoPermission.grant(emp.getId(), admin.getId()));
        String token = login(EMP_EMAIL);
        mockMvc.perform(post("/api/posheet/folders")
                .cookie(new Cookie("access_token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "공지"))))
                .andExpect(status().isCreated());
    }

    @Test
    void 인증없이_폴더_목록_조회하면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/posheet/folders")).andExpect(status().isUnauthorized());
    }
}
