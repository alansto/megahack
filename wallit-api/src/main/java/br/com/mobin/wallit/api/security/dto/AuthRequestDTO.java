package br.com.mobin.wallit.api.security.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Value
@JsonDeserialize(builder = AuthRequestDTO.JacksonBuilder.class)
@Builder(builderClassName = "JacksonBuilder")
@With
public class AuthRequestDTO {

    @Email
    String email;
    @Size(min = 6,max = 32)
    @NotEmpty
    String password;

    @JsonPOJOBuilder(withPrefix = "")
    public static class JacksonBuilder{}
}
