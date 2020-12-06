package com.nicerun.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nicerun.dao.MyTrackDao;
import com.nicerun.dto.MyTrackDto;

@Service
public class MyTrackService {
	@Autowired
	MyTrackDao myTrackDao;

	@Transactional
	public MyTrackDto saveMyTrackDto(MyTrackDto myTrackDto) throws Exception {
		return myTrackDao.save(myTrackDto);
	}

	public Optional<MyTrackDto> findMyTrackDto(String email, String trackid) throws Exception {
		return myTrackDao.findMyTrackDtoByEmailAndTrackid(email, trackid);
	}

	public List<MyTrackDto> FindAllMyTrackDtoByEmail(String email) throws Exception {
		return myTrackDao.findAllMyTrackDtoByEmailOrderByMytrackidDesc(email);
	}

	public MyTrackDto FindMyTrackDtoByMyTrackid(int myTrackId) {
		return myTrackDao.findMyTrackDtoByMytrackid(myTrackId);
	}

	@Transactional
	public Optional<MyTrackDto> test(int mytrackid) {
		return myTrackDao.findById(mytrackid);
	}
	
}
