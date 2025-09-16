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
import java.util.Collections;
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

import cl.fernando.login_service.dto.PhoneRequest;
import cl.fernando.login_service.dto.UserRequest;
import cl.fernando.login_service.dto.UserResponse;
import cl.fernando.login_service.entity.Phone;
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
    void testCreateUser_withPhones() {
        UserRequest requestWithPhones = new UserRequest();
        requestWithPhones.setName("Pedro Test");
        requestWithPhones.setEmail("pedro@test.com");
        requestWithPhones.setPassword("Ab12cd34");

        // Simulamos phones
        PhoneRequest phoneReq = new PhoneRequest();
        phoneReq.setNumber(12345678L);
        phoneReq.setCitycode(2);
        phoneReq.setCountrycode("56");
        requestWithPhones.setPhones(Collections.singletonList(phoneReq));

        when(userRepository.findByEmail(requestWithPhones.getEmail())).thenReturn(Optional.empty());
        when(jwtUtil.generateToken(requestWithPhones.getEmail())).thenReturn("fake-jwt-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.createUser(requestWithPhones);

        assertNotNull(response);
        assertEquals("Pedro Test", response.getName());
        assertEquals("fake-jwt-token", response.getToken());
        assertNotNull(response.getPhones());
        assertEquals(1, response.getPhones().size());
        assertEquals(12345678L, response.getPhones().get(0).getNumber());
        assertEquals(2, response.getPhones().get(0).getCitycode());
        assertEquals("56", response.getPhones().get(0).getCountrycode());
    }

    @Test
    void testCreateUser_userAlreadyExists() {
        when(userRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.of(userEntity));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(validRequest));
        assertEquals("Usuario ya existe", ex.getMessage());
    }
    
    @Test
    void shouldThrowWhenEmailInvalid() {
        UserRequest invalidEmailRequest = new UserRequest();
        invalidEmailRequest.setName("Fernando");
        invalidEmailRequest.setEmail("fernando-at-mail.com"); // ❌ no válido
        invalidEmailRequest.setPassword("Ab12cd34"); // válido

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> userService.createUser(invalidEmailRequest));

        assertEquals("Formato de email inválido", ex.getMessage());
    }
    
    @Test
    void shouldThrowWhenPasswordInvalid() {
        UserRequest invalidPasswordRequest = new UserRequest();
        invalidPasswordRequest.setName("Fernando");
        invalidPasswordRequest.setEmail("fernando@mail.com"); // válido
        invalidPasswordRequest.setPassword("abcdef12"); // ❌ no cumple (sin mayúscula, solo 2 dígitos)

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> userService.createUser(invalidPasswordRequest));

        assertEquals("Formato de password inválido", ex.getMessage());
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
    void testLogin_withPhones() {
        // Crear un user con teléfonos
        User userWithPhones = new User();
        userWithPhones.setId(UUID.randomUUID().toString());
        userWithPhones.setName("User Phones");
        userWithPhones.setEmail(validRequest.getEmail());
        userWithPhones.setPassword(validRequest.getPassword());
        userWithPhones.setCreated(LocalDateTime.now());
        userWithPhones.setLastLogin(LocalDateTime.now());
        userWithPhones.setActive(true);
        userWithPhones.setToken("fake-jwt-token");

        Phone phone = new Phone();
        phone.setNumber(87654321L);
        phone.setCitycode(2);
        phone.setCountrycode("56");
        phone.setUser(userWithPhones);

        userWithPhones.setPhones(Collections.singletonList(phone));

        when(jwtUtil.extractEmail(anyString())).thenReturn(validRequest.getEmail());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userWithPhones));
        when(jwtUtil.validateToken(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("new-fake-jwt-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.login("token");

        assertNotNull(response);
        assertEquals("new-fake-jwt-token", response.getToken());
        assertNotNull(response.getPhones());
        assertEquals(1, response.getPhones().size());
        assertEquals(87654321L, response.getPhones().get(0).getNumber());
        assertEquals(2, response.getPhones().get(0).getCitycode());
        assertEquals("56", response.getPhones().get(0).getCountrycode());
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
