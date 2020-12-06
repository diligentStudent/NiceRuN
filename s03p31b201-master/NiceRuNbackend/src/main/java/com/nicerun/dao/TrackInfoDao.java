package com.nicerun.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nicerun.dto.TrackInfoDto;

@Repository
public interface TrackInfoDao extends JpaRepository<TrackInfoDto, String> {
	
	@Query(value = "select * "
			+ "from trackinfo a "
			+ "left join mytrack b "
			+ "on a.mytrackid = b.mytrackid "
			+ "where b.email = :email "
			+ "and a.createat like :day% "
			+ "order by trackinfoid desc" , nativeQuery = true)
	List<TrackInfoDto> findMyTrackByEmailAndCreateatContaining(@Param("email") String email, @Param("day") String day);

}
