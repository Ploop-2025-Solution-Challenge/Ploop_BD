package com.example.ploop_backend;

import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.model.Role;
import com.example.ploop_backend.domain.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PloopBackendApplication implements CommandLineRunner {

	private final UserRepository userRepository;

	public PloopBackendApplication(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(PloopBackendApplication.class, args);
	}

	@Override
	public void run(String... args) {
		/*User user = User.builder()
				.email("test@google.com")
				.googleId("123456")
				.name("테스트 유저")
				.nickname("테유")
				.age(25)
				.country("KR")
				.region("서울")
				.picture("https://example.com/profile.jpg")
				.role(Role.USER)
				.build();*/

		//userRepository.save(user);
	}
}

