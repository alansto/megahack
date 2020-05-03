package br.com.mobin.wallit.api.repository;

import br.com.mobin.wallit.api.model.JourneyModel;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Repository
public class JourneyFilterRepository {

    private ReactiveMongoTemplate reactiveMongoTemplate;

    public Mono<JourneyModel> findOneByQuery(final Query query) {
        return reactiveMongoTemplate.findOne( query, JourneyModel.class );
    }

    public Flux<JourneyModel> findAllByQuery(final Query query) {
        return reactiveMongoTemplate.find( query, JourneyModel.class );
    }

    public Mono<Long> countByQuery(final Query query) {
        return reactiveMongoTemplate.count( query, JourneyModel.class );
    }
}
