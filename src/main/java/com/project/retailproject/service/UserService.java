package com.project.retailproject.service;

import com.project.retailproject.db.UserRepository;
import com.project.retailproject.dto.UserRequestDTO;
import com.project.retailproject.dto.UserResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponseDTO insertUser(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            auditLogService.logFailure("User.CREATE",
                    "Duplicate email: " + dto.getEmail());
            throw new BadRequestException("Email already registered: " + dto.getEmail());
        }
        try {
            User user = mapToEntity(dto);
            UserResponseDTO result = mapToDTO(userRepository.save(user));
            auditLogService.log("User.CREATE_SUCCESS | UserID: " + result.getUserId()
                    + " | Name: " + result.getUserName()
                    + " | Role: " + result.getRole()
                    + " | Email: " + result.getEmail());
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("User.CREATE", ex.getMessage());
            throw ex;
        }
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + id));

        String before = "Name: " + user.getUserName()
                + " | Role: " + user.getRole()
                + " | Phone: " + user.getPhoneNumber();

        try {
            user.setUserName(dto.getUserName());
            user.setRole(dto.getRole());
            user.setPhoneNumber(dto.getPhoneNumber());

            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            UserResponseDTO result = mapToDTO(userRepository.save(user));
            auditLogService.log("User.UPDATE_SUCCESS | UserID: " + id
                    + " | Before: " + before
                    + " | After: Name: " + dto.getUserName()
                    + " | Role: " + dto.getRole()
                    + " | Phone: " + dto.getPhoneNumber());
            return result;
        } catch (Exception ex) {
            auditLogService.logFailure("User.UPDATE", ex.getMessage());
            throw ex;
        }
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + id));
        try {
            userRepository.deleteById(id);
            auditLogService.log("User.DELETE_SUCCESS | UserID: " + id
                    + " | Name: " + user.getUserName()
                    + " | Email: " + user.getEmail()
                    + " | Role: " + user.getRole());
        } catch (Exception ex) {
            auditLogService.logFailure("User.DELETE", ex.getMessage());
            throw ex;
        }
    }

    public UserResponseDTO getUser(Long id) {
        return mapToDTO(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + id)));
    }

    // NEW — used by /api/users/me endpoint
    // Reads current user by email extracted from JWT token
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));
        return mapToDTO(user);
    }

    public List<UserResponseDTO> getUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<UserResponseDTO> getUsersByRole(String role) {
        return userRepository.findByRole(role).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public Page<UserResponseDTO> getAllUserPaginated(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToDTO);
    }

    // --- Mappers ---
    private User mapToEntity(UserRequestDTO dto) {
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setRole(dto.getRole());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return user;
    }

    private UserResponseDTO mapToDTO(User u) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(u.getUserId());
        dto.setUserName(u.getUserName());
        dto.setRole(u.getRole());
        dto.setEmail(u.getEmail());
        dto.setPhoneNumber(u.getPhoneNumber());
        return dto;
    }
}