package com.nicerun.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nicerun.dto.CalendarDto;
import com.nicerun.dto.UserDto;
import com.nicerun.request.UserRequest;
import com.nicerun.service.CalendarService;
import com.nicerun.service.UserService;
import com.nicerun.util.BasicResponse;
import com.nicerun.util.MailService;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = { "*" }, maxAge = 6000)
@RestController
@RequestMapping("/member")
public class MemberController {

	@Autowired
	UserService userService;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	CalendarService calendarService;

	@PostMapping("/signup")
	@ApiOperation(value = "회원가입")
	public Object signup(@RequestBody UserRequest userRequest) {
		final BasicResponse basicResponse = new BasicResponse();
		try {
			Map<String, Object> map = new HashMap<>();
			UserDto userDto = new UserDto(userRequest);
			UserDto result = userService.saveUser(userDto);
			map.put("user", result);
			basicResponse.object = map;
			basicResponse.data = "success";
			basicResponse.status = true;

		} catch (Exception e) {
			basicResponse.data = "error";
			basicResponse.status = true;
			e.printStackTrace();
		} finally {
			return new ResponseEntity(basicResponse, HttpStatus.OK);
		}
	}

	@PostMapping("/login")
	@ApiOperation(value = "로그인")
	public Object login(@RequestParam(required = true) final String email,
			@RequestParam(required = true) final String password) {
		final BasicResponse result = new BasicResponse();
		try {
			Map<String, Object> map = new HashMap<>();
			Optional<UserDto> userDto = userService.loginUser(email, password);
			if (userDto.isPresent()) {
				map.put("user", userDto);
				result.status = true;
				result.data = "success";
				result.object = map;
			} else {
				result.status = true;
				result.data = "fail";
			}
		} catch (Exception e) {
			result.status = true;
			result.data = "error";
			e.printStackTrace();
		} finally {
			return new ResponseEntity(result, HttpStatus.OK);
		}

	}

	@PostMapping("/emailvalidate")
	@ApiOperation(value = "아이디 유효성 검사")
	public Object idValidate(@RequestParam String email) {
		final BasicResponse basicResponse = new BasicResponse();

		try {
			Optional<UserDto> opUserDto = userService.findUserByEmail(email);

			if (opUserDto.isPresent()) {
				basicResponse.object = false;
				basicResponse.data = "fail";
				basicResponse.status = true;
			} else {
				basicResponse.object = true;
				basicResponse.data = "success";
				basicResponse.status = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			basicResponse.object = false;
			basicResponse.data = "error";
			basicResponse.status = true;
		} finally {
			return new ResponseEntity(basicResponse, HttpStatus.OK);
		}
	}

	@PostMapping("/myprofile")
	@ApiOperation(value = "마이 페이지")
	public Object myprofile(@RequestParam String email) {
		final BasicResponse basicResponse = new BasicResponse();

		try {
			Map<String, Object> map = new HashMap<>();
			Optional<UserDto> userDto = userService.findUserByEmail(email);

			if (userDto.isPresent()) {
				map.put("user", userDto.get());
				basicResponse.data = "success";
				basicResponse.object = map;
				basicResponse.status = true;
			} else {
				basicResponse.data = "fail";
				basicResponse.status = true;
			}

		} catch (Exception e) {
			basicResponse.status = true;
			basicResponse.data = "error";
			e.printStackTrace();
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}
	
	@PostMapping("/updatemyprofile")
	@ApiOperation(value = "마이페이지 정보 수정")
	public Object updateProfile(@RequestBody UserRequest userRequest) {
		final BasicResponse basicResponse = new BasicResponse();
		try {
			Map<String, Object> map = new HashMap<>();
			Optional<UserDto> userDto = userService.findUserByEmail(userRequest.getEmail());
			if(userDto.isPresent()) {
				UserDto updatUserDto = new UserDto(userRequest);
				UserDto result = userService.saveUser(updatUserDto);
				map.put("user", result);
				basicResponse.object = map;
				basicResponse.data = "success";
				basicResponse.status = true;
			} else {
				basicResponse.data = "fail";
				basicResponse.status = true;
			}
		} catch (Exception e) {
			basicResponse.object = null;
			basicResponse.data = "error";
			basicResponse.status = true;
			e.printStackTrace();
		} finally {
			return new ResponseEntity(basicResponse, HttpStatus.OK);
		}
	}
	

	@PostMapping("/changeprofileimg")
	@ApiOperation(value = "프로필 이미지 수정")
	public Object changeprofileimg(@RequestParam String email, @RequestBody MultipartFile mf) {
		final BasicResponse basicResponse = new BasicResponse();
		try {
			Map<String, Object> map = new HashMap<>();
			List<MultipartFile> mfl = new ArrayList<>();
			mfl.add(mf);
			UserDto userDto = userService.changeProfileImg(email, mfl);
			map.put("user", userDto);
			basicResponse.object = map;
			basicResponse.data = "success";
			basicResponse.status = true;
		} catch (Exception e) {
			basicResponse.data = "error";
			basicResponse.status = true;
			e.printStackTrace();
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}

	@PostMapping("/findpassword")
	@ApiOperation(value = "비밀번호 변경을 위한 이메일 발송")
	public Object findPassword(@RequestParam(required = true) String email) {
		final BasicResponse basicResponse = new BasicResponse();
		try {
			Optional<UserDto> userDto = userService.findUserByEmail(email);
			if (userDto.isPresent()) {
				String key = mailService.mailSend(userDto);
				if(key != null) {
					basicResponse.status = true;
					basicResponse.data = key;
				} else {
					basicResponse.status = true;
					basicResponse.data = "fail";
				}
			} else {
				basicResponse.status = true;
				basicResponse.data = "fail";
			}
		} catch (Exception e) {
			e.printStackTrace();
			basicResponse.status = true;
			basicResponse.data = "error";
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}
	
	@PostMapping("/changepassword")
	@ApiOperation(value = "비밀번호 변경")
	public Object changePassword(@RequestParam(required = true) String email, @RequestParam(required = true) String password) {
		final BasicResponse basicResponse = new BasicResponse();
		try {
			Optional<UserDto> userDto = userService.findUserByEmail(email);
			if(userDto.isPresent()) {
				userDto.get().setPassword(password);
				if(userService.saveUser(userDto.get()) != null) {
					basicResponse.status = true;
					basicResponse.data = "success";
					basicResponse.object = userDto;
				} else {
					basicResponse.status = true;
					basicResponse.data = "fail";
				}
			} else {
				basicResponse.status = true;
				basicResponse.data = "fail";
			}
		} catch (Exception e) {
			basicResponse.status = true;
			basicResponse.data = "error";
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}
	
	@PostMapping("/total")
	@ApiOperation(value = "총 달린거리")
	public Object total(@RequestParam(required = true) String email) {
		final BasicResponse basicResponse = new BasicResponse();
		try {
			Optional<CalendarDto> total = calendarService.getTotalDist(email);
			if(total.isPresent()) {
				Map<String, String> map = new HashMap<>();
				map.put("total", String.valueOf(total.get().getTotaldist()));
				map.put("count", String.valueOf(total.get().getGoaldist()));
				basicResponse.status = true;
				basicResponse.data = "success";
				basicResponse.object = map;
			} else {
				basicResponse.status = true;
				basicResponse.data = "fail";
			}
		} catch (Exception e) {
			basicResponse.status = true;
			basicResponse.data = "error";
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}
	
}
