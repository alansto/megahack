package br.com.mobin.wallit.api.resource;

import br.com.mobin.wallit.api.dto.JourneyDTO;
import br.com.mobin.wallit.api.dto.SubscribedJourneyDTO;
import br.com.mobin.wallit.api.service.JourneyService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

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

    @PatchMapping("/{id}/deposit")
    public Mono<Void> deposit() {

        return Mono.empty();
    }
}
