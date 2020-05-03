package br.com.mobin.wallit.api.security.aspect;

import br.com.mobin.wallit.api.security.SecurityHelper;
import br.com.mobin.wallit.core.security.model.AuthorizedUser;
import br.com.mobin.wallit.core.security.model.HasRole;
import br.com.mobin.wallit.core.security.model.LogicalOperator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@AllArgsConstructor
@Aspect
@Component
public class SecurityAspect {

    @Around(value="@annotation(br.com.mobin.wallit.core.security.model.HasRole)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        HasRole hasRole = method.getAnnotation(HasRole.class);

        Mono<Object> point = (Mono<Object>) joinPoint.proceed();

        return Mono.subscriberContext().<AuthorizedUser>map(context -> context.get(SecurityHelper.AUTHORIZED_USER))
                .map(user -> {
                    boolean found = false;
                    if (LogicalOperator.OR.equals( hasRole.operator() )) {
                        found = Arrays.asList( hasRole.roles() ).parallelStream()
                                .anyMatch(role ->
                                        user.getRoles().parallelStream()
                                                .anyMatch(roleUser -> roleUser.name().equalsIgnoreCase(role) )
                                );
                    } else if ( LogicalOperator.AND.equals( hasRole.operator() ) ) {
                        found = Arrays.asList( hasRole.roles() ).parallelStream()
                                .allMatch(role ->
                                        user.getRoles().parallelStream()
                                                .anyMatch(roleUser -> roleUser.name().equalsIgnoreCase(role) )
                                );
                    }
                    return Tuples.of(found, user);
                })
                .handle((tuple, sink) -> {

                    if (tuple.getT1()) {
                        sink.next(true);
                    } else {
                        log.error("User "+tuple.getT2().getEmail() +" not authorized to execute method: "+method.getName());
                        sink.error(
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN, "User "+tuple.getT2().getEmail() +" not authorized"
                                )
                        );
                    }
                }).then( point );
    }
}
