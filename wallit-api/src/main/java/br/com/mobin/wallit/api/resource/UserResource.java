package br.com.mobin.wallit.api.resource;

import br.com.mobin.wallit.api.dto.SubscribedJourneyDTO;
import br.com.mobin.wallit.api.dto.UserDTO;
import br.com.mobin.wallit.api.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@RestController
@RequestMapping("/v1/user")
public class UserResource {

    private UserService userService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserDTO> create(@RequestBody @Valid UserDTO userDTO) {

        return userService.create( userDTO );
    }

    @GetMapping("/{id}")
    public Mono<UserDTO> findById(@PathVariable @NotEmpty String id) {

        return userService.findById( id );
    }
}
