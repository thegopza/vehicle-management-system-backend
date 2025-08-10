package com.sam.vehicle_management_system.exception;

// --- *** ลบ Annotation @ResponseStatus ออกไป *** ---
public class OtConflictException extends RuntimeException {
    public OtConflictException(String message) {
        super(message);
    }
}