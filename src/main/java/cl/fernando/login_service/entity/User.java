package cl.fernando.login_service.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
	
	@Id
	private String id = UUID.randomUUID().toString();
	
	private String name;
	private String email;
	private String password;
	
	private LocalDateTime created = LocalDateTime.now();
	private LocalDateTime lastLogin = LocalDateTime.now();
	
	private String token;
	private boolean isActive = true;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Phone> phones;
	
}
