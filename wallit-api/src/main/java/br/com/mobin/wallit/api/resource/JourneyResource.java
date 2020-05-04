package br.com.mobin.wallit.api.resource;

import br.com.mobin.wallit.api.dto.JourneyDTO;
import br.com.mobin.wallit.api.dto.SubscribedJourneyDTO;
import br.com.mobin.wallit.api.service.JourneyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/v1/journey")
public class JourneyResource {

    private JourneyService journeyService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<JourneyDTO> create(@RequestBody @Valid JourneyDTO journeyDTO) {

        return journeyService.create( journeyDTO );
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<JourneyDTO> update(@RequestBody @Valid JourneyDTO journeyDTO) {

        return journeyService.update( journeyDTO );
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Page<JourneyDTO>> findAll(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") @Min(10) @Max(50) Integer size) {
        return journeyService.findAll(PageRequest.of( page, size ));
    }

    @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<JourneyDTO> findById(@PathVariable @NotEmpty String id) {

        return journeyService.findById( id );
    }

    @PostMapping("/{id}/subscribe")
    public Mono<Void> subscribe(@PathVariable @NotEmpty String id,
                                @RequestBody @NotEmpty SubscribedJourneyDTO journey) {

        return journeyService.subscribeJourney( id, journey );
    }

    @DeleteMapping("/{id}/unsubscribe")
    public Mono<Void> unsubscribe(@PathVariable @NotEmpty String id) {

        return journeyService.unsubscribeJourney( id );
    }

    @PostMapping(value = "/{id}/deposit",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Void> deposit(  @PathVariable @NotEmpty String id,
                                @RequestPart Mono<FilePart> receipt,
                                @RequestPart String amount) {

        return receipt.flatMap(filePart ->
            journeyService.deposit( id, new BigDecimal(amount), filePart )
        ).onErrorResume(NumberFormatException.class::isInstance, ex ->
            Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount value "+amount+" invalid"))
        );
    }
}
