package br.com.mobin.wallit.api.model;

import br.com.mobin.wallit.core.security.model.Role;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Value
@JsonDeserialize(builder = UserModel.JacksonBuilder.class)
@Builder(builderClassName = "JacksonBuilder")
@With
@Document("users")
public class UserModel {

    @Id
    String id;

    @Indexed(unique=true)
    String nickName;
    String fullName;

    @Indexed(unique=true)
    String cpf;

    @Indexed(unique=true)
    String email;
    String password;

    @Builder.Default
    Boolean enabled = true;

    @Singular
    List<Role> roles;

    @Builder.Default
    Integer score = 0;

    //TODO Validar campos retornados pelo enriquecimento pertinentes

    @Builder.Default
    LocalDateTime created = LocalDateTime.now();
    @Builder.Default
    LocalDateTime lastUpdated = LocalDateTime.now();
    @Singular
    List<SubscribedJourneyModel> journeys;

    @JsonPOJOBuilder(withPrefix = "")
    public static class JacksonBuilder{}
}
