package com.assessment.common;

import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.util.Map;

public interface IFileDecoder {
    boolean hasNext() throws IOException, CsvValidationException;
    Map<String, Object> next();
    void close() throws IOException;
}
