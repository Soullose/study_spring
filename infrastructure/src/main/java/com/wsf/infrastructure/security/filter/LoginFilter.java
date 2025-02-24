package com.wsf.infrastructure.security.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@Component
public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    protected final Logger log = LoggerFactory.getLogger(getClass());


    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";

    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";

    public static final String SPRING_SECURITY_FORM_REMEMBER_ME_KEY = "rememberMe";

    private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;

    private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;

    private String rememberMeParameter = SPRING_SECURITY_FORM_REMEMBER_ME_KEY;


    private boolean postOnly = true;

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/api/v1/auth/login",
            "POST");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter() {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
    }

    protected LoginFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        JsonNode jsonNode = objectMapper.readTree(request.getInputStream());
        String username = obtainUsername(jsonNode);
        username = (username != null) ? username.trim() : "";
        String password = obtainPassword(jsonNode);
        password = (password != null) ? password : "";

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                username,
                password
        );
        setDetails(request, usernamePasswordAuthenticationToken);
        return this.getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken);
    }

    @Nullable
    protected String obtainPassword(JsonNode jsonNode) {
        if (jsonNode.has(this.passwordParameter)) {
            return jsonNode.get(this.passwordParameter).asText("");
        }
        return null;
    }

    @Nullable
    protected String obtainUsername(JsonNode jsonNode) {
        if (jsonNode.has(this.usernameParameter)) {
            return jsonNode.get(this.usernameParameter).asText("");
        }
        return null;
    }

    @Nullable
    protected boolean obtainRememberMe(JsonNode jsonNode) {
        if (jsonNode.has(this.rememberMeParameter)) {
            return jsonNode.get(this.rememberMeParameter).asBoolean(false);
        }
        return false;
    }

    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

}