package br.com.mobin.wallit.api.security.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@JsonDeserialize(builder = AuthResponseDTO.JacksonBuilder.class)
@Builder(builderClassName = "JacksonBuilder")
@With
public class AuthResponseDTO {

    String token;

    @JsonPOJOBuilder(withPrefix = "")
    public static class JacksonBuilder{}
}
