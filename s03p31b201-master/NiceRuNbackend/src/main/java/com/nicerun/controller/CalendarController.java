package com.nicerun.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nicerun.dto.CalendarDto;
import com.nicerun.dto.TrackInfoDto;
import com.nicerun.response.CalendarResponse;
import com.nicerun.service.CalendarService;
import com.nicerun.service.MyTrackService;
import com.nicerun.service.TrackInfoService;
import com.nicerun.util.BasicResponse;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = { "*" }, maxAge = 6000)
@RestController
@RequestMapping("/calendar")
public class CalendarController {

	@Autowired
	CalendarService calendarService;

	@Autowired
	MyTrackService myTrackService;

	@Autowired
	TrackInfoService trackInfoService;

	@GetMapping("/getdaytrack")
	@ApiOperation(value = "해당 날짜를 누르면 그날의 운동 기록을 보여준다")
	public Object getDayTrack(@RequestParam(required = true) String day, @RequestParam(required = true) String email) {
		BasicResponse basicResponse = new BasicResponse();
		try {

			Map<String, Object> map = new HashMap<>();
			List<TrackInfoDto> result = new ArrayList<>();
			List<Boolean> download = new ArrayList<>();
			List<Boolean> upload = new ArrayList<>();

			List<TrackInfoDto> trackinfo = trackInfoService.findMyTrackByEmail(email, day);
			Optional<CalendarDto> calendar = calendarService.findCalenarEmailAndDay(email, day);

			if (calendar.isPresent()) {
				for (Iterator<TrackInfoDto> iter = trackinfo.iterator(); iter.hasNext();) {
					TrackInfoDto temp = iter.next();
					TrackInfoDto info = new TrackInfoDto();
					info.setTrackinfoid(temp.getTrackinfoid());
					info.setCreateat(temp.getCreateat());
					info.setDist(temp.getDist());
					info.setRunningtime(temp.getRunningtime());
					info.setKcal(temp.getKcal());
					info.setSpeed(temp.getSpeed());
					info.setTrackimg(temp.getTrackimg());
					info.setMytrackid(temp.getMytrackid());
					download.add(temp.getMytrack().isIsdownload());
					upload.add(temp.getMytrack().isIsupload());
					result.add(info);
				}
				map.put("trackinfoList", result);
				map.put("downloadList", download);
				map.put("uploadList", upload);
				map.put("calendar", calendar.get());
				basicResponse.object = map;
				basicResponse.status = true;
				basicResponse.data = "success";
			} else {
				basicResponse.status = true;
				basicResponse.data = "fail";
			}

		} catch (Exception e) {
			basicResponse.data = "error";
			basicResponse.status = true;
			e.printStackTrace();
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}

	@PostMapping("/getmonthtrack")
	@ApiOperation(value = "캘랜더에서 운동 성공 여부 표시")
	public Object getMonthTrack(@RequestParam(required = true) String date,
			@RequestParam(required = true) String email) {
		BasicResponse basicResponse = new BasicResponse();
		try {
			Map<String, Object> map = new HashMap<>();
			StringTokenizer st = new StringTokenizer(date, "-");
			int year = Integer.parseInt(st.nextToken());
			int month = Integer.parseInt(st.nextToken());
			Calendar cal = Calendar.getInstance();
			cal.set(year, month - 1, 1);
			int size = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

			List<CalendarResponse> result = new ArrayList<>();
			for (int i = 1; i <= size; i++) {
				CalendarResponse index = new CalendarResponse();
				index.setDate(date + "-" + i);
				index.setFlag(0);
				result.add(index);
			}

			List<CalendarDto> calendarDto = calendarService.getMonthTrack(email, date);

			if (calendarDto.isEmpty()) {
				basicResponse.status = true;
				basicResponse.data = "fail";
				map.put("daycalendar", result);
				basicResponse.object = map;
			} else {
				for (Iterator<CalendarDto> iter = calendarDto.iterator(); iter.hasNext();) {
					CalendarDto calendar = iter.next();

					boolean issuccess = calendar.isIssucceeded();
					int goal = 1;
					if (issuccess)
						goal = 2;

					String today = calendar.getToday().toString();
					st = new StringTokenizer(today, "-");
					String todayYear = st.nextToken();
					String todayMonth = st.nextToken();
					String todayDay = st.nextToken();
					result.get(Integer.parseInt(todayDay) - 1).setFlag(goal);
				}
				map.put("daycalendar", result);
				basicResponse.status = true;
				basicResponse.data = "success";
				basicResponse.object = map;
			}

		} catch (Exception e) {
			e.printStackTrace();
			basicResponse.status = true;
			basicResponse.data = "error";
		} finally {
			return new ResponseEntity<>(basicResponse, HttpStatus.OK);
		}
	}

}
