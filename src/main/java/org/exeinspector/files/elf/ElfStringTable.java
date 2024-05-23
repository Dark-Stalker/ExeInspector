package org.exeinspector.files.elf;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ElfStringTable extends ElfSection {
    private final byte[] data;
    public final int numStrings;

    ElfStringTable(ElfSectionHeader header, ByteArrayInputStream fileAsBytes) {
        super(header, fileAsBytes);
        long offset = header.sh_offset;
        int length = (int) header.sh_size;

        fileAsBytes.reset();
        if (fileAsBytes.skip(offset) != offset) throw new RuntimeException("seeking outside file");
        data = new byte[length];
        int bytesRead;
        try {
            bytesRead = fileAsBytes.read(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (bytesRead != length)
            throw new RuntimeException("Error reading string table (read " + bytesRead + "bytes - expected to " + "read " + data.length + "bytes)");

        int stringsCount = 0;
        for (byte datum : data) if (datum == '\0') stringsCount++;
        numStrings = stringsCount;
    }

    public String get(int index) {
        int endPtr = index;
        while (data[endPtr] != '\0')
            endPtr++;
        return new String(data, index, endPtr - index);
    }
}
