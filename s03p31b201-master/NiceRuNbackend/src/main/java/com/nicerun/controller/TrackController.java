package com.nicerun.controller;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.zone.ZoneOffsetTransitionRule.TimeDefinition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nicerun.dto.CalendarDto;
import com.nicerun.dto.MyTrackDto;
import com.nicerun.dto.SNSDto;
import com.nicerun.dto.TrackDto;
import com.nicerun.dto.TrackInfoDto;
import com.nicerun.dto.UserDto;
import com.nicerun.request.TrackRequest;
import com.nicerun.service.CalendarService;
import com.nicerun.service.MyTrackService;
import com.nicerun.service.SNSService;
import com.nicerun.service.TrackInfoService;
import com.nicerun.service.TrackService;
import com.nicerun.service.TrackService.FindAllTrackType;
import com.nicerun.service.UserService;
import com.nicerun.util.BasicResponse;
import com.nicerun.util.SaveFile;
import com.nicerun.util.SaveFile.FileType;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = { "*" }, maxAge = 6000)
@RestController
@RequestMapping("/track")
public class TrackController {

	@Autowired
	TrackService trackService;
	@Autowired
	MyTrackService myTrackService;
	@Autowired
	TrackInfoService trackInfoService;
	@Autowired
	UserService userService;
	@Autowired
	CalendarService calendarService;
	@Autowired
	SNSService sNSService;

	@PostMapping("/savetrack")
	@ApiOperation(value = "트랙 저장하기 isshared가 true일경우 공유된 트랙을 뛴것임 그럴경우 트랙을 저장하지 않음")
	public Object saveTrack(TrackRequest trackRequest, @RequestPart MultipartFile filename,
			@RequestPart MultipartFile trackimg) {// trackRequest와 filename그리고 trackimg를 requestParam으로 받음
		final BasicResponse basicResponse = new BasicResponse();
		try {
			List<MultipartFile> mfj = new ArrayList<>();
			List<MultipartFile> mfi = new ArrayList<>();
			Map<String, Object> map = new HashMap<>();
			MyTrackDto myTrackDto = null;
			TrackDto trackDto = new TrackDto(trackRequest);
			mfj.add(filename);
			mfi.add(trackimg);
			String jsonname = null;
			String imgname = null;
			// myTrack에도 저장 해주기위해 유저를 확인
			Optional<UserDto> opuserDto = userService.findUserByEmail(trackRequest.getEmail());
			// 이미지와 파일 저장
			// 공유한 트랙 이라면 이미지만 저장
			if (trackRequest.isSnsdownload()) {
				String path = new File("").getAbsolutePath();
				System.out.println(path + "\\track\\" + filename.getOriginalFilename());
				trackDto = trackService.findTrackByFilename(path + "\\track\\" + filename.getOriginalFilename()).get();
				System.out.println(trackDto.getFilename());
				myTrackDto = myTrackService.findMyTrackDto(trackRequest.getEmail(), trackDto.getTrackid()).get();
			} else {// 존재하지 않는 파일이라면 파일과 이미지 둘다 새로 저장
				jsonname = SaveFile.SaveFIle(mfj, FileType.JSONFile).get(0);
				imgname = SaveFile.SaveFIle(mfi, FileType.Image).get(0);
				trackDto = trackService.saveTrack(trackDto, jsonname, imgname);

				if (opuserDto.isPresent()) {
					// myTrack저장
					myTrackDto = new MyTrackDto(opuserDto.get().getEmail(), trackDto.getTrackid());
					myTrackDto = myTrackService.saveMyTrackDto(myTrackDto);

				} else {
//					basicResponse.object = null;
					basicResponse.data = "fail";
					basicResponse.status = true;
					return new ResponseEntity(basicResponse, HttpStatus.OK);
				}
			}
			// myTrackinfo 저장하기
			TrackInfoDto trackInfoDto = null;
			if (myTrackDto != null) {
				trackInfoDto = new TrackInfoDto(trackRequest);
				trackInfoDto.setMytrackid(myTrackDto.getMytrackid());
				if (imgname == null) {
					imgname = SaveFile.SaveFIle(mfi, FileType.Image).get(0);
				}
				trackInfoDto.setTrackimg(imgname);
				trackInfoDto = trackInfoService.saveTrackInfoDto(trackInfoDto);
			} else {
//				basicResponse.object = null;
				basicResponse.data = "fail";
				basicResponse.status = true;
				return new ResponseEntity(basicResponse, HttpStatus.OK);
			}

			// calendar 삽입/업데이트
			Optional<CalendarDto> opcalendarDto = calendarService
					.findCalendarDtoByEmailAndCreatedat(trackRequest.getEmail());
			CalendarDto calendarDto = null;
			if (opcalendarDto.isPresent()) {// 오늘 이미 뛰었다면 update
				calendarDto = opcalendarDto.get();
				double totalDist = calendarDto.getTotaldist() + trackRequest.getDist();

				calendarDto.setTotaldist(totalDist);
				calendarDto = calendarService.saveCalendarDto(calendarDto, totalDist);
			} else {// 오늘 처음 뛴다면 save
				calendarDto = new CalendarDto(trackRequest);
				UserDto userDto = opuserDto.get();
				calendarDto.setGoaldist(userDto.getGoaldist());
				calendarDto = calendarService.saveCalendarDto(calendarDto, trackRequest.getDist());
			}
			map.put("calendar", calendarDto);
			basicResponse.object = map;
			basicResponse.data = "success";
			basicResponse.status = true;

		} catch (Exception e) {
			basicResponse.object = null;
			basicResponse.data = "error";
			basicResponse.status = true;
			e.printStackTrace();
		} finally {
			return new ResponseEntity(basicResponse, HttpStatus.OK);
		}
	}

	@PostMapping("/showtrack")
	@ApiOperation(value = "저장한 트랙 보여주는 함수")
	public Object ShowTrack(@RequestParam String email) {
		final BasicResponse basicResponse = new BasicResponse();
		List<TrackDto> trackList = null;

		Map<String, Object> result = new HashMap<>();
		try {
			List<MyTrackDto> myTrackList = myTrackService.FindAllMyTrackDtoByEmail(email);
			trackList = trackService.findAllTrackByTrackid(myTrackList, FindAllTrackType.savedtrack);

			if (trackList != null && !trackList.isEmpty()) {
				result.put("trackList", trackList);
				basicResponse.data = "success";
				basicResponse.object = result;
			} else {
				basicResponse.data = "fail";
			}
		} catch (Exception e) {
			basicResponse.object = null;
			basicResponse.data = "error";
			basicResponse.status = true;
			e.printStackTrace();
			System.out.println("location: com.nicerun.controller => TrackController => ShowTrack()");
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}

	@PostMapping("/")
	@ApiOperation(value = "test")
	public void test(@RequestParam int trackid) {
		Optional<MyTrackDto> opmtd = myTrackService.test(trackid);
		MyTrackDto mtd = opmtd.get();
		List<TrackInfoDto> lti = mtd.getTrackinfos();
		for (Iterator<TrackInfoDto> iter = lti.iterator(); iter.hasNext();) {
			TrackInfoDto tid = iter.next();
			System.out.println(tid.getTrackinfoid());
			System.out.println(new Date().toString());
		}
	}

	@GetMapping("/recomtrack")
	@ApiOperation(value = "추천 트랙 보여주는 함수")
	public Object recomTrack(@RequestParam String email, @RequestParam String location) {
		final BasicResponse basicResponse = new BasicResponse();
		Map<String, Object> result = new HashMap<>();
		List<MyTrackDto> mtdList = null;
		List<TrackDto> tdList = null;
		List<SNSDto> snsList = null;
		List<Boolean> savedList = null;
		try {
			mtdList = new ArrayList<>();
			tdList = new ArrayList<>();
			snsList = new ArrayList<>();
			savedList = new ArrayList<>();

			snsList = sNSService.getSamePlaceSNS(location);
			System.out.println("size==>" + snsList.size());
			if (snsList.isEmpty()) {
				basicResponse.data = "fail";
				basicResponse.status = true;
			} else {
				for (Iterator<SNSDto> iter = snsList.iterator(); iter.hasNext();) {
					SNSDto snsDto = iter.next();
					MyTrackDto mtd = new MyTrackDto(myTrackService.FindMyTrackDtoByMyTrackid(snsDto.getMytrackid()));
					if (myTrackService.findMyTrackDto(email, mtd.getTrackid()).isPresent()) {
						savedList.add(true);
					} else {
						savedList.add(false);
					}
					mtdList.add(mtd);
				}
				tdList = new ArrayList<>();
				tdList = trackService.findAllTrackByTrackid(mtdList, FindAllTrackType.all);

				result.put("mytrackList", mtdList);
				result.put("trackList", tdList);
				result.put("snsList", snsList);
				result.put("savedList", savedList);

				basicResponse.object = result;
				basicResponse.data = "success";
				basicResponse.status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			basicResponse.data = "fail";
			basicResponse.status = true;
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}

	@PostMapping("/gettrackfile")
	@ApiOperation(value = "네비게이션 안내를 받을 파일을 받아온다")
	public Object getTrackFile(@RequestParam(required = true) String trackid) {
		final BasicResponse basicResponse = new BasicResponse();
		try {
			Optional<TrackDto> track = trackService.findTrackDtoByTrackid(trackid);
			Map<String, Object> map = new HashMap<>();
			if (track.isPresent()) {
				String fileName = track.get().getFilename();
				String imageName = track.get().getTrackimg();
				File img = new File(imageName);
				File file = new File(fileName);
				fileName = file.getName();
				imageName = img.getName();
				track.get().setFilename(fileName);
				track.get().setTrackimg(imageName);
				map.put("track", track.get());
				basicResponse.status = true;
				basicResponse.data = "success";
				basicResponse.object = map;
			} else {
				basicResponse.status = true;
				basicResponse.data = "fail";
//				basicResponse.object = false;
				return new ResponseEntity<>(basicResponse, HttpStatus.OK);
			}
		} catch (Exception e) {
			e.printStackTrace();
			basicResponse.status = true;
			basicResponse.data = "error";
//			basicResponse.object = false;
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}

}
