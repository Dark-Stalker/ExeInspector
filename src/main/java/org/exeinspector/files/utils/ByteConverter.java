package org.exeinspector.files.utils;

import org.apache.commons.io.FileUtils;

public class ByteConverter {
  public static String byteCountToDisplaySize(long size) {
    String result;
    if (size < 1023) {
      result = String.format("%d bytes", size);
    } else {
      result = FileUtils.byteCountToDisplaySize(size);
      result += String.format(" (%d bytes)", size);
    }
    return result;
  }
}
