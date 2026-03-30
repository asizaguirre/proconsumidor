package br.gov.mj.senacon.proconsumidor.query.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/actuator/health", "/actuator/info").permitAll() // Para readiness/liveness no gateway

                // [PRODUÇÃO] Reativar obrigatoriedade de JWT para o APIM
                .anyRequest().authenticated()

                // [TESTE LOCAL] Comente a linha acima e use a abaixo para testar a PoC sem Token
                // .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                // Configuração delegada ao application.yml (issuer-uri)
                // Aqui podemos adicionar JwtDecoder customizado para roles Entra ID
            }));

        return http.build();
    }
}
