package com.nicerun.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nicerun.dao.CalendarDao;
import com.nicerun.dto.CalendarDto;

@Service
public class CalendarService {

	@Autowired
	CalendarDao calendarDao;

	public Optional<CalendarDto> findCalendarDtoByEmailAndCreatedat(String email) {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		java.sql.Date sqlDate = java.sql.Date.valueOf(format1.format(new Date()));
		return calendarDao.findCalendarDtoByEmailAndToday(email, sqlDate);
	}

	@Transactional
	public CalendarDto saveCalendarDto(CalendarDto calendarDto, double totalDist) {
		System.out.println(calendarDto.getGoaldist());
		System.out.println(calendarDto.getTotaldist());
		if (totalDist >= calendarDto.getGoaldist() && !calendarDto.isIssucceeded()) {
			calendarDto.setIssucceeded(true);
		}
		calendarDto.setTotaldist(totalDist);
		return calendarDao.save(calendarDto);
	}
	
	public Optional<CalendarDto> findCalenarEmailAndDay(String email, String day) {
		java.sql.Date sqlDate = java.sql.Date.valueOf(day);
		return calendarDao.findCalendarDtoByEmailAndToday(email, sqlDate);
	}

	public List<CalendarDto> getMonthTrack(String email, String date) {
		return calendarDao.findByEmailAndTodayStartsWith(email, date);
	}

	public Optional<CalendarDto> getTotalDist(String email) {
		return calendarDao.getTotalDist(email);
	}

}
