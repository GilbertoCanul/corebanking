package dev.gcanul.corebanking.services;

import dev.gcanul.corebanking.dtos.AuthenticationRequest;
import dev.gcanul.corebanking.dtos.AuthenticationResponse;
import dev.gcanul.corebanking.dtos.RegisterRequest;
import dev.gcanul.corebanking.entities.User;
import dev.gcanul.corebanking.repositories.UserRepository;
import dev.gcanul.corebanking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest registerRequest) {
        // 1. Creamos el usuario encriptando la contraseña
        var user = User.builder()
                .username(registerRequest.username())
                .password(passwordEncoder.encode(registerRequest.password()))
                .build();

        // 2. Lo guardamos en la base de datos
        userRepository.save(user);

        // 3. Generamos el token para que ya entre logueado
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        // 1. El AuthenticationManager valida las credenciales. Si la contraseña es incorrecta, lanza excepción aquí.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.username(),
                        authenticationRequest.password()
                )
        );

        // 2. Si pasamos la línea anterior, el usuario es válido. Lo buscamos en la BD.
        var user = userRepository.findByUsername(authenticationRequest.username())
                .orElseThrow();

        // 3. Generamos su token y lo devolvemos
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }
}