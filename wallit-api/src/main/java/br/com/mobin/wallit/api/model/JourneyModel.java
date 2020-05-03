package br.com.mobin.wallit.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Value
@JsonDeserialize(builder = JourneyModel.JacksonBuilder.class)
@Builder(builderClassName = "JacksonBuilder")
@With
@Document("journeys")
public class JourneyModel {

    @Id
    String id;

    String title;
    String subTitle;

    String description;

    @Builder.Default
    Long activations = 0L;

    @Builder.Default
    LocalDateTime created = LocalDateTime.now();
    @Builder.Default
    LocalDateTime lastUpdated = LocalDateTime.now();

    @JsonPOJOBuilder(withPrefix = "")
    public static class JacksonBuilder{}
}
