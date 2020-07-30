package io.leontyev.crawler.engine;

import java.util.Objects;

public class KeyValue {

    private final String library;
    private final long occurrences;

    public KeyValue(String library, long occurrences) {
        this.library = library;
        this.occurrences = occurrences;
    }

    public String getLibrary() {
        return library;
    }

    public long getOccurrences() {
        return occurrences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyValue keyValue = (KeyValue) o;
        return library.equals(keyValue.library);
    }

    @Override
    public int hashCode() {
        return Objects.hash(library);
    }

    @Override
    public String toString() {
        return "KeyValue{" +
                "library='" + library + '\'' +
                ", occurrences=" + occurrences +
                '}';
    }
}
