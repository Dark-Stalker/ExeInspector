package org.exeinspector.files.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.util.Date;

public class AttributesUtils {
  public static long getFileSize(String pathToFile) throws IOException {
    BasicFileAttributes attribute = Files.readAttributes(Path.of(pathToFile), BasicFileAttributes.class);
    return attribute.size();
  }
  public static String getCreationTime(String pathToFile) throws IOException {
    BasicFileAttributes attribute = Files.readAttributes(Path.of(pathToFile), BasicFileAttributes.class);
    Date date = new Date(attribute.creationTime().toMillis());
    return DateFormat.getDateInstance().format(date);
  }
  public static String getLastModifiedTime(String pathToFile) throws IOException {
    BasicFileAttributes attribute = Files.readAttributes(Path.of(pathToFile), BasicFileAttributes.class);
    Date date = new Date(attribute.lastModifiedTime().toMillis());
    date.getTime();
    return DateFormat.getDateInstance().format(date);
  }
}
