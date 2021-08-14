package com.assessment.iam.dtos;

import java.util.List;

import lombok.Data;

@Data
public class UserPaginatedResponse {
	private List<UserResponseDto> users;
	private int pageSize;
	private int pageNumber;
	private long totalRecords;
}
