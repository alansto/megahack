package br.com.mobin.wallit.api.repository;

import br.com.mobin.wallit.api.model.JourneyModel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JourneyRepository extends ReactiveMongoRepository<JourneyModel,String> {
}
