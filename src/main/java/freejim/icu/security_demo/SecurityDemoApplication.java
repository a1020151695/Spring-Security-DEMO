package freejim.icu.security_demo;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

@SpringBootApplication
public class SecurityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityDemoApplication.class, args);
    }


    /**
     *    admin 过滤链
     **/
    @Configuration
    static class AdminSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // 路由
            http.antMatcher("/admin/**")
                    .authorizeRequests()  // 这些请求需要授权
                    .anyRequest().hasAnyAuthority("ADMIN").and()
                    .formLogin()  // 开放登录界面，定制成功登录后的路由
                    .loginPage("/admin/login_backend").permitAll()
                    .defaultSuccessUrl("/admin").and()
                    .logout()  // 开放退出界面，定制退出登录后的路由
                    .logoutUrl("/admin/logout").permitAll()
                    .logoutSuccessUrl("/admin/login_backend?logout");

        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            // AuthenticationManager
            auth.inMemoryAuthentication().withUser("admin").password("{noop}123456").roles("ADMIN").authorities("ADMIN");
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            // static resource
            web.ignoring().antMatchers("/css/**", "/img/**", "/**/*.png");
        }


    }
    /**
     *    user 过滤链
     *    order 不能重复，默认是100，小的优先
     **/
    @Configuration
    @Order(101)
    static class UserSecurityConfig extends WebSecurityConfigurerAdapter{
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/").permitAll()
                    .anyRequest().hasAnyAuthority("USER").and()
                .formLogin()
                    .loginPage("/user/login_frontend").permitAll()
                    .defaultSuccessUrl("/user").and()
                .logout()
                    .logoutUrl("/user/logout")
                    .logoutSuccessUrl("/user/login_frontend?logout").and()
                .exceptionHandling().accessDeniedHandler(UserSecurityConfig::accessDeniedHandle);
        }

        @Autowired
        UserService userService;
        @Autowired
        PasswordEncoder passwordEncoder;
        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userService);
            auth.jdbcAuthentication().passwordEncoder(passwordEncoder);
        }

        private static void accessDeniedHandle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
            request.setAttribute(WebAttributes.ACCESS_DENIED_403,
                    accessDeniedException);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setCharacterEncoding(Charset.defaultCharset().displayName());// 解决中文乱码
            response.addHeader("Content-Type", MediaType.TEXT_HTML_VALUE);
            response.getWriter().write("你的权限不够");
        }
    }

    @Controller
    static class UserLoginController{

        @RequestMapping("/user/login_frontend")
        public String userLogin(){return "login_frontend";}

        @RequestMapping("/user")
        public String userIndex(){return "user";}
    }
    @Controller
    static class AdminLoginController {
        // 后台-登录界面
        @GetMapping("/admin/login_backend")
        public String adminLogin() {
            return "login_backend";
        }

        // 后台-首页
        @GetMapping("/admin")
        public String adminIndex() {
            return "admin";
        }
    }

}
