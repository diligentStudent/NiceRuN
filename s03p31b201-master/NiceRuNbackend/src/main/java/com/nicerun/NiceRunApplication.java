package com.nicerun;

import java.util.Date;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NiceRunApplication {

	@PostConstruct
	public void started() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		System.out.println("현재시각 : " + new Date());
	}

	public static void main(String[] args) {
		SpringApplication.run(NiceRunApplication.class, args);
	}

}
