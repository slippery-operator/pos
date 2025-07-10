package com.increff.pos.service;

import com.increff.pos.dto.AuthDto;
import com.increff.pos.entity.Role;
import com.increff.pos.model.form.LoginForm;
import com.increff.pos.model.form.SignupForm;
import com.increff.pos.model.response.UserResponse;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.Assert.*;

/**
 * Test class for authentication functionality
 * Tests signup, login, and role assignment logic
 */
public class AuthServiceTest extends AbstractUnitTest {

    @Autowired
    private AuthDto authDto;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testSignupWithSupervisorEmail() {
        // Test signup with supervisor email pattern
        SignupForm signupForm = new SignupForm();
        signupForm.setName("Test Supervisor");
        signupForm.setEmail("supervisor@test.com");
        signupForm.setPassword("password123");

        UserResponse response = authDto.signup(signupForm);

        assertNotNull(response);
        assertEquals("supervisor@test.com", response.getEmail());
        assertEquals("Test Supervisor", response.getName());
        assertEquals(Role.SUPERVISOR, response.getRole());
        assertNotNull(response.getId());
    }

    @Test
    public void testSignupWithOperatorEmail() {
        // Test signup with operator email pattern
        SignupForm signupForm = new SignupForm();
        signupForm.setName("Test Operator");
        signupForm.setEmail("operator@test.com");
        signupForm.setPassword("password123");

        UserResponse response = authDto.signup(signupForm);

        assertNotNull(response);
        assertEquals("operator@test.com", response.getEmail());
        assertEquals("Test Operator", response.getName());
        assertEquals(Role.OPERATOR, response.getRole());
        assertNotNull(response.getId());
    }

    @Test
    public void testSignupWithAdminEmail() {
        // Test signup with admin email pattern
        SignupForm signupForm = new SignupForm();
        signupForm.setName("Test Admin");
        signupForm.setEmail("admin@test.com");
        signupForm.setPassword("password123");

        UserResponse response = authDto.signup(signupForm);

        assertNotNull(response);
        assertEquals("admin@test.com", response.getEmail());
        assertEquals("Test Admin", response.getName());
        assertEquals(Role.SUPERVISOR, response.getRole());
        assertNotNull(response.getId());
    }

    @Test
    public void testSignupWithManagerEmail() {
        // Test signup with manager email pattern
        SignupForm signupForm = new SignupForm();
        signupForm.setName("Test Manager");
        signupForm.setEmail("manager@test.com");
        signupForm.setPassword("password123");

        UserResponse response = authDto.signup(signupForm);

        assertNotNull(response);
        assertEquals("manager@test.com", response.getEmail());
        assertEquals("Test Manager", response.getName());
        assertEquals(Role.SUPERVISOR, response.getRole());
        assertNotNull(response.getId());
    }

    @Test
    public void testSignupWithLeadEmail() {
        // Test signup with lead email pattern
        SignupForm signupForm = new SignupForm();
        signupForm.setName("Test Lead");
        signupForm.setEmail("lead@test.com");
        signupForm.setPassword("password123");

        UserResponse response = authDto.signup(signupForm);

        assertNotNull(response);
        assertEquals("lead@test.com", response.getEmail());
        assertEquals("Test Lead", response.getName());
        assertEquals(Role.SUPERVISOR, response.getRole());
        assertNotNull(response.getId());
    }

    @Test
    public void testSignupWithMixedCaseEmail() {
        // Test signup with mixed case email (should be normalized to lowercase)
        SignupForm signupForm = new SignupForm();
        signupForm.setName("Test User");
        signupForm.setEmail("Test.User@Example.COM");
        signupForm.setPassword("password123");

        UserResponse response = authDto.signup(signupForm);

        assertNotNull(response);
        assertEquals("test.user@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals(Role.OPERATOR, response.getRole());
        assertNotNull(response.getId());
    }

    @Test
    public void testSignupWithTrimmedData() {
        // Test signup with data that needs trimming
        SignupForm signupForm = new SignupForm();
        signupForm.setName("  Test User  ");
        signupForm.setEmail("  test@example.com  ");
        signupForm.setPassword("password123");

        UserResponse response = authDto.signup(signupForm);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals(Role.OPERATOR, response.getRole());
        assertNotNull(response.getId());
    }
} 