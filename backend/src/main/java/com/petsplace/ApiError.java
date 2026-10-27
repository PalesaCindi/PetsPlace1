package com.petsplace;

/** A problem we want to tell the visitor about (shown by the website as a message). */
public class ApiError extends RuntimeException {
    public final int status;

    public ApiError(int status, String message) {
        super(message);
        this.status = status;
    }

    public static ApiError badRequest(String message) {
        return new ApiError(400, message);
    }
}
