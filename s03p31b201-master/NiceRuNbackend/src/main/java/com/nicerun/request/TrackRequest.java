package com.nicerun.request;

import lombok.Data;

@Data
public class TrackRequest {
	double dist;
	String email;
	String createat;
	double speed;
	int kcal;
	int runningtime;
	boolean snsdownload;
	String location;
}
