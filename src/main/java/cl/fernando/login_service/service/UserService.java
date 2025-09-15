package cl.fernando.login_service.service;

import java.util.Optional;

import cl.fernando.login_service.dto.UserRequest;
import cl.fernando.login_service.dto.UserResponse;
import cl.fernando.login_service.entity.User;

public interface UserService {
	
	UserResponse createUser(UserRequest req);
	
	UserResponse login(String token);
	
	Optional<User> findByEmail(String email);

}
