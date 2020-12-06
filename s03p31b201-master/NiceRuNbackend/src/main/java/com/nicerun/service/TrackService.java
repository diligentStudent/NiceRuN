package com.nicerun.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nicerun.dao.TrackDao;
import com.nicerun.dto.MyTrackDto;
import com.nicerun.dto.TrackDto;
import com.nicerun.util.SaveFile;
import com.nicerun.util.SaveFile.FileType;

@Service
public class TrackService {

	// savedtrack은 저장한 트랙들을 보여주는 것
	// all은 모든 트랙들을 보여주는것
	public static enum FindAllTrackType {
		savedtrack, all
	}

	@Autowired
	TrackDao trackDao;

	public String saveTrackFile(List<MultipartFile> mfl) throws Exception {
		List<String> result = SaveFile.SaveFIle(mfl, FileType.JSONFile);

		return result.get(0);
	}

	public TrackDto saveTrack(TrackDto trackDto, String trackFileName, String trackImg) throws Exception {
		UUID uuid = UUID.randomUUID();
		trackDto.setTrackid(uuid.toString());
		trackDto.setFilename(trackFileName);
		trackDto.setTrackimg(trackImg);

		return trackDao.save(trackDto);
	}

	public Optional<TrackDto> findTrackByFilename(String Filename) {
		return trackDao.findTrackDtoByFilename(Filename);
	}

	public TrackDto findTrackByTrackID(String trackid) {
		return trackDao.findTrackDtoByTrackid(trackid);
	}

	public List<TrackDto> findAllTrackByTrackid(List<MyTrackDto> mtdList, FindAllTrackType type) {// type이 0이면 저장된트랙만
																									// 보여주고 1이라면 모든
		// 트랙을 다보여줌
		List<TrackDto> result = new ArrayList<>();

		if (type == FindAllTrackType.savedtrack) {
			for (Iterator<MyTrackDto> iter = mtdList.iterator(); iter.hasNext();) {
				MyTrackDto mtd = iter.next();
				if (!mtd.isIsdownload()) {
					continue;
				}
				result.add(trackDao.findTrackDtoByTrackid(mtd.getTrackid()));
			}
		} else if (type == FindAllTrackType.all) {
			for (Iterator<MyTrackDto> iter = mtdList.iterator(); iter.hasNext();) {
				MyTrackDto mtd = iter.next();
				result.add(trackDao.findTrackDtoByTrackid(mtd.getTrackid()));
			}
		}
		return result;
	}
	
	public Optional<TrackDto> findTrackDtoByTrackid(String trackid) {
		return trackDao.findById(trackid);
	}
}
