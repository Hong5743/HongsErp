package com.hongs.hongs_erp.auth;

import tools.jackson.databind.ObjectMapper;
import com.hongs.hongs_erp.auth.application.port.out.LoginFailPort;
import com.hongs.hongs_erp.auth.application.port.out.RefreshTokenPort;
import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import com.hongs.hongs_erp.employee.application.port.out.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private TokenBlacklistPort tokenBlacklistPort;

    @MockitoBean
    private RefreshTokenPort refreshTokenPort;

    @MockitoBean
    private LoginFailPort loginFailPort;

    private static final String VALID_EMAIL = "test@hongs.com";
    private static final String VALID_PASSWORD = "Password1!";

    @BeforeEach
    void setUp() {
        when(tokenBlacklistPort.isBlacklisted(anyString())).thenReturn(false);
        when(loginFailPort.incrementAndGet(anyLong())).thenReturn(1);
        when(refreshTokenPort.findByUserId(anyLong())).thenReturn(Optional.empty());
    }

    @Test
    void 올바른_자격증명으로_로그인하면_쿠키를_받는다() throws Exception {
        User user = User.create(VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD), "테스터", User.Role.EMPLOYEE);
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", VALID_EMAIL, "password", VALID_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void 잘못된_비밀번호로_로그인하면_401을_반환한다() throws Exception {
        User user = User.create(VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD), "테스터", User.Role.EMPLOYEE);
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", VALID_EMAIL, "password", "WrongPass1!"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 잘못된_도메인_이메일로_로그인하면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "user@other.com", "password", VALID_PASSWORD))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 이메일_형식이_잘못되면_400과_필드별_에러를_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "not-an-email", "password", VALID_PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    void 비밀번호_복잡도_위반시_400과_필드별_에러를_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", VALID_EMAIL, "password", "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"));
    }

    @Test
    void 로그인_5회_실패시_계정이_잠긴다() throws Exception {
        User user = User.create(VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD), "테스터", User.Role.EMPLOYEE);
        userRepository.save(user);

        when(loginFailPort.incrementAndGet(anyLong())).thenReturn(5);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", VALID_EMAIL, "password", "WrongPass1!"))))
                .andExpect(status().isUnauthorized());

        verify(loginFailPort).incrementAndGet(anyLong());
    }

    @Test
    void 인증되지_않은_요청은_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void EMPLOYEE_역할로_관리자_API_접근시_403을_반환한다() throws Exception {
        User user = User.create(VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD), "테스터", User.Role.EMPLOYEE);
        userRepository.save(user);

        // login to get token
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", VALID_EMAIL, "password", VALID_PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = loginResult.getResponse().getCookie("access_token").getValue();

        mockMvc.perform(post("/api/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new jakarta.servlet.http.Cookie("access_token", accessToken))
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
