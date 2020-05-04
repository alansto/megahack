package br.com.mobin.wallit.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import java.math.BigDecimal;

@Value
@JsonDeserialize(builder = DepositDTO.JacksonBuilder.class)
@Builder(builderClassName = "JacksonBuilder")
@With
public class DepositDTO {

    BigDecimal amount;

    @JsonPOJOBuilder(withPrefix = "")
    public static class JacksonBuilder{}
}
