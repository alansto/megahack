package br.com.mobin.wallit.api.service;

import br.com.mobin.wallit.api.dto.JourneyDTO;
import br.com.mobin.wallit.api.dto.SubscribedJourneyDTO;
import br.com.mobin.wallit.api.integration.S3Integration;
import br.com.mobin.wallit.api.model.JourneyModel;
import br.com.mobin.wallit.api.model.SubscribedJourneyModel;
import br.com.mobin.wallit.api.model.UserModel;
import br.com.mobin.wallit.api.repository.JourneyFilterRepository;
import br.com.mobin.wallit.api.repository.JourneyRepository;
import br.com.mobin.wallit.api.repository.UserRepository;
import br.com.mobin.wallit.api.security.SecurityHelper;
import br.com.mobin.wallit.api.security.WallitRoles;
import br.com.mobin.wallit.core.security.model.AuthorizedUser;
import br.com.mobin.wallit.core.security.model.HasRole;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class JourneyService {

    private JourneyFilterRepository filterRepository;
    private JourneyRepository journeyRepository;
    private UserRepository userRepository;

    private S3Integration storageIntegration;

    @HasRole(roles = WallitRoles.ROLE_ADMIN)
    public Mono<JourneyDTO> create(final JourneyDTO journeyDTO) {

        return journeyRepository.insert(
                JourneyModel.builder()
                        .title( journeyDTO.getTitle() )
                        .subTitle( journeyDTO.getSubTitle() )
                        .description( journeyDTO.getDescription() )
                .build()
        )
        .map(this::parseTo)
        .onErrorMap(DuplicateKeyException.class::isInstance, ex -> {
            log.warn("User already exists: {}", journeyDTO, ex);
            return new ResponseStatusException( HttpStatus.CONFLICT, "Journey already exists" );
        });
    }

    @HasRole(roles = {WallitRoles.ROLE_USER,WallitRoles.ROLE_ADMIN})
    public Mono<JourneyDTO> findById(final String id) {
        return findModelById( id ).map(this::parseTo);
    }

    private Mono<JourneyModel> findModelById(final String id) {
        try {
            return journeyRepository.findById( id )
                    .switchIfEmpty(
                            Mono.error( new ResponseStatusException( HttpStatus.NOT_FOUND, "Journey not found" ) )
                    );
        } catch (IllegalArgumentException e) {
            return Mono.error( new ResponseStatusException( HttpStatus.NOT_FOUND, "Journey not found" ) );
        }
    }

    @HasRole(roles = WallitRoles.ROLE_ADMIN)
    public Mono<JourneyDTO> update(final JourneyDTO journeyDTO) {

        return findModelById( journeyDTO.getId() )
                .map(foundJourney ->
                    foundJourney.withTitle( journeyDTO.getTitle() )
                            .withSubTitle( journeyDTO.getSubTitle() )
                            .withDescription( journeyDTO.getDescription() )
                            .withLastUpdated( LocalDateTime.now() )
                )
                .flatMap(journeyRepository::save)
                .map(updatedJourney ->
                    journeyDTO.withCreated( updatedJourney.getCreated() )
                            .withLastUpdated( updatedJourney.getLastUpdated() )
                )
                .onErrorMap(IllegalArgumentException.class::isInstance, ex -> {
                    log.error("Journey {} not found", journeyDTO.getId(), ex);
                    return new ResponseStatusException( HttpStatus.NOT_FOUND, "Journey "+journeyDTO+" not found" );
                })
                .switchIfEmpty(
                        Mono.error( new ResponseStatusException( HttpStatus.NOT_FOUND, "Journey not found" ) )
                );
    }

    @HasRole(roles = {WallitRoles.ROLE_USER,WallitRoles.ROLE_ADMIN})
    public Mono<Page<JourneyDTO>> findAll(final Pageable pageable) {

        final Query query = new Query().with(Sort.by(Sort.Direction.DESC, "title")).with(pageable);

        return filterRepository.findAllByQuery( query )
                .collectList()
                .map(journeys ->
                    journeys.parallelStream()
                            .map(this::parseTo)
                            .collect(Collectors.toList())
                )
                .zipWith(
                        filterRepository.countByQuery(query)
                )
                .map(tuple ->
                    new PageImpl<JourneyDTO>( tuple.getT1(), pageable, tuple.getT2() )
                );
    }

    @HasRole(roles = {WallitRoles.ROLE_USER,WallitRoles.ROLE_ADMIN})
    public Mono<Void> subscribeJourney(final String journeyId, final SubscribedJourneyDTO journey) {

        return Mono.subscriberContext().<AuthorizedUser>map(context -> context.get(SecurityHelper.AUTHORIZED_USER))
                .flatMap(authorizedUser ->
                    findUserById( authorizedUser.getId() )
                )
                .flatMap(userModel ->
                    findById( journeyId )
                            .map(journeyModel -> {
                                var subscribe = SubscribedJourneyModel.builder()
                                        .id( journeyModel.getId() )
                                        .title( journeyModel.getTitle() )
                                        .subTitle( journeyModel.getSubTitle() )
                                        .goal( journey.getGoal() )
                                        .dueDate( journey.getDueDate() )
                                        .build();

                                return userModel.withJourneys(
                                        Optional.ofNullable( userModel.getJourneys() )
                                                .map(journeys -> {
                                                    var exists = journeys.parallelStream()
                                                            .anyMatch(subscribedJourneyModel -> subscribedJourneyModel.getId().equals(subscribe.getId()));

                                                    if (!exists)
                                                        journeys.add(subscribe);

                                                    return journeys;
                                                }).orElse( List.of( subscribe ) )
                                ).withScore( 50 );
                            })
                )
                .flatMap(userWithJourneys ->
                        userRepository.save( userWithJourneys )
                ).then();
    }

    @HasRole(roles = {WallitRoles.ROLE_USER,WallitRoles.ROLE_ADMIN})
    public Mono<Void> unsubscribeJourney(final String id) {

        return Mono.subscriberContext().<AuthorizedUser>map(context -> context.get(SecurityHelper.AUTHORIZED_USER))
                .flatMap(authorizedUser ->
                        findUserById( authorizedUser.getId() )
                )
                .<UserModel>handle((userModel, sink) -> {

                    var found = Optional.ofNullable( userModel.getJourneys() )
                            .map(journeys ->
                                    journeys.parallelStream()
                                            .filter(subscribed -> subscribed.getId().equals(id))
                                            .findFirst()
                            ).orElse( null );

                    found.ifPresentOrElse(
                            subscribedJourney -> {
                                if (subscribedJourney.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                                    sink.next(userModel);
                                } else {
                                    sink.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Journey balance not ZERO"));
                                }
                            },
                            () -> sink.error(
                                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Journey not subscribed")
                            )
                    );
                })
                .map(userModel ->
                        userModel.withJourneys(
                                Optional.ofNullable( userModel.getJourneys() )
                                        .map(journeys ->
                                                journeys.parallelStream()
                                                        .filter(subscribed -> !subscribed.getId().equals(id))
                                                        .collect(Collectors.toList())
                                        ).orElse( List.of() )
                        )
                )
                .flatMap(userRepository::save)
                .then();
    }

    @HasRole(roles = {WallitRoles.ROLE_USER,WallitRoles.ROLE_ADMIN})
    public Mono<Void> deposit(final String id, final BigDecimal amount, final FilePart receipt) {

        return Mono.subscriberContext().<AuthorizedUser>map(context -> context.get(SecurityHelper.AUTHORIZED_USER))
                .flatMap(authorizedUser -> {
                    var filename = id + "_"+ LocalDateTime.now().toString() +".jpg";
                    return storageIntegration.put( receipt, filename, "image/jpeg" )
                            .thenReturn( authorizedUser );
                })
                .flatMap(authorizedUser ->

                    findUserById( authorizedUser.getId() )
                            .map(userModel ->
                                    userModel.withJourneys(
                                            userModel.getJourneys().parallelStream()
                                                    .map(subscribedJourneyModel -> {
                                                        if (subscribedJourneyModel.getId().equals(id)) {
                                                            return subscribedJourneyModel.withBalance(
                                                                    subscribedJourneyModel.getBalance().add( amount )
                                                            );
                                                        }
                                                        return subscribedJourneyModel;
                                                    }).collect(Collectors.toList())
                                    ).withScore( userModel.getScore() + 150 )
                            )
                            .flatMap(userRepository::save)
                ).then();
    }

    private Mono<UserModel> findUserById(final String id) {
        try {
            return userRepository.findById( id )
                    .switchIfEmpty(
                            Mono.error( new ResponseStatusException( HttpStatus.NOT_FOUND, "User not found" ) )
                    );
        } catch (IllegalArgumentException e) {
            return Mono.error( new ResponseStatusException( HttpStatus.NOT_FOUND, "User not found" ) );
        }
    }

    private JourneyDTO parseTo( final JourneyModel journeyModel ) {
        return JourneyDTO.builder()
                .id( journeyModel.getId() )
                .title( journeyModel.getTitle() )
                .subTitle( journeyModel.getSubTitle() )
                .description( journeyModel.getDescription() )
                .created( journeyModel.getCreated() )
                .lastUpdated( journeyModel.getLastUpdated() )
                .build();
    }
}
