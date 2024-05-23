package org.exeinspector.files.elf;

import org.exeinspector.files.utils.ADSReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ElfFile {
    public static final byte DATA_LSB = 1;
    public static final byte DATA_MSB = 2;
    public static final byte CLASS_32 = 1;
    public static final byte CLASS_64 = 2;
    private final ElfSection[] sections;
    public final byte ei_class;
    public final byte ei_data;
    public final byte ei_version;
    public final byte ei_osabi;
    public final byte es_abiversion;
    public final short e_type;
    public final short e_machine;
    public final int e_version;
    public final long e_entry;
    public final long e_phoff;
    public final long e_shoff;
    public final int e_flags;
    public final short e_ehsize;
    public final short e_phentsize;
    public final short e_phnum;
    public final short e_shentsize;
    public final short e_shnum;
    public final short e_shstrndx;
    private final ByteArrayInputStream fileAsBytes;

    public ElfSection getSection(int index) {
        return sections[index];
    }

    public ElfSection getSectionByName(String sectionName) {
        for (int i = 1; i < e_shnum; i++) {
            ElfSection sh = getSection(i);
            if (sectionName.equals(sh.header.getName())) return sh;
        }
        return null;
    }

    short readShort() {
        int ch1 = (short) fileAsBytes.read();
        int ch2 = (short) fileAsBytes.read();
        if (ei_data == DATA_LSB) {
            return (short) (((short) ch2 & 0xff) << 8 | ((short) ch1 & 0xff));
        } else {
            return (short) (((short) ch1 & 0xff) << 8 | ((short) ch2 & 0xff));
        }
    }

    int readInt() {
        int ch1 = fileAsBytes.read();
        int ch2 = fileAsBytes.read();
        int ch3 = fileAsBytes.read();
        int ch4 = fileAsBytes.read();

        if (ei_data == ElfFile.DATA_LSB) {
            return ((int) ch4 & 0xff) << 24 | ((int) ch3 & 0xff) << 16 | ((int) ch2 & 0xff) << 8 | ((int) ch1 & 0xff);
        } else {
            return ((int) ch1 & 0xff) << 24 | ((int) ch2 & 0xff) << 16 | ((int) ch3 & 0xff) << 8 | ((int) ch4 & 0xff);
        }
    }

    long readLong() {
        int ch1 = fileAsBytes.read();
        int ch2 = fileAsBytes.read();
        int ch3 = fileAsBytes.read();
        int ch4 = fileAsBytes.read();
        int ch5 = fileAsBytes.read();
        int ch6 = fileAsBytes.read();
        int ch7 = fileAsBytes.read();
        int ch8 = fileAsBytes.read();

        if (ei_data == ElfFile.DATA_LSB) {
            return ((long) ch8 << 56) | ((long) ch7 & 0xff) << 48 | ((long) ch6 & 0xff) << 40
                    | ((long) ch5 & 0xff) << 32 | ((long) ch4 & 0xff) << 24 | ((long) ch3 & 0xff) << 16
                    | ((long) ch2 & 0xff) << 8 | ((long) ch1 & 0xff);
        } else {
            return ((long) ch1 << 56) | ((long) ch2 & 0xff) << 48 | ((long) ch3 & 0xff) << 40
                    | ((long) ch4 & 0xff) << 32 | ((long) ch5 & 0xff) << 24 | ((long) ch6 & 0xff) << 16
                    | ((long) ch7 & 0xff) << 8 | ((long) ch8 & 0xff);
        }
    }

    long readIntOrLong() {
        return ei_class == ElfFile.CLASS_32 ? readInt() : readLong();
    }

    public static ElfFile from(File file) throws RuntimeException, IOException {
        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file)) {
            int totalRead = 0;
            while (totalRead < buffer.length) {
                int readNow = in.read(buffer, totalRead, buffer.length - totalRead);
                if (readNow == -1) {
                    throw new RuntimeException("Premature end of file");
                } else {
                    totalRead += readNow;
                }
            }
        }
        return new ElfFile(new ByteArrayInputStream(buffer));
    }

    ElfFile(ByteArrayInputStream fileAsBytes) {
        this.fileAsBytes = fileAsBytes;
        byte[] ident = new byte[16];
        int bytesRead;
        try {
            bytesRead = fileAsBytes.read(ident);
        } catch (IOException e) {
            throw new ElfException("Error reading " + ident.length + " bytes", e);
        }
        if (bytesRead != ident.length) {
            throw new ElfException("Error reading elf header (read " + bytesRead + "bytes - expected to read " + ident.length + "bytes)");
        }
        if (!(0x7f == ident[0] && 'E' == ident[1] && 'L' == ident[2] && 'F' == ident[3])) {
            throw new ElfException("Bad magic number for file");
        }
        ei_class = ident[4];
        ei_data = ident[5];
        ei_version = ident[6];
        if (ei_version != 1) throw new ElfException("Invalid elf version: " + ei_version);
        ei_osabi = ident[7];
        es_abiversion = ident[8];
        e_type = readShort();
        e_machine = readShort();
        e_version = readInt();
        e_entry = readIntOrLong();
        e_phoff = readIntOrLong();
        e_shoff = readIntOrLong();
        e_flags = readInt();
        e_ehsize = readShort();
        e_phentsize = readShort();
        e_phnum = readShort();
        e_shentsize = readShort();
        e_shnum = readShort();
        e_shstrndx = readShort();

        sections = new ElfSection[e_shnum];
        for (int i = 0; i < e_shnum; i++) {
            final long sectionHeaderOffset = e_shoff + (i * e_shentsize);
            ElfSectionHeader elfSectionHeader = new ElfSectionHeader(fileAsBytes, this, sectionHeaderOffset);
            sections[i] = switch (elfSectionHeader.sh_type) {
                case ElfSectionHeader.SHT_STRTAB -> new ElfStringTable(elfSectionHeader, fileAsBytes);
                default -> new ElfSection(elfSectionHeader, fileAsBytes);
            };
        }
    }

    public String getFileType() {
        return switch (e_type) {
            case 1 -> "Relocatable file";
            case 2 -> "Executable file";
            case 3 -> "Shared object file";
            case 4 -> "Core file";
            default -> "Unknown";
        };
    }

    public String getBitDepth() {
        return switch (ei_class) {
            case 1 -> "ELF32";
            case 2 -> "ELF64";
            default -> "Unknown";
        };
    }

    public String getCompiler() {
        ElfSection dataSection = getSectionByName(".comment");
        StringBuilder compilerInfo = new StringBuilder();
        try {
            for (int i = 0; i < dataSection.getData().length - 1; i++) {
                compilerInfo.append((char) dataSection.getData()[i]);
            }
        } catch (NullPointerException e) {
            return "Не найдено";
        }
        return compilerInfo.toString();
    }
}
