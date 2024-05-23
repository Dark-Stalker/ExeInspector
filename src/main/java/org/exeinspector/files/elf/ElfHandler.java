package org.exeinspector.files.elf;

import org.exeinspector.files.utils.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

public class ElfHandler {
  public static boolean isElfFile(File file) throws IOException {
    try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
      int elf = reader.readInt();
      return elf == 0x7f454c46;
    }
  }
  public static LinkedHashMap<String, String> getInformation(File file) {
    ElfFile elfFile1;
    try {
      elfFile1 = ElfFile.from(file);
    } catch (IOException e) {
      throw new RuntimeException("Error reading from file", e);
    } catch (ElfException e) {
      throw e;
    }
      try {
        LinkedHashMap<String, String> information = new LinkedHashMap<>();
        information.put("Имя файла", file.getName());
        information.put("Путь к файла", file.getAbsolutePath());
        information.put("Размер файла", ByteConverter.byteCountToDisplaySize(AttributesUtils.getFileSize(file.getAbsolutePath())));
        information.put("Дата создания", AttributesUtils.getCreationTime(file.getAbsolutePath()));
        information.put("Дата последнего изменения", AttributesUtils.getLastModifiedTime(file.getAbsolutePath()));
        information.put("Разрядность файла", elfFile1.getBitDepth());
        information.put("Тип файла", elfFile1.getFileType());
        information.put("Компилятор", elfFile1.getCompiler());
        information.put("Альтернативные потоки данных", ADSReader.getADS(file.getAbsolutePath()));
        information.put("MD5", FileHasher.getHashOfFile(file.getAbsolutePath(), "MD5"));
        information.put("SHA-256", FileHasher.getHashOfFile(file.getAbsolutePath(), "SHA-256"));
        information.put("SHA-512", FileHasher.getHashOfFile(file.getAbsolutePath(), "SHA-512"));
        return information;
      } catch (IOException | NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      } catch (ElfException e) {
        throw e;
      }
  }
}
