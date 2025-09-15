package cl.fernando.login_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.fernando.login_service.dto.UserRequest;
import cl.fernando.login_service.dto.UserResponse;
import cl.fernando.login_service.service.UserServiceImpl;

@RestController
@RequestMapping("/api")
public class UserController {

	private final UserServiceImpl userService;
	public UserController(UserServiceImpl userService) {
		this.userService = userService;
	}
	
	@PostMapping("/sign-up")
	public ResponseEntity<?> signUp(@RequestBody UserRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
	}
	
	@PostMapping("/login")
	public ResponseEntity<UserResponse> login(@RequestHeader("Authorization") String token) {
		String jwt = token.replace("Bearer ", "");
		return ResponseEntity.ok(userService.login(jwt));
	}
}
