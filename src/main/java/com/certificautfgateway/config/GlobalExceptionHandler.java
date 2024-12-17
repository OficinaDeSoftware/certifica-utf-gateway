package com.certificautfgateway.config;

import com.certificautfgateway.api.exception.AuthException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.http.HttpStatus;

import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer serverCodecConfigurer ) {
        super( errorAttributes, new WebProperties.Resources(), applicationContext );
        super.setMessageWriters( serverCodecConfigurer.getWriters() );
        super.setMessageReaders( serverCodecConfigurer.getReaders() );
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction( ErrorAttributes errorAttributes ) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse );
    }

    private Mono<ServerResponse> renderErrorResponse( ServerRequest request ) {

        ErrorAttributeOptions options = ErrorAttributeOptions.of( ErrorAttributeOptions.Include.MESSAGE, ErrorAttributeOptions.Include.PATH );
        Map<String, Object> errorPropertiesMap = getErrorAttributes( request, options );

        Throwable throwable = getError( request );
        HttpStatusCode status = determineHttpStatus( throwable );

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorPropertiesMap));
    }

    private HttpStatusCode determineHttpStatus( Throwable throwable ){

        if (throwable instanceof ResponseStatusException) {
            return ((ResponseStatusException) throwable).getStatusCode();
        }

        if ( throwable instanceof AuthException ) {
            return HttpStatus.BAD_REQUEST;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
