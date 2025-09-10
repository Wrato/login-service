package cl.fernando.login_service.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
	private String id;
	private String name;
	private String email;
	private LocalDateTime created;
	private LocalDateTime lastLogin;
	private String token;
	private boolean isActive;
	private List<PhoneResponse> phones;
	
	public UserResponse(String id, String name, String email, String token) {
		super();
		this.id = id;
		this.name = name;
		this.email = email;
		this.token = token;
	}	
	
}
