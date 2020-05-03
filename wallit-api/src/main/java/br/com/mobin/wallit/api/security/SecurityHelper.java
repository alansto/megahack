package br.com.mobin.wallit.api.security;

import br.com.mobin.wallit.api.config.SecurityConfig;
import br.com.mobin.wallit.api.model.UserModel;
import br.com.mobin.wallit.core.security.model.AuthorizedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

@AllArgsConstructor
@Component
public class SecurityHelper {

    public static final String AUTHORIZATION_TOKEN_PREFIX = "Bearer";
    public static final String AUTHORIZED_USER = "AUTHORIZED_USER_KEY";

    private SecurityConfig securityConfig;
    private ObjectMapper objectMapper;

    public Mono<AuthorizedUser> mapUserByToken(final String token) {

        return Mono.fromCallable(() -> {
            String parsedToken = token.replace(AUTHORIZATION_TOKEN_PREFIX, Strings.EMPTY).strip();

            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            Key signingKey = getSigningKey( signatureAlgorithm );

            String subject = Jwts.parser()
                    .setSigningKey( signingKey )
                    .parseClaimsJws( parsedToken )
                    .getBody()
                    .getSubject();

            AuthorizedUser authorizedUser = objectMapper.readValue(subject, AuthorizedUser.class);
            return authorizedUser.withToken( parsedToken );
        });
    }

    public Mono<String> generateToken(final UserModel userModel) {

        return Mono.fromCallable(() -> {
            var authorizedUser = AuthorizedUser.builder()
                    .id(userModel.getId())
                    .email( userModel.getEmail() )
                    .nickName( userModel.getNickName() )
                    .cpf( userModel.getCpf() )
                    .roles( userModel.getRoles() )
                    .build();

            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            Key signingKey = getSigningKey( signatureAlgorithm );

            var expiration = Date.from( LocalDateTime.now().plusDays(1L).atZone(ZoneId.systemDefault()).toInstant());
            return Jwts.builder()
                    .setIssuedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
                    .setSubject( objectMapper.writeValueAsString( authorizedUser ) )
                    .setIssuer( userModel.getEmail() )
                    .setExpiration( expiration )
                    .signWith( signingKey, signatureAlgorithm )
                    .compact();
        });
    }

    private Key getSigningKey( final SignatureAlgorithm signatureAlgorithm ) {
        byte[] apiKeySecretBytes = Base64.getEncoder().encode(securityConfig.getJwtSecretKey().getBytes());
        return new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
    }
}
