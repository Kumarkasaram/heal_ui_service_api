package com.heal.dashboard.service.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountBean extends BaseEntity {
    private int accountId;
    @JsonIgnore
    private int status;
    private int timezoneMilli;
    private int timeOffset;
    private String name;
    private String identifier;
    private String timeZoneString;
    private LocalDateTime updatedTimeString;
    private String updatedBy;
    private String abbreviation;
    private String offsetName;
    private String userDetailsId;
  }
