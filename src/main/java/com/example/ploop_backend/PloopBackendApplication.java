package com.example.ploop_backend;

import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.model.Role;
import com.example.ploop_backend.domain.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.support.MultipartFilter;

@EnableScheduling
@EnableJpaAuditing
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
	}

	@Bean
	public FilterRegistrationBean<MultipartFilter> multipartFilter() {
		FilterRegistrationBean<MultipartFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new MultipartFilter());
		registrationBean.setOrder(0); // ⭐ 가장 먼저 실행되게 설정
		return registrationBean;
	}
}

