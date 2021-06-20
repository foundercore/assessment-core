package com.assessment;

import com.assessment.common.CsvFileDecoder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;

public class CsvDecoderTest {
    public static void main(String[] args) {
        String file = "/home/ramraj/Downloads/user_bulk_creation_schema_users.csv";
        try {
            CsvFileDecoder decoder = new CsvFileDecoder(file);
            while (decoder.hasNext()){
                System.out.println(decoder.next());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }
}
