package com.assessment.common;

import com.assessment.exception.UnexpectedException;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class CsvFileDecoder implements IFileDecoder, Closeable {

    private CSVReader csvReader;
    private String [] headers = null;
    private Map<String, Object> record = null;
    private boolean ignoreEmptyLines = true;

    public CsvFileDecoder(String file) throws IOException, CsvValidationException {
        this(new File(file), ',', true);
    }

    public CsvFileDecoder(File file, char delimiter, boolean fileHasHeaders) throws IOException, CsvValidationException {
        if (file.isFile() && file.exists()){

            Reader reader = Files.newBufferedReader(Paths.get(file.toURI()), StandardCharsets.UTF_8);

            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(delimiter)
                    .withQuoteChar('"')
                    .withStrictQuotes(false)
                    .build();

            this.csvReader = new CSVReaderBuilder(reader)
                    .withSkipLines(0)
                    .withCSVParser(parser)
                    .build();
            if (fileHasHeaders){
                headers = this.csvReader.readNext();
            }
        }else {
            throw new UnexpectedException("File does not exist or a directory");
        }
    }

    @Override
    public void close() throws IOException {
        if (csvReader != null){
            try {
                csvReader.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw e;
            }
        }
    }

    @Override
    public boolean hasNext() throws IOException, CsvValidationException {
        try {
            String [] row = getNextRecord();
            if (row != null){
                record = new LinkedHashMap<>();
                if (headers != null){
                    for (int i = 0; i < headers.length; i++) {
                        record.put(headers[i], row[i]);
                    }
                }else {
                    int index = 0;
                    for (String item: row){
                        record.put(String.valueOf(index), item);
                        index++;
                    }
                }
                return true;
            }
        } catch (IOException | CsvValidationException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        close();
        return false;
    }

    private String[] getNextRecord() throws IOException, CsvValidationException {
        String [] row = null;
        boolean readUntilRecordFoundOrFileEnd = true;
        while (readUntilRecordFoundOrFileEnd){
            row = csvReader.readNext();
            if (row != null && ignoreEmptyLines){
                boolean isEmpty = true;
                for (String item: row){
                    isEmpty = isEmpty && item.trim().isEmpty();
                }
                if (!isEmpty){
                    readUntilRecordFoundOrFileEnd = false;
                }
            }else {
                readUntilRecordFoundOrFileEnd = false;
            }
        }
        return row;
    }

    @Override
    public Map<String, Object> next(){
        return record;
    }
}
