package com.trillion.tikitaka.user;

import com.trillion.tikitaka.user.dto.PasswordChangeRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordChangeRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testValidPassword() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setNewPassword("Password123!");

        Set violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidPassword() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setNewPassword("short");

        Set violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
