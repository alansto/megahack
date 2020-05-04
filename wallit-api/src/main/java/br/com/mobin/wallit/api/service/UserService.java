package br.com.mobin.wallit.api.service;

import br.com.mobin.wallit.api.dto.SubscribedJourneyDTO;
import br.com.mobin.wallit.api.dto.UserDTO;
import br.com.mobin.wallit.api.model.UserModel;
import br.com.mobin.wallit.api.repository.JourneyFilterRepository;
import br.com.mobin.wallit.api.repository.UserRepository;
import br.com.mobin.wallit.api.security.WallitRoles;
import br.com.mobin.wallit.core.security.model.HasRole;
import br.com.mobin.wallit.core.security.utils.CryptoUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class UserService {

    private UserRepository userRepository;
    private JourneyFilterRepository journeyFilterRepository;

    @HasRole(roles = WallitRoles.ROLE_ADMIN)
    public Mono<UserDTO> create(final UserDTO userDTO) {

        return prepare( userDTO )
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
                    log.warn("User already exists: {}", userDTO, ex);
                    return new ResponseStatusException( HttpStatus.CONFLICT, "User already exists" );
                })
                .map(this::parseTo);
    }

    @HasRole(roles = WallitRoles.ROLE_USER)
    public Mono<UserDTO> findById(final String id) {
        return findModelById( id )
                .map(this::parseTo);
    }

    private Mono<UserModel> findModelById(final String id) {
        try {
            return userRepository.findById( id )
                    .switchIfEmpty(
                            Mono.error( new ResponseStatusException( HttpStatus.NOT_FOUND, "User not found" ) )
                    );
        } catch (IllegalArgumentException e) {
            return Mono.error( new ResponseStatusException( HttpStatus.NOT_FOUND, "User not found" ) );
        }
    }

    private Mono<UserModel> prepare( final UserDTO userDTO ) {

        return Mono.fromCallable(() -> {
            String encryptedPassword = CryptoUtil.encrypt( userDTO.getPassword() );

            return UserModel.builder()
                    .nickName( userDTO.getNickName() )
                    .password( encryptedPassword )
                    .cpf( userDTO.getCpf() )
                    .fullName( userDTO.getFullName() )
                    .email( userDTO.getEmail() )
                    .roles( userDTO.getRoles() )
                    .build();
        });
    }

    private UserDTO parseTo(final UserModel userModel) {

        return UserDTO.builder()
                .id( userModel.getId() )
                .nickName( userModel.getNickName() )
                .fullName( userModel.getFullName() )
                .cpf( userModel.getCpf() )
                .email( userModel.getEmail() )
                .created( userModel.getCreated() )
                .lastUpdated( userModel.getLastUpdated() )
                .score( userModel.getScore() )
                .journeys(
                        Optional.ofNullable( userModel.getJourneys() )
                            .map(subscribes ->
                                subscribes.parallelStream()
                                        .map(subscribeModel ->
                                            SubscribedJourneyDTO.builder()
                                                    .id( subscribeModel.getId() )
                                                    .title( subscribeModel.getTitle() )
                                                    .subTitle( subscribeModel.getSubTitle() )
                                                    .balance( subscribeModel.getBalance() )
                                                    .goal( subscribeModel.getGoal() )
                                                    .dueDate( subscribeModel.getDueDate() )
                                                    .subscribed( subscribeModel.getSubscribed() )
                                                    .build()
                                        ).collect(Collectors.toList())
                            ).orElse( List.of() )
                )
                .build();
    }
}
