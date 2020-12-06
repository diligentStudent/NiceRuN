package com.nicerun.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.nicerun.request.TrackRequest;

import lombok.Data;

@Entity
@Table(name = "track")
@Data
public class TrackDto {
	@Id
	String trackid; // 트랙의 고유값 PK
	String filename; // 트랙의 JSON file의 이름
	double dist; // 트랙의 길이
	String trackimg; // 트랙의 이미지
	String location;// 트랙이 위치하는 지역

	public TrackDto() {
	}

	public TrackDto(TrackRequest trackRequest) {
		this.dist = trackRequest.getDist();
		this.location = trackRequest.getLocation();
	}
}
