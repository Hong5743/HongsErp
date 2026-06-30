package com.hongs.hongs_erp.posheet;

import tools.jackson.databind.ObjectMapper;
import com.hongs.hongs_erp.auth.application.port.out.RefreshTokenPort;
import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import com.hongs.hongs_erp.employee.application.port.out.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.port.out.PoFolderRepository;
import com.hongs.hongs_erp.posheet.application.port.out.StoragePort;
import com.hongs.hongs_erp.posheet.domain.PoFolder;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PoFileControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired PoFolderRepository folderRepository;
    @MockitoBean TokenBlacklistPort tokenBlacklistPort;
    @MockitoBean RefreshTokenPort refreshTokenPort;
    @MockitoBean StoragePort storagePort;

    private String adminToken;
    private Long folderId;

    @BeforeEach
    void setUp() throws Exception {
        when(tokenBlacklistPort.isBlacklisted(anyString())).thenReturn(false);
        when(refreshTokenPort.findByUserId(anyLong())).thenReturn(Optional.empty());
        when(storagePort.upload(anyString(), any(), anyLong(), anyString()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(storagePort.generatePresignedUrl(anyString(), any()))
                .thenReturn("https://minio.local/bucket/key?sig=xxx");

        if (!userRepository.existsByEmail("admin@hongs.com")) {
            userRepository.save(User.create("admin@hongs.com",
                    passwordEncoder.encode("Admin1234!"), "관리자", User.Role.ADMIN));
        }
        User admin = userRepository.findByEmail("admin@hongs.com").orElseThrow();
        folderId = folderRepository.save(PoFolder.createRoot("계약서", admin.getId())).getId();

        var res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "admin@hongs.com", "password", "Admin1234!"))))
                .andExpect(status().isOk()).andReturn();
        adminToken = res.getResponse().getCookie("access_token").getValue();
    }

    @Test
    void ADMIN이_파일을_업로드하면_201과_파일정보를_반환한다() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "계약서.pdf",
                "application/pdf", "PDF content".getBytes());
        mockMvc.perform(multipart("/api/posheet/folders/" + folderId + "/files")
                        .file(file)
                        .cookie(new Cookie("access_token", adminToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("계약서.pdf"))
                .andExpect(jsonPath("$.currentVersion").value(1));
    }

    @Test
    void presigned_URL_조회가_200과_URL을_반환한다() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf",
                "application/pdf", "PDF".getBytes());
        String body = mockMvc.perform(multipart("/api/posheet/folders/" + folderId + "/files")
                        .file(file)
                        .cookie(new Cookie("access_token", adminToken)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Long fileId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/api/posheet/files/" + fileId + "/preview")
                        .cookie(new Cookie("access_token", adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(containsString("minio.local")));
    }
}
