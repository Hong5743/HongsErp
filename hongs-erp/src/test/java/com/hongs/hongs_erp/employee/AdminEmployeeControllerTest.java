package com.hongs.hongs_erp.employee;

import tools.jackson.databind.ObjectMapper;
import com.hongs.hongs_erp.auth.application.port.out.RefreshTokenPort;
import com.hongs.hongs_erp.auth.application.port.out.TokenBlacklistPort;
import com.hongs.hongs_erp.employee.application.port.out.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;
import jakarta.servlet.http.Cookie;
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

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminEmployeeControllerTest {

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

    private static final String ADMIN_EMAIL = "admin@hongs.com";
    private static final String ADMIN_PASSWORD = "Admin1234!";

    @BeforeEach
    void setUp() {
        when(tokenBlacklistPort.isBlacklisted(anyString())).thenReturn(false);
        when(refreshTokenPort.findByUserId(anyLong())).thenReturn(Optional.empty());
        // AdminInitializer가 test 컨텍스트에서도 실행되므로 중복 생성 방지
        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            userRepository.save(User.create(ADMIN_EMAIL, passwordEncoder.encode(ADMIN_PASSWORD), "관리자", User.Role.ADMIN));
        }
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("access_token").getValue();
    }

    @Test
    void ADMIN이_EDITOR역할과_부서를_지정해_사원을_생성하면_201과_정보를_반환한다() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(post("/api/admin/employees")
                        .cookie(new Cookie("access_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "editor@hongs.com",
                                "password", "Password1!",
                                "name", "에디터",
                                "role", "EDITOR",
                                "department", "기획팀"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("EDITOR"))
                .andExpect(jsonPath("$.department").value("기획팀"));
    }

    @Test
    void ADMIN이_부서_없이_사원을_생성하면_department가_null이다() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(post("/api/admin/employees")
                        .cookie(new Cookie("access_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "emp@hongs.com",
                                "password", "Password1!",
                                "name", "직원"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.department").doesNotExist());
    }

    @Test
    void ADMIN이_사원목록을_조회하면_department_필드가_포함된다() throws Exception {
        userRepository.save(User.create("editor@hongs.com", passwordEncoder.encode("Password1!"),
                "에디터", User.Role.EDITOR, "개발팀"));
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(get("/api/admin/employees")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].department", hasItem("개발팀")));
    }

    @Test
    void 기존_EMPLOYEE_사원이_마이그레이션_후에도_정상_조회된다() throws Exception {
        // department 없이 생성된 기존 EMPLOYEE
        userRepository.save(User.create("emp@hongs.com", passwordEncoder.encode("Password1!"),
                "직원", User.Role.EMPLOYEE));
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(get("/api/admin/employees")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].role", hasItem("EMPLOYEE")));
    }

    @Test
    void EMPLOYEE_역할로_사원목록_조회시_403을_반환한다() throws Exception {
        userRepository.save(User.create("emp@hongs.com", passwordEncoder.encode("Password1!"),
                "직원", User.Role.EMPLOYEE));
        String token = loginAndGetToken("emp@hongs.com", "Password1!");

        mockMvc.perform(get("/api/admin/employees")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 중복된_이메일로_사원_생성시_400을_반환한다() throws Exception {
        userRepository.save(User.create("dup@hongs.com", passwordEncoder.encode("Password1!"),
                "중복", User.Role.EMPLOYEE));
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(post("/api/admin/employees")
                        .cookie(new Cookie("access_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "dup@hongs.com",
                                "password", "Password1!",
                                "name", "중복직원"
                        ))))
                .andExpect(status().isBadRequest());
    }
}
