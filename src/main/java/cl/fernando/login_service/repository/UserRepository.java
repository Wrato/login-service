package cl.fernando.login_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.fernando.login_service.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
	
	Optional<User> findByEmail(String email);

}
