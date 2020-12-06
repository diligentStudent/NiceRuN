package com.nicerun.dto;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nicerun.request.TrackRequest;

import lombok.Data;

@Data
@Entity
@Table(name = "trackinfo")
public class TrackInfoDto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int trackinfoid;
	int mytrackid;
	@JsonFormat(timezone ="Asia/Seoul")
	Timestamp createat;
	double speed;
	int kcal;
	int runningtime;
	double dist;
	String trackimg;

	@ManyToOne
	@JoinColumn(name = "mytrackid", referencedColumnName = "mytrackid", insertable = false, updatable = false)
	MyTrackDto mytrack;

	public TrackInfoDto() {
	}

	public TrackInfoDto(TrackRequest trackRequest) {
		this.dist = trackRequest.getDist();
		this.createat = java.sql.Timestamp.valueOf(trackRequest.getCreateat());
		this.speed = trackRequest.getSpeed();
		this.kcal = trackRequest.getKcal();
		this.runningtime = trackRequest.getRunningtime();
		createat = Timestamp.valueOf(trackRequest.getCreateat());
	}

	public void setMytrack(MyTrackDto myTrackDto) {
		this.mytrack = myTrackDto;
	}
}
