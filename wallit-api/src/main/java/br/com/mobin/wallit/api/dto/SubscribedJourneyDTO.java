package br.com.mobin.wallit.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@JsonDeserialize(builder = SubscribedJourneyDTO.JacksonBuilder.class)
@Builder(builderClassName = "JacksonBuilder")
@With
public class SubscribedJourneyDTO {

    String id;

    String title;
    String subTitle;

    BigDecimal balance;
    @Positive
    @NotNull
    BigDecimal goal;
    @NotNull
    @Future
    LocalDate dueDate;

    LocalDateTime subscribed;

    @JsonPOJOBuilder(withPrefix = "")
    public static class JacksonBuilder{}
}
