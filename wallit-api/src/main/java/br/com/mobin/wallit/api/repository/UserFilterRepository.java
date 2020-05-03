package br.com.mobin.wallit.api.repository;

import br.com.mobin.wallit.api.model.UserModel;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Repository
public class UserFilterRepository {

    private ReactiveMongoTemplate reactiveMongoTemplate;

    public Mono<UserModel> findOneByQuery(final Query query) {
        return reactiveMongoTemplate.findOne( query, UserModel.class );
    }

    public Flux<UserModel> findAllByQuery(final Query query) {
        return reactiveMongoTemplate.find( query, UserModel.class );
    }

    public Mono<Long> countByQuery(final Query query) {
        return reactiveMongoTemplate.count( query, UserModel.class );
    }
}
