package com.wsf.infrastructure.security.config;

import com.wsf.infrastructure.security.filter.JwtAuthenticationTokenFilter;
import com.wsf.infrastructure.security.handler.AccessDeniedHandlerImpl;
import com.wsf.infrastructure.security.handler.AuthenticationEntryPointImpl;
import com.wsf.infrastructure.security.service.OpenUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * open
 * SoulLose
 * 2022-04-25 16:14
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final static Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    
    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    
    @Autowired
    private AuthenticationEntryPointImpl authenticationEntryPoint;
    
    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;
    
    @Autowired
    private OpenUserDetailsService userDetailsService;
    
    
    
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
//        return new BCryptPasswordEncoder();
    }
    
    /**
     * Override this method to configure the {@link HttpSecurity}. Typically subclasses
     * should not invoke this method by calling super as it may override their
     * configuration. The default configuration is:
     *
     * <pre>
     * http.authorizeRequests().anyRequest().authenticated().and().formLogin().and().httpBasic();
     * </pre>
     *
     * @param http the {@link HttpSecurity} to modify
     * @throws Exception if an error occurs
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.debug("Using custom configure(HttpSecurity). If subclassed this will potentially override subclass configure(HttpSecurity).");

        http
                //关闭csrf
                .csrf().disable()
                //不通过session获取SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                //登录接口允许匿名访问
                .antMatchers("/open/login").anonymous()
                //除上面外的所有请求全部需要认证
                .anyRequest().authenticated()
//                .and()
//                .formLogin()
//                .failureHandler(AuthenticationFailureListener)
                ;
        
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        //配置异常处理
        http.exceptionHandling()
                //认证失败
                .authenticationEntryPoint(authenticationEntryPoint)
                //授权失败
                .accessDeniedHandler(accessDeniedHandler);
        //允许跨域
        http.cors();
    }
    
    /**
     * Override this method to configure {@link WebSecurity}. For example, if you wish to
     * ignore certain requests.
     *
     * @param web
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
//所需要用到的静态资源，允许访问
        web.ignoring().antMatchers( "/swagger-ui.html",
                "/swagger-ui/*",
                "/swagger-resources/**",
                "/v2/api-docs",
                "/v3/api-docs",
                "/webjars/**",
                "/test/**");
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        super.configure(auth);
//        UserDetailsPasswordService
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
    
    /**
     * Override this method to expose the {@link AuthenticationManager} from
     * {@link #configure(AuthenticationManagerBuilder)} to be exposed as a Bean. For
     * example:
     *
     * <pre>
     * &#064;Bean(name name="myAuthenticationManager")
     * &#064;Override
     * public AuthenticationManager authenticationManagerBean() throws Exception {
     *     return super.authenticationManagerBean();
     * }
     * </pre>
     *
     * @return the {@link AuthenticationManager}
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
