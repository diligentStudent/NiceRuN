package com.nicerun.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nicerun.dto.DownloadTrackRequest;
import com.nicerun.dto.MyTrackDto;
import com.nicerun.dto.SNSDto;
import com.nicerun.dto.TrackDto;
import com.nicerun.request.SNSRequest;
import com.nicerun.service.MyTrackService;
import com.nicerun.service.SNSService;
import com.nicerun.service.TrackService;
import com.nicerun.service.TrackService.FindAllTrackType;
import com.nicerun.util.BasicResponse;
import com.nicerun.util.DownloadTrack.FromWhere;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = { "*" }, maxAge = 6000)
@RestController
@RequestMapping("/sns")
public class SNSController {

	@Autowired
	SNSService sNSService;
	@Autowired
	MyTrackService myTrackService;
	@Autowired
	TrackService trackService;

	@PostMapping("/upload")
	@ApiOperation(value = "\"comment\": \"ㅋㅋㅋㅋ\",\r\n" + "  \"createdat\": \"\",\r\n" + "  \"mytrackid: 1\" " + "\r\n"
			+ "보내주시면 됩니다")
	public Object upload(@RequestBody SNSRequest sNSRequest) {
		final BasicResponse basicResponse = new BasicResponse();
		try {
			SNSDto sd = null;
			Map<String, Object> map = new HashMap<>();
			MyTrackDto mtd = myTrackService.FindMyTrackDtoByMyTrackid(sNSRequest.getMytrackid());
			sd = sNSService.saveSNS(sNSRequest.getComment(), mtd);
			if (sd != null) {
				mtd.setIsupload(true);
				myTrackService.saveMyTrackDto(mtd);
				map.put("sns", sd);
				basicResponse.object = map;
				basicResponse.data = "success";
				basicResponse.status = true;
			} else {
				map.put("sns", sd);
//				basicResponse.object = map;
				basicResponse.data = "fail";
				basicResponse.status = true;
				System.out.println("already exist in sns table");
			}
		} catch (Exception e) {
//			basicResponse.object = null;
			basicResponse.data = "fail";
			basicResponse.status = true;
			e.printStackTrace();
			System.out.println("location: com.nicerun.controller => SNSCtroller => upload()");
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}

	@PostMapping("/downloadtrack")
	@ApiOperation(value = "sns에서 마음에 드는 트랙을 mytrack에 저장 이때 isdownload나 isupload를 둘다 true로 바꿔야함, 0은 자신의 트랙 저장 1은 SNS에서 다운")
	public Object DownloadTrack(@RequestBody DownloadTrackRequest dtr) {
		final BasicResponse basicResponse = new BasicResponse();
		FromWhere fw = null;
		MyTrackDto myTrackDto = null;
		MyTrackDto getOriginTrack = null;

		try {
			Map<String, Object> map = new HashMap<>();
			switch (dtr.getType()) {
			case 0:
				fw = FromWhere.owntrack;
				break;
			case 1:
				fw = FromWhere.sns;
				break;
			}

			switch (fw) {
			case owntrack:
				getOriginTrack = myTrackService.FindMyTrackDtoByMyTrackid(dtr.getMytrackid());
				getOriginTrack.setForOwnTrack();
				myTrackService.saveMyTrackDto(getOriginTrack);
				myTrackDto = new MyTrackDto(getOriginTrack);
				map.put("mytrack", myTrackDto);
				basicResponse.object = map;
				break;

			case sns:
				getOriginTrack = myTrackService.FindMyTrackDtoByMyTrackid(dtr.getMytrackid());
				myTrackDto = new MyTrackDto();
				myTrackDto.setEmail(dtr.getEmail());
				myTrackDto.setForSNS(getOriginTrack);
				myTrackDto = new MyTrackDto(myTrackService.saveMyTrackDto(myTrackDto));
				map.put("mytrack", myTrackDto);
				basicResponse.object = map;
				break;
			}
			basicResponse.data = "success";
			basicResponse.status = true;
		} catch (Exception e) {
//			basicResponse.object = null;
			basicResponse.data = "fail";
			basicResponse.status = true;
			e.printStackTrace();
			System.out.println("location: com.nicerun.controller => SNSCtroller => DownloadTrack()");
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}

	@GetMapping("/showsns")
	@ApiOperation(value = "sns에 저장된 트랙을보여준다 이때 해당트랙을 저장했으면 savedList를 true로 넣어주고 저장이 되지 않았다면 false로 보내준다")
	public Object ShowSNS(@RequestParam String email) {
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

			snsList = sNSService.getAllSNS();
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

			result.put("mytrack", mtdList);
			result.put("track", tdList);
			result.put("sns", snsList);
			result.put("saved", savedList);

			basicResponse.object = result;
			basicResponse.data = "success";
			basicResponse.status = true;
		} catch (Exception e) {
			e.printStackTrace();
			basicResponse.data = "fail";
			basicResponse.status = true;
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}

}
