package com.nicerun.dto;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.nicerun.request.UserRequest;

import lombok.Data;

@Entity
@Data
@Table(name = "user")
public class UserDto {
	@Id
	String email;
	String location;
	int height;
	int weight;
	boolean gender;// true남 false여
	int goaldist;
	String password;
	String lastname;// 성
	String firstname;// 이름
	String profileimg;// 프로필 이미지
	Date birthday;// 생년,월,일

	public UserDto() {
		
	}
	
	public UserDto(UserRequest userRequest) {
		this.birthday = userRequest.getBirthday();
		this.email = userRequest.getEmail();
		this.firstname = userRequest.getFirstname();
		this.gender = userRequest.isGender();
		this.goaldist = userRequest.getGoaldist();
		this.height = userRequest.getHeight();
		this.lastname = userRequest.getLastname();
		this.location = userRequest.getLocation();
		this.password = userRequest.getPassword();
		this.profileimg = userRequest.getProfileimg();
		this.weight = userRequest.getWeight();
	}
}
