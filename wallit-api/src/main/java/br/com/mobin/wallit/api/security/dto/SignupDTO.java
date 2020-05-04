package br.com.mobin.wallit.api.security.dto;

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

@Value
@JsonDeserialize(builder = SignupDTO.JacksonBuilder.class)
@Builder(builderClassName = "JacksonBuilder")
@With
public class SignupDTO {

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

    LocalDateTime created;

    @JsonPOJOBuilder(withPrefix = "")
    public static class JacksonBuilder{}
}
