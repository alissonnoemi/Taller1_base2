package com.itsqmet.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {
    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws
            Exception{
        http
                .authorizeHttpRequests(auth -> auth
                        //permitir el acceso a las siguientes rutas sin autenticación
                        .requestMatchers("/","/login"
                                ,"/registroNegocio/**"
                                ,"/registroNegocio",
                                "/registroCliente",
                                "/guardarNegocio",
                                "/guardarCliente",
                                "/agendamiento",
                                "/agradecimiento",
                                "/clinicas",
                                "/consultorias",
                                "/contacto",
                                "/demo",
                                "/educacion",
                                "/gestionPersonal",
                                "/pagarPlan",
                                "/precios",
                                "/salonesBelleza",
                                "/talleres",
                                "/css/**","/js/**", "/imagenes/**").permitAll()
                        //autenticacion dependiendo del rol
                        .requestMatchers("/negocios", "/registrarServicios", "/profesionales","/historialCitasNegocio","/editarNegocio","/crearServicio","/crearProfesional","/historialProfesional").hasRole("NEGOCIO")
                        .requestMatchers( "/listaClientes","/guardarCliente", "/cita", "/editarCliente","/guardarCita").hasRole("CLIENTE")
                        .requestMatchers("/listaCita").hasAnyRole("NEGOCIO", "CLIENTE")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                //dsp de iniciar login a donde me va a redirigir dependiendo del rol o lo que tiene permiso
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/postLogin", true)
                )
                //metodo para cerrar sesión
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );
        //retorna un objeto de metodo con toda la configuracion de seguridad
        return http.build();
    } // Bean para el PasswordEncoder ayuda a manejar las contraseñas encriptadas y poq medio se va a encriptar
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
