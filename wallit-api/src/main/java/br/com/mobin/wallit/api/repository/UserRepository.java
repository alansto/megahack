package br.com.mobin.wallit.api.repository;

import br.com.mobin.wallit.api.model.UserModel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveMongoRepository<UserModel,String> {
}
