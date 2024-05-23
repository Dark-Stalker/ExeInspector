package org.exeinspector.files.pe;

import org.exeinspector.files.elf.ElfException;
import org.exeinspector.files.elf.ElfFile;
import org.exeinspector.files.utils.ADSReader;
import org.exeinspector.files.utils.AttributesUtils;
import org.exeinspector.files.utils.ByteConverter;
import org.exeinspector.files.utils.FileHasher;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

public class PeHandler {
  public static boolean isPeFile(File file) throws IOException {
    try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
      short MZ = reader.readShort();
      if (MZ != 0x4D5A) {
        return false;
      }
      reader.seek(0x3C);
      int offset = 0;
      for (int i = 0; i < 4; i++) {
        int b = reader.readUnsignedByte();
        b = b << 8 * i;
        offset += b;
      }
      reader.seek(offset);
      short magic = reader.readShort();
      return magic == 0x5045;
    }
  }

  public static LinkedHashMap<String, String> getInformation(File file) throws IOException, NoSuchAlgorithmException {
    PeFile peFile = PeFile.form(file);
    LinkedHashMap<String, String> information = new LinkedHashMap<>();
    information.put("Имя файла", file.getName());
    information.put("Путь к файла", file.getAbsolutePath());
    information.put("Размер файла", ByteConverter.byteCountToDisplaySize(AttributesUtils.getFileSize(file.getAbsolutePath())));
    information.put("Дата создания", AttributesUtils.getCreationTime(file.getAbsolutePath()));
    information.put("Дата последнего изменения", AttributesUtils.getLastModifiedTime(file.getAbsolutePath()));
    information.put("Разрядность файла", peFile.getBitDepth());
    information.put("Тип файла", PeFile.FILE_TYPE);
    information.put("Альтернативные потоки данных", ADSReader.getADS(file.getAbsolutePath()));
    information.put("Контрольная сумма в заголовке", String.format("%08X", peFile.getCheckSum()));
    information.put("MD5", FileHasher.getHashOfFile(file.getAbsolutePath(), "MD5"));
    information.put("SHA-256", FileHasher.getHashOfFile(file.getAbsolutePath(), "SHA-256"));
    information.put("SHA-512", FileHasher.getHashOfFile(file.getAbsolutePath(), "SHA-512"));
    information.put("Цифровая подпись", ParsePESignature.getInfoAboutCertificate(file, peFile.getSecurityDirectoryRVA()));
    return information;
  }
}
