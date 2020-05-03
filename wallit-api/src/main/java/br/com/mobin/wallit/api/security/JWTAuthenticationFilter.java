package br.com.mobin.wallit.api.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@AllArgsConstructor
public class JWTAuthenticationFilter implements WebFilter {

    private Function<ServerWebExchange,Boolean> configureAdapter;
    private SecurityHelper securityHelper;

    private static final String CONSUMER_IP_HEADER = "X-FORWARDED-FOR";

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        if (this.configureAdapter.apply(serverWebExchange)) {

            List<String> authorizations = serverWebExchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (authorizations != null && !authorizations.isEmpty()) {

                return Mono.fromCallable(() ->
                        authorizations.stream()
                                .filter(Objects::nonNull).findFirst().orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.FORBIDDEN, "User not Authorized" )
                        )
                )
                .flatMap(token ->
                        securityHelper.mapUserByToken( token )
                                .map(authorizedUser ->
                                        authorizedUser.withRemoteAddress(
                                                Optional.ofNullable(serverWebExchange.getRequest().getHeaders().get( CONSUMER_IP_HEADER ))
                                                        .map(headers -> headers.stream().findFirst())
                                                        .orElse(
                                                                Optional.ofNullable(serverWebExchange.getRequest().getRemoteAddress())
                                                                        .map(inetSocketAddress ->
                                                                                inetSocketAddress.getAddress().getHostAddress()
                                                                        )
                                                        )
                                                        .orElse("unknown_address")
                                        )
                                )
                )
                .flatMap(authorizedUser ->
                        webFilterChain.filter( serverWebExchange )
                                .subscriberContext(context ->
                                        context.put(SecurityHelper.AUTHORIZED_USER, authorizedUser)
                                )
                )
                .onErrorMap(throwable -> {
                    if (!(throwable instanceof ResponseStatusException)) {
                        log.error( "User is not Authorized", throwable );
                        return new ResponseStatusException(HttpStatus.FORBIDDEN, "User not Authorized" );
                    }
                    return throwable;
                });
            }

            return Mono.error(
                    new ResponseStatusException(HttpStatus.FORBIDDEN, "User not Authorized" )
            );
        }
        return webFilterChain.filter(serverWebExchange);
    }
}
