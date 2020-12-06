package com.nicerun.dto;

import java.sql.Date;
import java.text.SimpleDateFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.nicerun.request.TrackRequest;

import lombok.Data;

@Entity
@Data
@Table(name = "calendar")
public class CalendarDto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int calendarid;
	String email;
	boolean issucceeded;
	double totaldist;
	int goaldist;
	Date today;

	public CalendarDto() {

	}

	public CalendarDto(TrackRequest tr) {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		Date sqlDate = java.sql.Date.valueOf(format1.format(new java.util.Date()));
		this.email = tr.getEmail();
		this.today = sqlDate;
	}

}
