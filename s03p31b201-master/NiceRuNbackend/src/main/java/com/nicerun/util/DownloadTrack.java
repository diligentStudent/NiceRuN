package com.nicerun.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nicerun.dto.DownloadTrackRequest;
import com.nicerun.dto.MyTrackDto;
import com.nicerun.service.MyTrackService;

public class DownloadTrack {

	public static enum FromWhere {
		sns, owntrack
	}
}
