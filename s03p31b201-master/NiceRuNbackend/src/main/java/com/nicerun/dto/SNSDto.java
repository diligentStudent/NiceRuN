package com.nicerun.dto;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Entity
@Table(name = "sns")
@Data
public class SNSDto {
	@Id
	int mytrackid;
	@JsonFormat(timezone ="Asia/Seoul")
	@Column(name = "createdat", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	Timestamp createdat;
	String comment;

	public SNSDto() {
	}

	public SNSDto(String comment) {
		this.comment = comment;
	}
}
