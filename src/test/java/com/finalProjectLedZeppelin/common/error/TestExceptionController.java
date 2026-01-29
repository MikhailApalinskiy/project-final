package com.finalProjectLedZeppelin.common.error;

import com.finalProjectLedZeppelin.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/__test")
class TestExceptionController {

    @GetMapping("/illegal-argument")
    void illegalArgument() {
        throw new IllegalArgumentException("bad argument");
    }

    @GetMapping("/email-exists")
    void emailExists() {
        throw new AuthService.EmailAlreadyExistsException("email exists");
    }

    @GetMapping("/bad-credentials")
    void badCredentials() {
        throw new AuthService.BadCredentialsException();
    }

    @GetMapping("/not-found")
    void notFound() {
        throw new NotFoundException("not found");
    }

    @PostMapping("/not-readable")
    void notReadable(@RequestBody TestDto dto) {
    }

    @PostMapping("/validation")
    void validation(@Valid @RequestBody TestDto dto) {
    }

    record TestDto(
            @NotBlank String name
    ) {
    }
}
