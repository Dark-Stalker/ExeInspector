package org.exeinspector.files.elf;

public class ElfException extends RuntimeException {

    public ElfException(String message) {
        super(message);
    }

    public ElfException(Throwable cause) {
        super(cause);
    }

    public ElfException(String message, Throwable cause) {
        super(message, cause);
    }

}
