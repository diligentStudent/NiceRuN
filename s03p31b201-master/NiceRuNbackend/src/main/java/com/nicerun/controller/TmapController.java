package com.nicerun.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.nicerun.dto.TmapDto;
import com.nicerun.util.BasicResponse;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = { "*" }, maxAge = 6000)
@RestController
@RequestMapping("/tmap")
public class TmapController {

	@Autowired
	private RestTemplate restTemplate;

	private TmapDto tmapDto;

	private String host = "https://apis.openapi.sk.com";

	@PostMapping("/findroute")
	@ApiOperation(value = "시작지점 찾는 함수")
	public Object getText() {
		final BasicResponse basicResponse = new BasicResponse();
		// 서버로 요청할 Header
		HttpHeaders headers = new HttpHeaders();
		headers.add("appKey", "l7xxb9b86d78bbd04a24b067b17932901d82");
		headers.add("Accept-Language", "ko");
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

		// 서버로 요청할 Body
		// 현제 추가한 데이터들은 모두 필수 데이터이다
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("startX", "127.305032");
		params.add("startY", "36.344931");
		params.add("endX", "127.00160213");
		params.add("endY", "37.57081522");
		params.add("startName", "출발지점");
		params.add("endName", "도착지점");

		HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<MultiValueMap<String, String>>(params, headers);
		try {
			// RestTemplate을 이용해 카카오페이에 데이터를 보내는 방법.
			// post방식으로 HOST + "/v1/payment/ready"에 body(header+body)정보를 보낸다
			// 정보를 보내고 요청이 성공적으로 이루어지면 카카오페이에서 응답정보를 보내준다.
			// kakaoPayReadyDto.class는 응답을 받는 객체를 설정한 것이다.
			tmapDto = restTemplate.postForObject(new URI(host + "/tmap/routes/pedestrian?version=1&format=json"), body,
					TmapDto.class);
			System.out.println(tmapDto.getDiscription());
			basicResponse.data = "success";
			basicResponse.object = tmapDto;
			basicResponse.status = true;
		} catch (Exception e) {
			basicResponse.data = "fail";
			basicResponse.object = null;
			basicResponse.status = true;
			e.printStackTrace();

		} finally {
			return basicResponse;
		}

	}

	public void getReady() {

		host = "https://apis.openapi.sk.com/tmap/routes/pedestrian";
		// 서버로 요청할 Header
		HttpHeaders headers = new HttpHeaders();
		headers.add("appKey", "l7xxb9b86d78bbd04a24b067b17932901d82");
		headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
	}
}
