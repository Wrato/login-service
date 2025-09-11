package cl.fernando.login_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import cl.fernando.login_service.dto.PhoneResponse;
import cl.fernando.login_service.dto.UserRequest;
import cl.fernando.login_service.dto.UserResponse;
import cl.fernando.login_service.entity.Phone;
import cl.fernando.login_service.entity.User;
import cl.fernando.login_service.repository.UserRepository;
import cl.fernando.login_service.util.JwtUtil;

@Service
public class UserService {
	
	private final UserRepository repository;
    private final JwtUtil jwtUtil;
    private final ModelMapper mapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repository, JwtUtil jwtUtil, ModelMapper mapper) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
        this.mapper = mapper;
    }
    
    public UserResponse createUser(UserRequest req) {
        // Validaciones
        if (!req.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new RuntimeException("Formato de email inválido");
        }
        if (!req.getPassword().matches("^(?=.*[A-Z])(?=(?:.*\\d){2})([a-zA-Z\\d]{8,12})$")) {
            throw new RuntimeException("Formato de password inválido");
        }
        repository.findByEmail(req.getEmail())
                  .ifPresent(u -> { throw new RuntimeException("Usuario ya existe"); });

        // Crear user
        User newUser = new User();
        newUser.setName(req.getName());
        newUser.setEmail(req.getEmail());
        newUser.setPassword(encoder.encode(req.getPassword()));
        newUser.setCreated(LocalDateTime.now());
        newUser.setLastLogin(LocalDateTime.now());
        newUser.setActive(true);
        newUser.setToken(jwtUtil.generateToken(newUser.getEmail()));
        
        // Mapear phones
        if (req.getPhones() != null) {
            List<Phone> phones = req.getPhones().stream()
                .map(pr -> {
                    Phone p = new Phone();
                    p.setNumber(pr.getNumber());
                    p.setCitycode(pr.getCitycode());
                    p.setCountrycode(pr.getCountrycode());
                    p.setUser(newUser); // ahora sí se puede asignar el user
                    return p;
                })
                .collect(Collectors.toList());
            newUser.setPhones(phones);
        }

        User savedUser = repository.save(newUser);

        return mapper.map(savedUser, UserResponse.class);
    }
    
    public UserResponse login(String token) {
        String email = jwtUtil.extractEmail(token);
        User user = repository.findByEmail(email)
                              .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!jwtUtil.validateToken(token, user.getEmail())) {
            throw new RuntimeException("Token inválido o expirado");
        }

        user.setLastLogin(LocalDateTime.now());
        user.setToken(jwtUtil.generateToken(user.getEmail()));
        User updated = repository.save(user);

        UserResponse response = mapper.map(updated, UserResponse.class);

        if (updated.getPhones() != null) {
            response.setPhones(
                updated.getPhones().stream()
                    .map(p -> new PhoneResponse(
                        p.getNumber(), p.getCitycode(), p.getCountrycode()))
                    .collect(Collectors.toList())
            );
        }

        return response;
    }
    
    public java.util.Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

}
