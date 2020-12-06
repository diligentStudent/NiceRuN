package com.nicerun.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nicerun.dto.UserDto;

@Repository
public interface UserDao extends JpaRepository<UserDto, String> {

	Optional<UserDto> findUserByEmailAndPassword(String email, String password);
	
	Optional<UserDto> findUserDtoByEmail(String email);
}
