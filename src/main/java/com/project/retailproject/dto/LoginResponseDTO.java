package com.project.retailproject.dto;

public class LoginResponseDTO {

    private String token;
    private String email;
    private String role;
    private String userName;
    private Long userId;

    // Getters & Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}