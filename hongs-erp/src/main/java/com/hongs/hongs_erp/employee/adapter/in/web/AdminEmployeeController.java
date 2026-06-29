package com.hongs.hongs_erp.employee.adapter.in.web;

import com.hongs.hongs_erp.employee.application.dto.request.SignupCommand;
import com.hongs.hongs_erp.employee.application.dto.response.EmployeeResponse;
import com.hongs.hongs_erp.employee.application.port.in.CreateEmployeeUseCase;
import com.hongs.hongs_erp.employee.application.port.in.UnlockEmployeeUseCase;
import com.hongs.hongs_erp.employee.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/employees")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmployeeController {

    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final UnlockEmployeeUseCase unlockEmployeeUseCase;

    public AdminEmployeeController(CreateEmployeeUseCase createEmployeeUseCase, UnlockEmployeeUseCase unlockEmployeeUseCase) {
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.unlockEmployeeUseCase = unlockEmployeeUseCase;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        SignupCommand command = new SignupCommand(
                request.email(), request.password(), request.name(),
                request.role() != null ? request.role() : User.Role.EMPLOYEE
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createEmployeeUseCase.createEmployee(command));
    }

    @PatchMapping("/{id}/unlock")
    public ResponseEntity<Void> unlock(@PathVariable Long id) {
        unlockEmployeeUseCase.unlock(id);
        return ResponseEntity.noContent().build();
    }

    public record CreateEmployeeRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 64) @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,64}$",
                    message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다"
            ) String password,
            @NotBlank String name,
            User.Role role
    ) {}
}
