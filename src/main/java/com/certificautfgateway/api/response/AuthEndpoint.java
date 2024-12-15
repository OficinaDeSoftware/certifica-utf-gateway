package com.certificautfgateway.api.response;

import com.certificautfgateway.api.exception.AuthException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthEndpoint {

    private final WebClient.Builder webClientBuilder;

    public AuthEndpoint( WebClient.Builder webClientBuilder ) {
        this.webClientBuilder = webClientBuilder.baseUrl("http://CERTIFICA-UTF-AUTH/api/auth");
    }

    public Mono<AuthValidateTokenResponse> validateToken( String token ) throws AuthException {
         return webClientBuilder
        .build()
        .get()
        .uri("/validate/token")
        .header( HttpHeaders.AUTHORIZATION, token)
        .retrieve()
        .bodyToMono( AuthValidateTokenResponse.class )
        .onErrorMap( error -> {
            return new AuthException("Unauthorized access to application");
        });
    }
}
