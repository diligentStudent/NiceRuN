package com.nicerun.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nicerun.dao.MyTrackDao;
import com.nicerun.dao.SNSDao;
import com.nicerun.dto.MyTrackDto;
import com.nicerun.dto.SNSDto;
import com.nicerun.dto.TrackDto;
import com.nicerun.request.SNSRequest;
import com.nicerun.service.TrackService.FindAllTrackType;

@Service
public class SNSService {
	@Autowired
	SNSDao snsDao;
	@Autowired
	MyTrackService myTrackService;
	@Autowired
	TrackService trackService;

	public SNSDto saveSNS(String comment, MyTrackDto mtd) {
		SNSDto snsDto = new SNSDto(comment);
		snsDto.setMytrackid(mtd.getMytrackid());
		if (mtd.isIsupload()) {
			return null;
		} else {
			return snsDao.save(snsDto);
		}
	}

	public List<SNSDto> getAllSNS() {
		return snsDao.findAll();
	}

	public List<SNSDto> getSamePlaceSNS(String location) {
		List<SNSDto> result = new ArrayList<>();
		List<SNSDto> snsList = snsDao.findAllOrderByCreatedatDesc();

		for (Iterator<SNSDto> iter = snsList.iterator(); iter.hasNext();) {
			SNSDto snsDto = iter.next();
			System.out.println("mytrackid  ==> "+snsDto.getMytrackid());
			MyTrackDto mtd = myTrackService.FindMyTrackDtoByMyTrackid(snsDto.getMytrackid());
			TrackDto td = trackService.findTrackByTrackID(mtd.getTrackid());
			if (!td.getLocation().equals(location)) {
				continue;
			}
			result.add(snsDto);
		}

		return result;
	}
}
