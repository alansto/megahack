package br.com.mobin.wallit.api.resource;

import br.com.mobin.wallit.api.security.dto.AuthRequestDTO;
import br.com.mobin.wallit.api.security.dto.AuthResponseDTO;
import br.com.mobin.wallit.api.security.dto.SignupDTO;
import br.com.mobin.wallit.api.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@AllArgsConstructor
@RestController
@RequestMapping("/v1")
public class AuthResource {

    private AuthService authService;

    @PostMapping("/signin")
    public Mono<AuthResponseDTO> signin(@RequestBody @Valid AuthRequestDTO authRequestDTO) {
        return authService.signin( authRequestDTO );
    }

    @PostMapping(value ="/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SignupDTO> create(@RequestBody @Valid SignupDTO signupDTO) {
        return authService.signup( signupDTO );
    }
}
