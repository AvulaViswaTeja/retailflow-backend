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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserResponseDTO insertUser(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already registered: " + dto.getEmail());
        }
        User user = mapToEntity(dto);
        return mapToDTO(userRepository.save(user));
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + id));
        user.setUserName(dto.getUserName());
        user.setRole(dto.getRole());
        user.setPhoneNumber(dto.getPhoneNumber());
        return mapToDTO(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public UserResponseDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + id));
        return mapToDTO(user);
    }

    public List<UserResponseDTO> getUsers() {
        return userRepository.findAll()
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<UserResponseDTO> getUsersByRole(String role) {
        return userRepository.findByRole(role)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
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
        user.setPassword(dto.getPassword()); // will be hashed when JWT is added
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