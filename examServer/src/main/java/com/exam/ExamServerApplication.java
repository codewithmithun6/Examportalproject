package com.exam;

import com.exam.helper.UserFoundException;
import com.exam.model.Role;
import com.exam.model.User;
import com.exam.model.UserRole;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.Set;


@SpringBootApplication
public class ExamServerApplication implements CommandLineRunner {

	@Autowired
	private UserService userService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(ExamServerApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {

		try {


			System.out.println("Starting code...");

			User user = new User();

			user.setFirstName("Mithun");
			user.setLastName("Kumar");
			user.setUserName("admin");
			user.setPassword(this.bCryptPasswordEncoder.encode("admin@123"));
			user.setEmail("admin33@gmail.com");
			user.setPhone("8909876543");
			user.setProfile("default.png");

			Role role1 = new Role();
			role1.setRoleId(44L);
			role1.setRoleName("ADMIN");

			Set<UserRole> userRoleSet = new HashSet<>();

			UserRole userRole = new UserRole();

			userRole.setRole(role1);
			userRole.setUser(user);

			userRoleSet.add(userRole);

			User user1 = this.userService.createUser(user, userRoleSet);
			System.out.println(user1.getUserName());

		}catch (UserFoundException e){
			e.printStackTrace();
		}

	}
}
