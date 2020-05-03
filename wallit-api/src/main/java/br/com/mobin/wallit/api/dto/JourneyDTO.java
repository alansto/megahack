package br.com.mobin.wallit.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Value
@JsonDeserialize(builder = JourneyDTO.JacksonBuilder.class)
@Builder(builderClassName = "JacksonBuilder")
@With
public class JourneyDTO {

    String id;

    @Size(max = 20)
    @NotEmpty
    String title;
    @Size(max = 60)
    @NotEmpty
    String subTitle;

    @Size(max = 400)
    @NotEmpty
    String description;

    LocalDateTime created;
    LocalDateTime lastUpdated;

    @JsonPOJOBuilder(withPrefix = "")
    public static class JacksonBuilder{}
}
