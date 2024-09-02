import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class OAuthSecurityConfig {

    private final ApprovalFeatureToggleConfig approvalsFeatureToggleConfig;

    @Value("${server.servlet.context-path:/}")
    private final String contextPath;

    @Value("${okta.oauth2.audience}")
    private final String resourceId;

    @Value("${okta.oauth2.issuer}/v1/keys")
    private final String jwksUrl;

    @Value("${okta.oauth2.client-id}")
    private final String validClientId;

    @Value("${cors.allowed.origin}")
    private final String[] corsAllowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> 
                authorize
                    .requestMatchers(getBypassSecurityUris(contextPath).toArray(new String[0])).permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated()
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer
                    .jwt(jwt -> jwt.decoder(jwtDecoder()))
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();

        // Adding custom validators for audience and client ID
        OAuth2TokenValidator<Jwt> withAudience = new JwtClaimValidator<List<String>>(JwtClaimNames.AUD, aud -> aud.contains(resourceId));
        OAuth2TokenValidator<Jwt> withClientId = new JwtClaimValidator<String>("azp", clientId -> validClientId.equals(clientId));

        OAuth2TokenValidator<Jwt> withBothValidators = new DelegatingOAuth2TokenValidator<>(withAudience, withClientId);
        jwtDecoder.setJwtValidator(withBothValidators);

        return jwtDecoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(corsAllowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> getBypassSecurityUris(String contextPath) {
        // Implement this method as per your requirements
        return List.of();
    }
}
