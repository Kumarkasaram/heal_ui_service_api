package com.heal.dashboard.service.beans;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
public class BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private int id;

	@CreatedDate
	@JsonIgnore
	private LocalDateTime createdTime;
	@JsonIgnore
	@LastModifiedDate
	private LocalDateTime updatedTime;
}
