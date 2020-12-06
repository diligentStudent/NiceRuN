package com.nicerun.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nicerun.dto.SNSDto;

@Repository
public interface SNSDao extends JpaRepository<SNSDto, String> {

	@Query(value = "select * from sns order by createdat desc",nativeQuery = true)
	public List<SNSDto> findAllOrderByCreatedatDesc();
}
