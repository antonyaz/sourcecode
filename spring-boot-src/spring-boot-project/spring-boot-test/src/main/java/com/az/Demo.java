package com.az;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Demo {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Demo.class, args);
		UserService userService = (UserService) context.getBean("userService");
		userService.sayHello();
		System.out.println(userService);
		System.out.println("hello world");
	}
}
