package CoinLogger;

import CoinLogger.api.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailService userDetailService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
                .antMatchers("/login","/member/join","/id/check", "/join")
                .permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin() //인증은 formLogin방식으로 하겠다.
                .loginPage("/login") //로그인 페이지를 /loginForm URL로 하겠다.
                .loginProcessingUrl("/loginAction") //로그인 즉 인증 처리를 하는 URL을 설정합니다.
                .usernameParameter("userId")
                .passwordParameter("password")
                .successHandler(((request, response, authentication) -> {
                    request.getSession().setAttribute("memberID", request.getParameter("userId"));
                    response.sendRedirect("/all/account");

                }))
                .and()
           .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logoutAction"))
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/login");
    }

    //BCrypt 암호화 엔코더 빈 생성
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
