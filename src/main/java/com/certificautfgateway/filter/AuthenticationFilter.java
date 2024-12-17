package com.certificautfgateway.filter;

import com.certificautfgateway.api.exception.AuthException;
import org.springframework.stereotype.Component;

import com.certificautfgateway.api.response.AuthEndpoint;
import com.certificautfgateway.api.response.AuthValidateTokenResponse;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouterValidator validator;

    private final AuthEndpoint authEndpoint;

    public AuthenticationFilter( RouterValidator validator, AuthEndpoint authEndpoint) {
        super( Config.class );
        this.validator = validator;
        this.authEndpoint = authEndpoint;
    }

    @Override
    public GatewayFilter apply( Config config ) {
        return ( exchange, chain ) -> {

            if ( !validator.isSecured.test( exchange.getRequest() ) ) {
                return chain.filter(exchange);
            }

            if ( !exchange.getRequest().getHeaders().containsKey( HttpHeaders.AUTHORIZATION ) ) {
                throw new AuthException("Missing authorization header");
            }

            String authHeader = exchange.getRequest().getHeaders().get( HttpHeaders.AUTHORIZATION ).get(0);

            if ( authHeader != null && authHeader.startsWith("Bearer ") ) {
                authHeader = authHeader.substring(7 );
            }

            final String token = authHeader;

            final Mono<AuthValidateTokenResponse> authValidateTokenResponseMono = authEndpoint.validateToken(token);

            return authValidateTokenResponseMono.flatMap( response -> chain.filter( exchange ) );
        };
    }

    public static class Config {}
}