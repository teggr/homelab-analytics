package dev.homelab.analytics;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
public class SecurityConfig {
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/tracker.js", "/api/collect", "/login", "/css/**", "/js/**", "/error").permitAll()
        .anyRequest().authenticated())
      .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
      .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll())
      .csrf(csrf -> csrf.ignoringRequestMatchers("/api/collect"))
      .headers(headers -> headers
        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
      .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(PasswordEncoder encoder,
      @Value("${homelab.admin.username}") String username,
      @Value("${homelab.admin.password}") String password) {
    if (username == null || username.isBlank() || password == null || password.isBlank()) {
      password = UUID.randomUUID().toString();
      username = "admin";
      System.out.println("WARNING: HOMELAB_ADMIN_USERNAME/HOMELAB_ADMIN_PASSWORD are not set. "
        + "Generated temporary username=admin password=" + password);
    }
    return new InMemoryUserDetailsManager(
      User.withUsername(username).password(encoder.encode(password)).roles("ADMIN").build());
  }
}
