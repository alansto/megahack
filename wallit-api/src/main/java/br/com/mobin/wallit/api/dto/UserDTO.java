package br.com.mobin.wallit.api.dto;

import br.com.mobin.wallit.core.security.model.Role;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.hibernate.validator.constraints.br.CPF;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Value
@JsonDeserialize(builder = UserDTO.JacksonBuilder.class)
@Builder(builderClassName = "JacksonBuilder")
@With
public class UserDTO {

    String id;

    @Size(min = 4,max = 60)
    @NotEmpty
    String nickName;
    @Size(max = 60)
    @NotEmpty
    String fullName;
    @CPF
    String cpf;

    @Email
    String email;
    @Size(min = 6,max = 32)
    @NotEmpty
    String password;

    @NotEmpty
    List<Role> roles;

    Integer score;

    LocalDateTime created;
    LocalDateTime lastUpdated;

    List<SubscribedJourneyDTO> journeys;

    @JsonPOJOBuilder(withPrefix = "")
    public static class JacksonBuilder{}
}
