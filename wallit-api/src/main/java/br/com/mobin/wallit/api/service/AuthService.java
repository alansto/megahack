package br.com.mobin.wallit.api.service;

import br.com.mobin.wallit.api.model.UserModel;
import br.com.mobin.wallit.api.repository.UserFilterRepository;
import br.com.mobin.wallit.api.repository.UserRepository;
import br.com.mobin.wallit.api.security.SecurityHelper;
import br.com.mobin.wallit.api.security.dto.AuthRequestDTO;
import br.com.mobin.wallit.api.security.dto.AuthResponseDTO;
import br.com.mobin.wallit.api.security.dto.SignupDTO;
import br.com.mobin.wallit.core.security.model.Role;
import br.com.mobin.wallit.core.security.utils.CryptoUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class AuthService {

    private SecurityHelper securityHelper;
    private UserFilterRepository filterRepository;
    private UserRepository userRepository;

    public Mono<AuthResponseDTO> signin(final AuthRequestDTO authRequestDTO) {

        return Mono.fromCallable(() ->
            new Query().addCriteria(
                    Criteria.where("email").is(authRequestDTO.getEmail())
                            .and("password").is(CryptoUtil.encrypt( authRequestDTO.getPassword()))
            )
        )
        .flatMap(query ->
            filterRepository.findOneByQuery( query )
                    .flatMap(securityHelper::generateToken)
                    .map(token ->
                            AuthResponseDTO.builder().token(token).build()
                    )
                    .switchIfEmpty(
                            Mono.error(
                                    new ResponseStatusException(
                                            HttpStatus.UNAUTHORIZED,
                                            "User: "+authRequestDTO.getEmail()+" not authorized")
                            )
                    )
        )
         .onErrorMap(throwable -> {
             log.error("Error to authenticate user: "+authRequestDTO.getEmail());
             return new ResponseStatusException( HttpStatus.UNAUTHORIZED, "User: "+authRequestDTO.getEmail()+" not authorized");
         });
    }

    public Mono<SignupDTO> signup(final SignupDTO signupDTO) {

        return prepare( signupDTO )
                .flatMap( userRepository::save )
                .flatMap(createdUser -> {
                    //TODO Dispara evento para enriquecimento do cadastro do novo usuario
                    return Mono.just( createdUser );
                })
                .flatMap(createdUser -> {
                    //TODO Dispara evento para envio de email de confirmacao de cadastro do usuario
                    return Mono.just( createdUser );
                })
                .onErrorMap(DuplicateKeyException.class::isInstance, ex -> {
                    log.warn("User already exists: {}", signupDTO, ex);
                    return new ResponseStatusException( HttpStatus.CONFLICT, "User already exists" );
                })
                .map(this::parseTo);


    }

    private Mono<UserModel> prepare(final SignupDTO signupDTO ) {

        return Mono.fromCallable(() -> {
            String encryptedPassword = CryptoUtil.encrypt( signupDTO.getPassword() );

            return UserModel.builder()
                    .nickName( signupDTO.getNickName() )
                    .password( encryptedPassword )
                    .cpf( signupDTO.getCpf() )
                    .fullName( signupDTO.getFullName() )
                    .email( signupDTO.getEmail() )
                    .role( Role.ROLE_USER )
                    .build();
        });
    }

    private SignupDTO parseTo(final UserModel userModel) {

        return SignupDTO.builder()
                .id( userModel.getId() )
                .nickName( userModel.getNickName() )
                .fullName( userModel.getFullName() )
                .cpf( userModel.getCpf() )
                .email( userModel.getEmail() )
                .created( userModel.getCreated() )
                .build();
    }
}
