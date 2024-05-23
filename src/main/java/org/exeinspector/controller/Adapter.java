package org.exeinspector.controller;

import org.exeinspector.files.elf.ElfHandler;
import org.exeinspector.files.pe.PeHandler;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

public class Adapter {
  public static LinkedHashMap<String, String> sendFileToHandler(File file) throws IOException, NoSuchAlgorithmException {
    LinkedHashMap<String, String> information;
    if (ElfHandler.isElfFile(file)) {
      information = ElfHandler.getInformation(file);
    } else if (PeHandler.isPeFile(file)) {
      information = PeHandler.getInformation(file);
    } else {
      throw new RuntimeException("Данный формат файлов не поддерживается.\n Требуется PE или ELF формат файла.");
    }
    return information;
  }
}
