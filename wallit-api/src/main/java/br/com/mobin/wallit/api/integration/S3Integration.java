package br.com.mobin.wallit.api.integration;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class S3Integration {

    public Mono<Void> put(final FilePart file,String fileName, String contentType) {


        return Mono.empty();
    }
}
