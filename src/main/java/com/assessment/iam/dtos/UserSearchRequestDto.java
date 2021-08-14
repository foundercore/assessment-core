package com.assessment.iam.dtos;

import com.assessment.common.model.PaginationModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchRequestDto extends PaginationModel {
	private boolean enabled = true;
	private String userId;
}
