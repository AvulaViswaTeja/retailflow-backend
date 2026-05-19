package com.project.retailproject.common;

public class ApiResponse<T> {

    private int statusCode;
    private String message;
    private T data;
    private boolean success;

    // Constructor for success
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.statusCode = 200;
        response.message = message;
        response.data = data;
        response.success = true;
        return response;
    }

    // Constructor for error
    public static <T> ApiResponse<T> error(int statusCode, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.statusCode = statusCode;
        response.message = message;
        response.data = null;
        response.success = false;
        return response;
    }

    // Getters
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public boolean isSuccess() { return success; }
}