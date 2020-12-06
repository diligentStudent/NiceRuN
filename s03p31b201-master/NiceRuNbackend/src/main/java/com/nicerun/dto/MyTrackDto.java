package com.nicerun.dto;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Entity
@Table(name = "mytrack")
@Data
public class MyTrackDto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int mytrackid;
	String email;
	String trackid;
	boolean isdownload; // true면 저장한트랙에서 보여줄 트랙 false면 거른다
	boolean isupload; // true면 sns에 올라갔다는 의미 따라서 true라면 공유를 하면 안됨 근데 sns에서 다운로드한건 이미 sns에 올라갔다는 의미 이므로
						// 처음 DB에 저장할때부터
						// isupload가 true여야함
	@JsonFormat(timezone ="Asia/Seoul")
	Timestamp sharedtime;

	@OneToMany(mappedBy = "mytrack")
	List<TrackInfoDto> trackinfos = new ArrayList<>();

	public MyTrackDto() {

	}

	public MyTrackDto(String email, String trackid) {
		this.email = email;
		this.trackid = trackid;
	}

	public MyTrackDto(MyTrackDto mtd) {
		this.email = mtd.getEmail();
		this.isdownload = mtd.isIsdownload();
		this.isupload = mtd.isIsupload();
		this.mytrackid = mtd.getMytrackid();
		this.sharedtime = mtd.getSharedtime();
		this.trackid = mtd.getTrackid();

	}

	public void setForSNS(MyTrackDto mtd) {
		this.isdownload = true;
		this.isupload = true;
		this.trackid = mtd.trackid;

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = format.format(new java.util.Date());
		this.sharedtime = Timestamp.valueOf(time);
	}

	public void setForOwnTrack() {
		this.isdownload = true;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = format.format(new java.util.Date());
		this.sharedtime = Timestamp.valueOf(time);
	}
}
