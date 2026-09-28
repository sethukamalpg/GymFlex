package com.example.GymFlex.exception;

public class MembershipExpiredException extends RuntimeException {
    public MembershipExpiredException(String message) {
        super(message);
    }
}
