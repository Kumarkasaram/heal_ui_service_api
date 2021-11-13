package com.heal.dashboard.service.beans;



import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessBean extends BaseEntity {

    private String accessDetails;
    private String userIdentifier;
}
