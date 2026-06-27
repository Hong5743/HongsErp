package com.hongs.hongs_erp.employee.application.service;

import com.hongs.hongs_erp.employee.application.dto.request.SignupCommand;
import com.hongs.hongs_erp.employee.application.dto.response.EmployeeResponse;
import com.hongs.hongs_erp.employee.application.dto.response.SignupResponse;
import com.hongs.hongs_erp.employee.application.port.in.CreateEmployeeUseCase;
import com.hongs.hongs_erp.employee.application.port.in.SignupUseCase;
import com.hongs.hongs_erp.employee.application.port.in.UnlockEmployeeUseCase;
import com.hongs.hongs_erp.employee.application.port.out.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements SignupUseCase, CreateEmployeeUseCase, UnlockEmployeeUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public SignupResponse signup(SignupCommand command) {
        User saved = saveNewEmployee(command);
        return new SignupResponse(saved.getId(), saved.getEmail(), saved.getName());
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(SignupCommand command) {
        return EmployeeResponse.from(saveNewEmployee(command));
    }

    private User saveNewEmployee(SignupCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }
        String encoded = passwordEncoder.encode(command.password());
        return userRepository.save(User.create(command.email(), encoded, command.name(), command.role()));
    }

    @Override
    @Transactional
    public void unlock(Long employeeId) {
        User user = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다"));
        userRepository.update(user.unlock());
    }
}
