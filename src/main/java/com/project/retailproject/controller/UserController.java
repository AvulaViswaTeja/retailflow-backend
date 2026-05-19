package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.UserRequestDTO;
import com.project.retailproject.dto.UserResponseDTO;
import com.project.retailproject.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> addUser(
            @Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO data = userService.insertUser(dto);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> data = userService.getUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        UserResponseDTO data = userService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO data = userService.updateUser(id, dto);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getUsersByRole(
            @PathVariable String role) {
        List<UserResponseDTO> data = userService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", data));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sorting,
            @RequestParam(defaultValue = "true") boolean asc) {
        Sort sort = asc ? Sort.by(sorting).ascending() : Sort.by(sorting).descending();
        Page<UserResponseDTO> data = userService.getAllUserPaginated(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", data));
    }
}