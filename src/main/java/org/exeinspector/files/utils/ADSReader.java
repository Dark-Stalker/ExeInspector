package org.exeinspector.files.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ADSReader {

    public static String getADS(String path) {
        ArrayList<String> parsedADS = ADSReader.start(path);
        if (parsedADS.isEmpty()) {
            return "Отсутствуют";
        }
        String size;
        String name;
        StringBuilder ADSInfo = new StringBuilder();
        for (String parsedAD : parsedADS) {
            size = parsedAD.strip().split(" ", 2)[0];
            name = parsedAD.strip().split(" ", 2)[1].split(":", 2)[1];
            ADSInfo.append("Имя: ");
            ADSInfo.append(name);
            ADSInfo.append("\n\tРазмер: ");
            ADSInfo.append(ByteConverter.byteCountToDisplaySize(Long.decode(size))).append('\n');
        }
        return ADSInfo.toString();
    }

    private static ArrayList<String> start(String path) {
        ArrayList<String> parsedADS = new ArrayList<>();

        final String command = "cmd.exe /c dir " + path + " /r"; // listing of given Path.

        final Pattern pattern = Pattern.compile(
            "\\s*"                 // any amount of whitespace
                + "[0123456789,]+\\s*"   // digits (with possible comma), whitespace
                + "([^:]+:"    // group 1 = file name, then colon,
                + "[^:]+:"     // then ADS, then colon,
                + ".+)");      // then everything else.

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
                String line;

                while ((line = br.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        parsedADS.add((matcher.group()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return parsedADS;
    }
}