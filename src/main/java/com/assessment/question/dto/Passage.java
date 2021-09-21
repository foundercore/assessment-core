package com.assessment.question.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Document(collection = Passage.COLLECTION_NAME)
public class Passage {
    public static final String COLLECTION_NAME = "passage";

    @Id
    private PassageId id;

//    @NotNull
//    @NotEmpty
//    private String topic;

    @NotNull
    @NotEmpty
    private String content;
}
