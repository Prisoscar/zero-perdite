package mkle.zeroperdite.security;

import static com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import mkle.zeroperdite.data.Role;
import mkle.zeroperdite.views.login.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain vaadinSecurityFilterChain(HttpSecurity http) throws Exception {

        final String apiRootAuthority = Role.API_ROOT.getAuthority();

        BasicAuthenticationFilter basicAuthenticationFilter = new BasicAuthenticationFilter();
        http.addFilterAfter(basicAuthenticationFilter, org.springframework.security.web.authentication.www.BasicAuthenticationFilter.class);

        http.csrf(csrf -> csrf.ignoringRequestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/api/**"), PathPatternRequestMatcher.withDefaults().matcher(("/io/**"))/*, new AntPathRequestMatcher("/download/**")*/));
//        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/images/*.png", "/*.css").permitAll());
        http.authorizeHttpRequests(
                authorize -> authorize.requestMatchers(
                        PathPatternRequestMatcher.withDefaults().matcher("/images/*.png"),
                        PathPatternRequestMatcher.withDefaults().matcher("/icons/*.png"),
                        PathPatternRequestMatcher.withDefaults().matcher("/icons/*.svg"),
                        PathPatternRequestMatcher.withDefaults().matcher("/fonts/*.ttf"),
                        PathPatternRequestMatcher.withDefaults().matcher("/styles/zero-perdite/**")
                ).permitAll());
        // Icons from the line-awesome addon
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/line-awesome/*/*.svg")).permitAll());
        http.authorizeHttpRequests(
                authorize -> authorize.requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/io/**")).permitAll());//.hasAnyAuthority(Role.USER.getAuthority(), Role.ADMIN.getAuthority()));
        http.authorizeHttpRequests(
                authorize -> authorize.requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/api/**")).hasAuthority(apiRootAuthority));


        // Icons from the line-awesome addon
        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/line-awesome/**").permitAll());

        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
        });

        return http.build();
    }

}
