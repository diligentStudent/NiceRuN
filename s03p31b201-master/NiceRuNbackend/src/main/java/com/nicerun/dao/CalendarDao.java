package com.nicerun.dao;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nicerun.dto.CalendarDto;

@Repository
public interface CalendarDao extends JpaRepository<CalendarDto, String> {
	public Optional<CalendarDto> findCalendarDtoByEmailAndToday(String email, Date today);

	@Query(value = "select * "
			+ "from calendar "
			+ "where email = :email "
			+ "and date_format(today, '%Y-%m') = :date"
			, nativeQuery = true)
	public List<CalendarDto> findByEmailAndTodayStartsWith(@Param(value = "email") String email, @Param(value = "date") String date);

	@Query(value = "select "
			+ "email, sum(totaldist) as totaldist, max(calendarid) as calendarid, "
			+ "count(goaldist) as goaldist, max(today) as today, count(issucceeded) as issucceeded "
	        + "from calendar " 
			+ "where email = :email"
			, nativeQuery = true)
	public Optional<CalendarDto> getTotalDist(@Param(value = "email") String email);
}
