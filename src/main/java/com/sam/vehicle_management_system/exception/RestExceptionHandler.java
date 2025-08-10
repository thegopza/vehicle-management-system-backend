package com.sam.vehicle_management_system.exception;

import com.sam.vehicle_management_system.payload.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(OtConflictException.class)
    protected ResponseEntity<Object> handleOtConflict(OtConflictException ex) {
        // เมื่อเกิด OtConflictException ให้สร้าง MessageResponse พร้อมสถานะ 409 CONFLICT
        MessageResponse messageResponse = new MessageResponse(ex.getMessage());
        return new ResponseEntity<>(messageResponse, HttpStatus.CONFLICT);
    }
}