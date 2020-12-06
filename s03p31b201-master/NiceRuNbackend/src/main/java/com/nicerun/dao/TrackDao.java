package com.nicerun.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nicerun.dto.TrackDto;

@Repository
public interface TrackDao extends JpaRepository<TrackDto, String> {
	public Optional<TrackDto> findTrackDtoByFilename(String Filename);

	public TrackDto findTrackDtoByTrackid(String trackid);
}
