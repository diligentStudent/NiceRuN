package com.nicerun.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nicerun.dao.TrackInfoDao;
import com.nicerun.dto.MyTrackDto;
import com.nicerun.dto.TrackInfoDto;

@Service
public class TrackInfoService {

	@Autowired
	TrackInfoDao trackInfoDao;

	public TrackInfoDto saveTrackInfoDto(TrackInfoDto trackInfoDto) throws Exception {
		return trackInfoDao.save(trackInfoDto);
	}
	
	@Transactional
	public List<TrackInfoDto> findMyTrackByEmail(String email, String day){
		return trackInfoDao.findMyTrackByEmailAndCreateatContaining(email, day);
	}
}
