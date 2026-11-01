package vocab;

import java.io.*;
import java.nio.charset.StandardCharsets;


public class WordStorage {

    private final File file;

    public WordStorage(File file) {
        this.file = file;
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
    }

    public WordBook load() throws IOException {

        WordBook book = new WordBook();
        if (!file.exists()) return book;

        try (BufferedReader in = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))){
            int lineNumber = 0;
            String line;

            while ((line = in.readLine()) != null) {
                // 剥除BOM
                lineNumber++;
                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                try {
                    Word word = parseLine(line);
                    book.add(word);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "line " + lineNumber + ": " + e.getMessage(), e
                    );
                }
            }

        }
        return book;

    }

    public void save(WordBook book) throws IOException {
        if (book == null) {
            throw new IllegalArgumentException("book must not be null");
        }

        Word[] words = book.toArray();      // 只读一次快照

        // 1. 只校验，不碰文件
        for (Word w : words) {
            validateForSave(w);
        }

        // 2. 目录
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if(!parent.mkdirs()) {
                throw new IOException(
                        "failed to create directory: " + parent
                );
            }
        }

        // 3. 打开文件（截断/覆盖可能存在的原文件）
        try (PrintWriter out = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {

            out.print("# vocab-checkin v1\n");

            for (Word w : words) {
                writeWord(out, w);
            }

            if (out.checkError()) {
                throw new IOException("failed to write " + file);
            }
        }

    }

    private void validateForSave(Word w) {
        String spelling = w.getSpelling();
        if (spelling.contains("|")  ||
            spelling.contains("\n") ||
            spelling.contains("\r") ||
            spelling.startsWith("#"))
        {
            throw new IllegalArgumentException(
                    "spelling should not contains '|' '\\n' '\\r' '#' or starts with '#'"
            );
        }

        String[] meanings = w.getMeanings();
        for (String meaning : meanings) {
            if (meaning.contains("|")  ||
                meaning.contains("/")  ||
                meaning.contains("\n") ||
                meaning.contains("\r"))
            {
                throw new IllegalArgumentException(
                        "meaning should not contains following characters: '|' '/' '\\n' '\\r'"
                );
            }
        }
    }

    private void writeWord(PrintWriter out, Word word) {
        out.print(word.getSpelling());
        out.print("|");
        out.print(String.join("/", word.getMeanings()));
        out.print("|");
        out.print(word.getJoinDay());
        out.print("|");
        out.print(word.getLastDay());
        out.print("|");
        out.print(word.getReviewCount());
        out.print("|");
        out.print(word.isArchived());
        out.print("\n");
    }

    private Word parseLine(String line) {
        // 整行读取信息，分割字段并规范化
        String[] parts = line.split("\\|", -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("expected 6 fields, got " + parts.length);
        }
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        // 语义化
        String spelling = parts[0];
        // 释义规范化
        String[] meanings;
        if (parts[1].isEmpty()) {
            meanings = new String[]{};
        } else {
            String[] temp = parts[1].split("/", -1);
            for (int i = 0; i < temp.length; i++) {
                temp[i] = temp[i].trim();
                if (temp[i].isEmpty()) {
                    throw new IllegalArgumentException("expected non-empty meaning");
                }
            }
            meanings = temp;
        }
        // 数字字段合法判定
        String[] fieldNames = {"joinDay", "lastDay", "reviewCount"};
        int[] numbers = new int[3];
        for (int i = 0; i < 3; i++) {
            try {
                numbers[i] = Integer.parseInt(parts[i + 2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        fieldNames[i] + " is not a number: '" + parts[i + 2] + "'"
                );
            }
        }
        // archived合法判定
        boolean archived;
        if (parts[5].equals("true")) {
            archived = true;
        } else if (parts[5].equals("false")) {
            archived = false;
        } else {
            throw new IllegalArgumentException("expected 'true' or 'false', got " + parts[5]);
        }

        return new Word(spelling, meanings, numbers[0], numbers[1], numbers[2], archived);
    }
}