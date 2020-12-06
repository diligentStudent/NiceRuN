package com.nicerun.util;

import java.util.Optional;
import java.util.Random;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.nicerun.dto.UserDto;

@Service
public class MailService {

	@Autowired
	private JavaMailSender mailSender;
	private static final String FROM_ADDRESS = "popom.m99@gmail.com";
	private static final String TITLE = "Nice Run 비밀번호 변경 인증코드입니다.";

	public String mailSend(Optional<UserDto> userDto) {

		try {
			String key = makekey(4, 1);
			MimeMessage message = mailSender.createMimeMessage();
			message.addRecipient(RecipientType.TO, new InternetAddress(userDto.get().getEmail()));
			message.setFrom(FROM_ADDRESS);
			message.setSubject(TITLE);
			
			StringBuffer sb = new StringBuffer();
			
			sb.append("<!DOCTYPE html>");
			sb.append("<head>");
			sb.append("<meta charset=utf-8/>");
			sb.append("<title>Nicd Run</title>");
			sb.append("</head>");
			sb.append("<body style=\"font-family: Arial, '맑은 고딕', 'Malgun Gothic', Dotum, '돋움',sans-serif, Helvetica; font-size:12px; color:#464646; line-height:0;\">");
			sb.append("<div style=\"width:100%; padding:20px 0;\">");
			sb.append("<div style=\"width:600px font-size:12px; color:#636363; background-color:#f4f4f4; line-height:1.3em; padding:20px 30px; margin-top:10px;\">");
			sb.append("<div style=\"font-size:13px padding:30px; text-align:center;\">");
            sb.append("<p>당신의 러닝 친구 Nice RUN입니다.</p>");
            sb.append("<br />");
            sb.append("<p style=\"line-height:1.6em;\">" + userDto.get().getLastname() + " " + userDto.get().getFirstname() + "(" + userDto.get().getEmail() + ")" + "님 인증코드를 입력해주세요.</p>");
            sb.append("<br />");
			sb.append("<div style=\"background:lightblue; border:1px solid black; padding:5px 5px 5px 5px; margin-bottom:10px;\">");
			sb.append("<p style=\"font-size:17px line-height:1.6em; color:black;\">");
			sb.append("인증 코드 : " + key);
			sb.append("</div>");
			sb.append("</p>");
			sb.append("<p>저희 사이트를 이용해 주셔서 감사합니다.<p/> <p>다른 궁금하신 사항은  웹사이트(<a href=\"http://i3b201.p.ssafy.io/\" target=\"_blank\">www.NiceRun.co.kr</a>)에서 확인해 주세요.</p>");
			sb.append("<p>");
			sb.append("Copyrjght 2020 1쏠4커 All rights reserved.</p>");
			sb.append("</div>");
			sb.append("</div>");
			sb.append("</div>");
			sb.append("</body>");
			sb.append("</html>");

            message.setContent(sb.toString(), "text/html; charset=utf-8");

            mailSender.send(message);


			return key;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public String makekey(int len, int dupCd) {

		Random rand = new Random();
		String numStr = "";

		for (int i = 0; i < len; i++) {

			String ran = Integer.toString(rand.nextInt(10));

			if (dupCd == 1) {
				// 중복 허용
				numStr += ran;
			} else if (dupCd == 2) {
				// 중복을 허용하지 않을시
				if (!numStr.contains(ran)) {
					numStr += ran;
				} else {
					//중복
					i -= 1;
				}
			}
		}
		return numStr;
	}

}
