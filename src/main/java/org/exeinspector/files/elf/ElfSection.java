package org.exeinspector.files.elf;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ElfSection {
    public final ElfSectionHeader header;
    private final ByteArrayInputStream fileAsBytes;

    ElfSection(ElfSectionHeader header, ByteArrayInputStream fileAsBytes) {
        this.header = header;
        this.fileAsBytes = fileAsBytes;
    }

    public byte[] getData() {
        if (header.sh_size == 0 || header.sh_type == ElfSectionHeader.SHT_NOBITS || header.sh_type == ElfSectionHeader.SHT_NULL) {
            return new byte[0];
        } else if (header.sh_size > (long) Integer.MAX_VALUE) {
            throw new RuntimeException("Too big section: " + header.sh_size);
        }

        byte[] result = new byte[(int) header.sh_size];
        fileAsBytes.reset();
        if (fileAsBytes.skip(header.sh_offset) != header.sh_offset) throw new RuntimeException("seeking outside file");
        try {
            fileAsBytes.read(result);
        } catch (IOException e) {
            throw new RuntimeException("Error reading " + result.length + " bytes", e);
        }
        return result;
    }
}
