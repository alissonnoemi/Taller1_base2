package com.itsqmet.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ConfiguracionSeguridad {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/", "/login",
                                "/registroNegocio/**", "/registroNegocio", "/registroCliente",
                                "/guardarNegocio", "/guardarCliente", "/agendamiento", "/agradecimiento",
                                "/clinicas", "/consultorias", "/contacto", "/demo", "/educacion",
                                "/gestionPersonal", "/pagarPlan", "/precios", "/salonesBelleza", "/talleres"
                        ).permitAll()
                        .requestMatchers("/css/**", "/js/**", "/imagenes/**").permitAll()

                        .requestMatchers("/**").hasRole("ADMIN")

                        .requestMatchers(
                                "/listaClientes","/guardarCliente","/cita","/editarCliente","/guardarCita"
                        ).hasRole("CLIENTE")

                        .requestMatchers(
                                "/negocios","/registrarServicios","/profesionales",
                                "/historialCitasNegocio","/editarNegocio","/crearServicio","/crearProfesional",
                                "/historialProfesional"
                        ).hasRole("NEGOCIO")

                        .requestMatchers("/listaCita").hasAnyRole("CLIENTE","NEGOCIO")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/postLogin", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
