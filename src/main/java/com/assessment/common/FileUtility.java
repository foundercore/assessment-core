package com.assessment.common;

import org.springframework.web.multipart.MultipartFile;

import com.assessment.exception.UnexpectedException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class FileUtility {

    public static String readDataFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
    }

    public static String readDataFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    public static void createAndWrite(File file, String data) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream outputStream = new FileOutputStream(file.getAbsoluteFile());
        outputStream.write(data.getBytes());
        outputStream.close();
    }

    public static void deleteDirectory(File file) throws IOException {
        if (file.isDirectory()) {
            delete(file);
        }
    }

    public static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            if (Objects.requireNonNull(file.list()).length == 0) {
                file.delete();
            } else {
                for (String temp : Objects.requireNonNull(file.list())) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }
                if (Objects.requireNonNull(file.list()).length == 0) {
                    file.delete();
                }
            }
        } else {
            file.delete();
        }
    }

    public static List<File> listOnlyDirs(File file, boolean recursive) {
        return null;
    }

    public static void saveFile(MultipartFile file, String fileName) {
        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();

                String formattedFileName = fileName;

                File serverFile = new File(formattedFileName);

                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                stream.write(bytes);
                stream.close();
            } else {
                throw new UnexpectedException("No file to upload");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new UnexpectedException("File IO error, can not upload");
        }
    }
}
