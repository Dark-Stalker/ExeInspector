package org.exeinspector.files.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class FileHasher {
  public static String getHashOfFile(String pathToFile, String algorithm) throws IOException, NoSuchAlgorithmException {
    byte[] digest = getDigest(pathToFile, algorithm);
    return getHashInHex(digest);
  }
  private static byte[] getDigest(String pathToFile, String algorithm) throws IOException, NoSuchAlgorithmException{
    MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
    try (
        FileInputStream fileInputStream = new FileInputStream(pathToFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        DigestInputStream digestInputStream = new DigestInputStream(bufferedInputStream, messageDigest);
    ) {
      while (digestInputStream.read() != -1);
    }
    return messageDigest.digest();
  }
  private static String getHashInHex(byte[] digest) {
    StringBuilder hashBuilderString = new StringBuilder();
    for (byte b : digest) {
      hashBuilderString.append(String.format("%02x", b));
    }
    return hashBuilderString.toString().toUpperCase();
  }
}
