package org.exeinspector.files.elf;

import java.io.ByteArrayInputStream;

public class ElfSectionHeader {
    public static final int SHT_NULL = 0;
    public static final int SHT_STRTAB = 3;
    public static final int SHT_NOBITS = 8;
    public final int sh_name;
    public final int sh_type;
    public final long sh_flags;
    public final long sh_addr;
    public final long sh_offset;
    public final long sh_size;
    public final int sh_link;
    public final int sh_info;
    public final long sh_addralign;
    public final long sh_entsize;
    private final ElfFile elfHeader;

    ElfSectionHeader(ByteArrayInputStream fileAsBytes, ElfFile elfFile, long offset) {
        this.elfHeader = elfFile;
        fileAsBytes.reset();
        if (fileAsBytes.skip(offset) != offset) throw new RuntimeException("seeking outside file");

        sh_name = elfFile.readInt();
        sh_type = elfFile.readInt();
        sh_flags = elfFile.readIntOrLong();
        sh_addr = elfFile.readIntOrLong();
        sh_offset = elfFile.readIntOrLong();
        sh_size = elfFile.readIntOrLong();
        sh_link = elfFile.readInt();
        sh_info = elfFile.readInt();
        sh_addralign = elfFile.readIntOrLong();
        sh_entsize = elfFile.readIntOrLong();
    }

    public String getName() {
        if (sh_name == 0) return null;
        ElfStringTable tbl = (ElfStringTable) elfHeader.getSection(elfHeader.e_shstrndx);
        return tbl.get(sh_name);
    }
}
