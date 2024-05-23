package org.exeinspector.files.pe;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PeFile {
  public static String FILE_TYPE = "Portable Executable";
  private final File pathToFile;
  private final int Magic;
  private final int e_lfanew;
  private final int CheckSum;
  private final int SecurityDirectoryRVA;

  public static PeFile form(File pathToFile) throws IOException {
    int e_lfanew = 0;
    int Magic = 0;
    int CheckSum = 0;
    int SecurityDirectoryRVA = 0;
    try (RandomAccessFile reader = new RandomAccessFile(pathToFile, "r")) {
      reader.seek(0x3C);
      for (int i = 0; i < 4; i++) {
        int b = reader.readUnsignedByte();
        b = b << 8 * i;
        e_lfanew += b;
      }
      reader.seek(e_lfanew + 0x18);
      for (int i = 0; i < 2; i++) {
        int b = reader.readUnsignedByte();
        b = b << 8 * i;
        Magic += b;
      }
      reader.seek(e_lfanew + 0x18 + 0x40);
      for (int i = 0; i < 4; i++) {
        int b = reader.readUnsignedByte();
        b = b << 8 * i;
        CheckSum += b;
      }

      int offsetToDataDirectories = 0;
      if (Magic == 0x10b) {
        offsetToDataDirectories = 0x60;
      } else if (Magic == 0x20b) {
        offsetToDataDirectories = 0x70;
      } else {
        throw new IOException("Invalid file format");
      }
      reader.seek(e_lfanew + 0x18 + offsetToDataDirectories + 0x20);
      for (int i = 0; i < 4; i++) {
        int b = reader.readUnsignedByte();
        b = b << 8 * i;
        SecurityDirectoryRVA += b;
      }
    }
    return new PeFile(pathToFile, e_lfanew, CheckSum, CheckSum, SecurityDirectoryRVA);
  }
  protected PeFile(File pathToFile, int e_lfanew, int magic, int checkSum, int securityDirectoryRVA) {
    this.pathToFile = pathToFile;
    this.e_lfanew = e_lfanew;
    Magic = magic;
    CheckSum = checkSum;
    SecurityDirectoryRVA = securityDirectoryRVA;
  }

  public String getBitDepth() {
    if (Magic == 0x10b) {
      return "PE32";
    } else {
      return "PE64";
    }
  }

  public int getCheckSum() {
    return CheckSum;
  }

  public int getSecurityDirectoryRVA() {
    return SecurityDirectoryRVA;
  }
}
