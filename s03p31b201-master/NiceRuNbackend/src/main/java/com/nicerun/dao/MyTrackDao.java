package com.nicerun.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nicerun.dto.MyTrackDto;

public interface MyTrackDao extends JpaRepository<MyTrackDto, Integer> {
	public Optional<MyTrackDto> findMyTrackDtoByEmailAndTrackid(String email, String trackid);

	public List<MyTrackDto> findAllMyTrackDtoByEmailOrderByMytrackidDesc(String email);

	public MyTrackDto findMyTrackDtoByMytrackid(int mytrackid);
}
