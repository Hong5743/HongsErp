package com.hongs.hongs_erp.employee.application.service;

import org.springframework.stereotype.Service;

import com.hongs.hongs_erp.employee.application.dto.request.SignupCommand;
import com.hongs.hongs_erp.employee.application.dto.response.SignupResponse;
import com.hongs.hongs_erp.employee.application.port.input.SignupUseCase;
import com.hongs.hongs_erp.employee.application.port.output.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;

@Service
public class UserService implements SignupUseCase {
    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public SignupResponse signup(final SignupCommand command) {
        // Create a new User entity from the command
        final User user = new User(command.id(), command.password(), command.userName());
        
        // Save the user using the repository
        final User savedUser = userRepository.save(user);
        
        // Return a response with the saved user's details
        return new SignupResponse(savedUser.getId(), savedUser.getUserName());
    }

}
