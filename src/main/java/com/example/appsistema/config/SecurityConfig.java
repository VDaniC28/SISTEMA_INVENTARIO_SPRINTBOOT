package com.example.appsistema.config;

import com.example.appsistema.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private CustomLogoutHandler customLogoutHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            // Capturar y guardar la empresa seleccionada en la sesión
            String empresaIdStr = request.getParameter("empresaId");
            if (empresaIdStr != null && !empresaIdStr.isEmpty()) {
                HttpSession session = request.getSession();
                
                try {
                    // Convertir a Integer y guardar con ambos nombres para compatibilidad
                    Integer empresaId = Integer.parseInt(empresaIdStr);
                    session.setAttribute("idEmpresaActual", empresaId); // Para el módulo de guías
                    session.setAttribute("empresaId", empresaIdStr); // Para compatibilidad con código existente
                    System.out.println("Empresa guardada en sesión - ID: " + empresaId);
                } catch (NumberFormatException e) {
                    System.err.println("Error al convertir empresaId: " + empresaIdStr);
                    session.setAttribute("empresaId", empresaIdStr);
                }
            }
            
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            String redirectUrl = "/dashboard"; // URL por defecto
            
            if (roles.contains("ROLE_ADMINISTRADOR")) {
                redirectUrl = "/admin/vistaAdmin";
            } else if (roles.contains("ROLE_JEFE_PRODUCCION")) {
                redirectUrl = "/admin/vistaAdmin";
            } else if (roles.contains("ROLE_VENDEDOR")) {
                redirectUrl = "/admin/vistaAdmin";
            } else if (roles.contains("ROLE_SUPERVISOR")) {
                redirectUrl = "/admin/vistaAdmin";
            }
            
            System.out.println("Redirigiendo a: " + redirectUrl);
            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public TenantAuthenticationFilter tenantAuthenticationFilter() {
        return new TenantAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // URLs que no requieren autenticación
                .requestMatchers(
                    "/",
                    "/login",
                    "/registro",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/static/**",
                    "/webjars/**",
                    "/error"
                ).permitAll()
                // URLs que requieren roles específicos
                .requestMatchers("/admin/**").hasAnyRole("ADMINISTRADOR", "JEFE_PRODUCCION", "VENDEDOR", "SUPERVISOR")
                .requestMatchers("/jefe-produccion/**").hasRole("JEFE_PRODUCCION")
                .requestMatchers("/vendedor/**").hasRole("VENDEDOR")
                .requestMatchers("/supervisor/**").hasRole("SUPERVISOR")
                // Cualquier otra URL requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .authenticationProvider(authProvider())
            // Agregar el filtro antes del filtro de autenticación
            .addFilterBefore(tenantAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}