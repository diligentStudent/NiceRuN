package com.nicerun.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nicerun.dao.UserDao;
import com.nicerun.dto.UserDto;
import com.nicerun.util.SaveFile;
import com.nicerun.util.SaveFile.FileType;

@Service
public class UserService {
	@Autowired
	UserDao userDao;

	@Transactional
	public UserDto saveUser(UserDto userDto) {
		return userDao.save(userDto);
	}

	public Optional<UserDto> loginUser(String email, String password) {
		return userDao.findUserByEmailAndPassword(email, password);
	}

	public Optional<UserDto> findUserByEmail(String email) {
		return userDao.findUserDtoByEmail(email);
	}

	public UserDto changeProfileImg(String email, List<MultipartFile> mfl) throws Exception {
		Optional<UserDto> opuserDto = userDao.findUserDtoByEmail(email);

		if (opuserDto.isPresent()) {
			UserDto userDto = opuserDto.get();
			List<String> list = SaveFile.SaveFIle(mfl, FileType.Image);
			userDto.setProfileimg(list.get(0));

			return saveUser(userDto);
		} else {
			return null;
		}
	}

}
