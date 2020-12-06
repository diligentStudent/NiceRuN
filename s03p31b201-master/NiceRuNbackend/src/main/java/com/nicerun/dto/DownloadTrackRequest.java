package com.nicerun.dto;

import lombok.Data;

@Data
public class DownloadTrackRequest {
	String email;
	int mytrackid;
	int type;
}
