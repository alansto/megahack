package br.com.mobin.wallit.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@JsonDeserialize(builder = SubscribedJourneyModel.JacksonBuilder.class)
@Builder(builderClassName = "JacksonBuilder")
@With
public class SubscribedJourneyModel {

    @Indexed(unique = true)
    String id;

    String title;
    String subTitle;

    @Builder.Default
    BigDecimal balance = BigDecimal.ZERO;
    @Builder.Default
    BigDecimal goal = BigDecimal.ZERO;

    LocalDate dueDate;

    @Builder.Default
    LocalDateTime subscribed = LocalDateTime.now();

    @JsonPOJOBuilder(withPrefix = "")
    public static class JacksonBuilder{}
}
