import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
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
        return NimbusJwtDecoder.withJwkSetUri(jwksUrl)
            .build()
            .andThen(jwt -> validateJwt(jwt, resourceId, validClientId));
    }

    private Jwt validateJwt(Jwt jwt, String audience, String clientId) {
        validateAudience(jwt, audience);
        validateClientId(jwt, clientId);
        return jwt;
    }

    private void validateAudience(Jwt jwt, String audience) {
        if (!jwt.getAudience().contains(audience)) {
            throw new IllegalArgumentException("Invalid audience");
        }
    }

    private void validateClientId(Jwt jwt, String clientId) {
        String tokenClientId = jwt.getClaim("azp");
        if (tokenClientId == null) {
            tokenClientId = jwt.getClaim("client_id");
        }
        if (!clientId.equals(tokenClientId)) {
            throw new IllegalArgumentException("Invalid client ID");
        }
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
