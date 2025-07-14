package com.increff.pos.dto;

import com.increff.pos.api.AuthApi;
import com.increff.pos.model.form.LoginForm;
import com.increff.pos.model.form.SignupForm;
import com.increff.pos.model.response.LoginResponse;
import com.increff.pos.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.increff.pos.util.StringUtil.normalize;
import static com.increff.pos.util.StringUtil.toLowerCase;

@Service
public class AuthDto {

    @Autowired
    private AuthApi api;

    public UserResponse signup(SignupForm signupForm) {
        return api.signup(normalize(signupForm.getName()), toLowerCase(signupForm.getEmail()), signupForm.getPassword());
    }

    public LoginResponse login(LoginForm loginForm) {
        UserResponse user = api.login(normalize(loginForm.getEmail()), loginForm.getPassword());
        return new LoginResponse("Login successful", user);
    }
}
