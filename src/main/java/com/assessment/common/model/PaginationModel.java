package com.assessment.common.model;

import lombok.Getter;

@Getter
public abstract class PaginationModel {
	/* search option */
	public String searchStringRegexPattern = "";
	public String searchOnColumn = "";

	/* sorting info */
	public String sortColumn = "";
	public String sortOrder = "";

	/* pagination specific */
	public int pageSize = 100;
	public int pageNumber = 1;

}
