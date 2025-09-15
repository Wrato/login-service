package cl.fernando.login_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import cl.fernando.login_service.dto.UserRequest;
import cl.fernando.login_service.dto.UserResponse;
import cl.fernando.login_service.entity.User;
import cl.fernando.login_service.repository.UserRepository;
import cl.fernando.login_service.service.UserServiceImpl;
import cl.fernando.login_service.util.JwtUtil;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {
	
	@Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest validRequest;
    private User userEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        modelMapper = new ModelMapper();
        userService = new UserServiceImpl(userRepository, jwtUtil, modelMapper);

        validRequest = new UserRequest();
        validRequest.setName("Juan Perez");
        validRequest.setEmail("juan@testssw.cl");
        validRequest.setPassword("Ab12cd34");

        userEntity = new User();
        userEntity.setId(UUID.randomUUID().toString());
        userEntity.setName(validRequest.getName());
        userEntity.setEmail(validRequest.getEmail());
        userEntity.setPassword(validRequest.getPassword());
        userEntity.setCreated(LocalDateTime.now());
        userEntity.setLastLogin(LocalDateTime.now());
        userEntity.setActive(true);
        userEntity.setToken("fake-jwt-token");
    }

    @Test
    void testCreateUser_success() {
        when(userRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.empty());
        when(jwtUtil.generateToken(validRequest.getEmail())).thenReturn("fake-jwt-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.createUser(validRequest);

        assertNotNull(response);
        assertEquals("Juan Perez", response.getName());
        assertEquals("fake-jwt-token", response.getToken());
    }

    @Test
    void testCreateUser_userAlreadyExists() {
        when(userRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.of(userEntity));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(validRequest));
        assertEquals("Usuario ya existe", ex.getMessage());
    }

    @Test
    void testLogin_success() {
        when(jwtUtil.extractEmail(anyString())).thenReturn(validRequest.getEmail());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(jwtUtil.validateToken(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("fake-jwt-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.login("token");

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
    }

    @Test
    void testLogin_userNotFound() {
        when(jwtUtil.extractEmail(anyString())).thenReturn("notfound@test.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login("token"));
        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void testLogin_invalidToken() {
        when(jwtUtil.extractEmail(anyString())).thenReturn(validRequest.getEmail());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(jwtUtil.validateToken(anyString(), anyString())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login("token"));
        assertEquals("Token inválido o expirado", ex.getMessage());
    }

    @Test
    void testFindByEmail_found() {
        when(userRepository.findByEmail("juan@testssw.cl")).thenReturn(Optional.of(userEntity));
        Optional<User> found = userService.findByEmail("juan@testssw.cl");
        assertTrue(found.isPresent());
    }

    @Test
    void testFindByEmail_notFound() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        Optional<User> found = userService.findByEmail("notfound@test.com");
        assertFalse(found.isPresent());
    }

}
