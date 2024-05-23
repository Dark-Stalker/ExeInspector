package org.exeinspector.files.pe;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.*;
import java.text.DateFormat;
import java.util.Arrays;

public class ParsePESignature {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }
  public static String getInfoAboutCertificate(File pathToFile, int certOffset) {
    if (certOffset <= 0) {
      return "Подпись отсутствует";
    }
    X509Certificate certificate = getCertificateOrNull(pathToFile, certOffset);
    if (certificate == null) {
      return "Подпись отсутствует";
    }
    StringBuilder stringBuilder = new StringBuilder();
    String[] subject = certificate.getSubjectX500Principal().toString().split(",");
    String[] issuer = certificate.getIssuerX500Principal().toString().split(",");
    stringBuilder.append(String.format("Кому выдан: %s\n", subject[0].substring(3)));
    stringBuilder.append(String.format("Кем выдан: %s\n", issuer[0].substring(3)));
    stringBuilder.append(String.format("Действителен с %s по %s\n",
        DateFormat.getDateInstance().format(certificate.getNotBefore()),
        DateFormat.getDateInstance().format(certificate.getNotAfter())));
    try {
      certificate.checkValidity();
      stringBuilder.append("(Подпись действительна)");
    } catch (CertificateNotYetValidException e) {
      stringBuilder.append("(Подпись ещё недействительна)");
    } catch (CertificateExpiredException e) {
      stringBuilder.append("(Подпись уже недействительна)");
    }
    return stringBuilder.toString();
  }
  private static X509Certificate getCertificateOrNull(File pathToFile, int certOffset) {
    try (
      FileInputStream fileInputStream = new FileInputStream(pathToFile);
    ) {
      fileInputStream.skipNBytes(certOffset + 8);
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");
      X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(fileInputStream);
      return certificate;
    } catch (CertificateException | NoSuchProviderException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}