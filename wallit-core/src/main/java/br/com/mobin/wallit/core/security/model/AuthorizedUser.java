package br.com.mobin.wallit.core.security.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.With;
import java.util.List;

@Value
@JsonDeserialize(builder = AuthorizedUser.JacksonBuilder.class)
@Builder(builderClassName = "JacksonBuilder")
@With
public class AuthorizedUser {

    String id;
    String nickName;
    String email;
    String cpf;

    @Singular
    List<Role> roles;

    Boolean enabled;

    String token;
    String remoteAddress;

    @JsonPOJOBuilder(withPrefix = "")
    public static class JacksonBuilder{}
}
