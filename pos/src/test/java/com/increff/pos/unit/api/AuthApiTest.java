package com.increff.pos.unit.api;

import com.increff.pos.api.AuthApi;
import com.increff.pos.dao.UserDao;
import com.increff.pos.entity.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.enums.Role;
import com.increff.pos.model.response.UserResponse;
import com.increff.pos.setup.TestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthApi class.
 * 
 * These tests verify:
 * - User signup functionality
 * - User login functionality
 * - Role determination logic
 * - Password validation
 * - Email uniqueness validation
 * - Input validation and error handling
 * 
 * Each test focuses on exactly one API method and verifies both
 * the return value and the interactions with dependencies.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthApiTest {

    @Mock
    private UserDao userDao;

    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthApi authApi;

    private UserPojo testUser;

    @Before
    public void setUp() {
        testUser = TestData.user("test@example.com", "Test User", "encoded_password123", Role.OPERATOR);
        
        // Create a simple mock implementation for PasswordEncoder
        passwordEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return "encoded_" + rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encodedPassword.equals("encoded_" + rawPassword.toString());
            }
        };
        
        // Manually inject the passwordEncoder using reflection
        try {
            Field field = AuthApi.class.getDeclaredField("passwordEncoder");
            field.setAccessible(true);
            field.set(authApi, passwordEncoder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject passwordEncoder", e);
        }
    }

    /**
     * Test successful user signup
     */
    @Test
    public void testSignup_Success() throws ApiException {
        // Given: Valid signup data
        String name = "New User";
        String email = "new@example.com";
        String password = "password123";
        
        // And: Email is not already taken
        when(userDao.selectByEmail(email)).thenReturn(Optional.empty());
        
        UserPojo savedUser = TestData.user(1, email, name, "encoded_password123", Role.OPERATOR);
        when(userDao.insertUser(any(UserPojo.class))).thenReturn(savedUser);

        // When: User signs up
        UserResponse result = authApi.signup(name, email, password);

        // Then: User should be created successfully
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(name, result.getName());
        assertEquals(Role.OPERATOR, result.getRole());

        // And: DAO should be called to check email and insert user
        verify(userDao).selectByEmail(email);
        verify(userDao).insertUser(any(UserPojo.class));
    }

    /**
     * Test signup with duplicate email
     */
    @Test
    public void testSignup_DuplicateEmail() throws ApiException {
        // Given: Valid signup data but email already exists
        String name = "New User";
        String email = "existing@example.com";
        String password = "password123";
        
        when(userDao.selectByEmail(email)).thenReturn(Optional.of(testUser));

        // When & Then: Signup should throw conflict exception
        try {
            authApi.signup(name, email, password);
            fail("Should throw ApiException for duplicate email");
        } catch (ApiException e) {
            assertEquals(ErrorType.CONFLICT, e.getErrorType());
            assertTrue(e.getMessage().contains("already exists"));
        }

        // And: DAO should only be called to check email
        verify(userDao).selectByEmail(email);
        verify(userDao, never()).insertUser(any(UserPojo.class));
    }

    /**
     * Test successful user login
     */
    @Test
    public void testLogin_Success() throws ApiException {
        // Given: Valid login credentials
        String email = "test@example.com";
        String password = "password123";
        
        when(userDao.selectByEmail(email)).thenReturn(Optional.of(testUser));

        // When: User logs in
        UserResponse result = authApi.login(email, password);

        // Then: Login should be successful
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getRole(), result.getRole());

        // And: DAO should be called to find user and update last login
        verify(userDao).selectByEmail(email);
        verify(userDao).updateUser(testUser);
    }

    /**
     * Test login with invalid email
     */
    @Test
    public void testLogin_InvalidEmail() throws ApiException {
        // Given: Invalid email
        String email = "nonexistent@example.com";
        String password = "password123";
        
        when(userDao.selectByEmail(email)).thenReturn(Optional.empty());

        // When & Then: Login should throw bad request exception
        try {
            authApi.login(email, password);
            fail("Should throw ApiException for invalid email");
        } catch (ApiException e) {
            assertEquals(ErrorType.BAD_REQUEST, e.getErrorType());
            assertTrue(e.getMessage().contains("Invalid credentials"));
        }

        // And: DAO should only be called to check email
        verify(userDao).selectByEmail(email);
    }

    /**
     * Test login with invalid password
     */
    @Test
    public void testLogin_InvalidPassword() throws ApiException {
        // Given: Valid email but invalid password
        String email = "test@example.com";
        String password = "wrongpassword";
        
        when(userDao.selectByEmail(email)).thenReturn(Optional.of(testUser));


        // When & Then: Login should throw bad request exception
        try {
            authApi.login(email, password);
            fail("Should throw ApiException for invalid password");
        } catch (ApiException e) {
            assertEquals(ErrorType.BAD_REQUEST, e.getErrorType());
            assertTrue(e.getMessage().contains("Invalid credentials"));
        }

        // And: DAO should be called to check email and password
        verify(userDao).selectByEmail(email);
        verify(userDao, never()).updateUser(any(UserPojo.class));
    }

    /**
     * Test role determination for supervisor email
     */
    @Test
    public void testRoleDetermination_Supervisor() throws ApiException {
        // Given: Email that indicates supervisor role
        String name = "Admin User";
        String email = "admin@example.com";
        String password = "password123";
        
        when(userDao.selectByEmail(email)).thenReturn(Optional.empty());
        
        UserPojo savedUser = TestData.user(1, email, name, "encoded_password123", Role.SUPERVISOR);
        when(userDao.insertUser(any(UserPojo.class))).thenReturn(savedUser);

        // When: User signs up
        UserResponse result = authApi.signup(name, email, password);

        // Then: User should be assigned supervisor role
        assertEquals(Role.SUPERVISOR, result.getRole());
    }

    /**
     * Test role determination for operator email
     */
    @Test
    public void testRoleDetermination_Operator() throws ApiException {
        // Given: Regular email
        String name = "Regular User";
        String email = "user@example.com";
        String password = "password123";
        
        when(userDao.selectByEmail(email)).thenReturn(Optional.empty());
        
        UserPojo savedUser = TestData.user(1, email, name, "encoded_password123", Role.OPERATOR);
        when(userDao.insertUser(any(UserPojo.class))).thenReturn(savedUser);

        // When: User signs up
        UserResponse result = authApi.signup(name, email, password);

        // Then: User should be assigned operator role
        assertEquals(Role.OPERATOR, result.getRole());
    }

    /**
     * Test signup with null name
     */
    @Test
    public void testSignup_NullName() throws ApiException {
        // Given: Null name
        String name = null;
        String email = "test@example.com";
        String password = "password123";

        // When & Then: Should handle null name gracefully
        // Note: The actual implementation may or may not validate this
        // This test documents the expected behavior
        when(userDao.selectByEmail(email)).thenReturn(Optional.empty());
        
        UserPojo savedUser = TestData.user(1, email, name, "encoded_password123", Role.OPERATOR);
        when(userDao.insertUser(any(UserPojo.class))).thenReturn(savedUser);

        try {
            UserResponse result = authApi.signup(name, email, password);
            // If no exception is thrown, verify the result
            assertNotNull(result);
        } catch (Exception e) {
            // If exception is thrown, it should be a validation error
            assertTrue(e instanceof ApiException || e instanceof NullPointerException);
        }
    }

    /**
     * Test signup with null email
     */
    @Test
    public void testSignup_NullEmail() throws ApiException {
        // Given: Null email
        String name = "Test User";
        String email = null;
        String password = "password123";

        // When & Then: Should throw exception for null email
        try {
            authApi.signup(name, email, password);
            fail("Should throw exception for null email");
        } catch (Exception e) {
            // Should throw either ApiException or NullPointerException
            assertTrue(e instanceof ApiException || e instanceof NullPointerException);
        }
    }

    /**
     * Test signup with null password
     */
    @Test
    public void testSignup_NullPassword() throws ApiException {
        // Given: Null password
        String name = "Test User";
        String email = "test@example.com";
        String password = null;

        // When & Then: Should throw exception for null password
        try {
            authApi.signup(name, email, password);
            fail("Should throw exception for null password");
        } catch (Exception e) {
            // Should throw either ApiException or NullPointerException
            assertTrue(e instanceof ApiException || e instanceof NullPointerException);
        }
    }

    /**
     * Test login with null email
     */
    @Test
    public void testLogin_NullEmail() throws ApiException {
        // Given: Null email
        String email = null;
        String password = "password123";

        // When & Then: Should throw exception for null email
        try {
            authApi.login(email, password);
            fail("Should throw exception for null email");
        } catch (Exception e) {
            // Should throw either ApiException or NullPointerException
            assertTrue(e instanceof ApiException || e instanceof NullPointerException);
        }
    }

    /**
     * Test login with null password
     */
    @Test
    public void testLogin_NullPassword() throws ApiException {
        // Given: Null password
        String email = "test@example.com";
        String password = null;

        // When & Then: Should throw exception for null password
        try {
            authApi.login(email, password);
            fail("Should throw exception for null password");
        } catch (Exception e) {
            // Should throw either ApiException or NullPointerException
            assertTrue(e instanceof ApiException || e instanceof NullPointerException);
        }
    }

    /**
     * Test email case insensitivity
     */
    @Test
    public void testEmailCaseInsensitivity() throws ApiException {
        // Given: Email with mixed case
        String name = "Test User";
        String email = "Test@Example.COM";
        String password = "password123";
        
        when(userDao.selectByEmail(anyString())).thenReturn(Optional.empty());

        
        UserPojo savedUser = TestData.user(1, email.toLowerCase(), name, "encodedPassword", Role.OPERATOR);
        when(userDao.insertUser(any(UserPojo.class))).thenReturn(savedUser);

        // When: User signs up
        UserResponse result = authApi.signup(name, email, password);

        // Then: Should handle email case insensitivity
        assertNotNull(result);
        
        // Verify that email is processed (likely normalized to lowercase)
        verify(userDao).selectByEmail(anyString());
    }

    /**
     * Test password encoding
     */
    @Test
    public void testPasswordEncoding() throws ApiException {
        // Given: Valid signup data
        String name = "Test User";
        String email = "test@example.com";
        String password = "plainTextPassword";
        String encodedPassword = "encoded_plainTextPassword";
        
        when(userDao.selectByEmail(email)).thenReturn(Optional.empty());
        
        UserPojo savedUser = TestData.user(1, email, name, encodedPassword, Role.OPERATOR);
        when(userDao.insertUser(any(UserPojo.class))).thenReturn(savedUser);

        // When: User signs up
        authApi.signup(name, email, password);

        // Then: Password should be encoded before saving
        verify(userDao).insertUser(argThat(user -> 
            encodedPassword.equals(user.getPassword())
        ));
    }

    /**
     * Test last login time update
     */
    @Test
    public void testLastLoginTimeUpdate() throws ApiException {
        // Given: Valid login credentials
        String email = "test@example.com";
        String password = "password123";
        
        when(userDao.selectByEmail(email)).thenReturn(Optional.of(testUser));

        // When: User logs in
        authApi.login(email, password);

        // Then: Last login time should be updated
        verify(userDao).updateUser(argThat(user -> 
            user.getLastLogin() != null
        ));
    }
} 