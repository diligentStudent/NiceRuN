package com.nicerun.request;

import java.sql.Date;

import lombok.Data;

@Data
public class UserRequest {
	String location;
	int height;
	int weight;
	boolean gender;//true남 false여
	int goaldist;
	String email;
	String password;
	String lastname;//성
	String firstname;//이름
	String profileimg;//프로필 이미지
	Date birthday;//생년,월,일
	
	public UserRequest(){
		
	}
	
}
