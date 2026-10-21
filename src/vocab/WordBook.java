package vocab;

import java.util.Arrays;

public class WordBook {

    private Word[] words;
    private int size;
    private static final int INITIAL_CAPACITY = 10;
    private static final int[] TABLE = {1, 2, 4, 7, 15, 30};

    public WordBook() {
        size = 0;
        words = new Word[INITIAL_CAPACITY];
    }

    public void add(Word w) {
        if (size == words.length) {
            words = Arrays.copyOf(words, words.length * 2);
        }
        words[size++] = w;
    }

    public Word find(String spelling) {
        for (int i = 0; i < size; i++) {
            if (spelling.equals(words[i].getSpelling())) {
                return words[i];
            }
        }
        return null;
    }

    public boolean isDue(Word w, int today) {
        return today >= dueDay(w) && !w.isArchived();
    }

    public void markReviewed(Word w, int today) {
        w.recordReviewed(today);
    }

    public void archive(Word w) {
        w.markArchived();
    }

    public String display(int today) {
        if (size == 0) {
            return "\"The secret of getting ahead is getting started.\" — Mark Twain";                  // 空本直接返回，不进循环
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                result.append("\n\n");          // 只在块之间加分隔，避免末尾多空行
            }

            // 处理特殊字段
            String meaningText;
            if (words[i].getMeanings().length == 0) {
                meaningText = "n/a";
            } else {
                meaningText = String.join("/", words[i].getMeanings());
            }
            String lastLine;
            if (words[i].isArchived()) {
                lastLine = "  status   : archived";
            } else {
                lastLine = "  next due : day " + dueDay(words[i])
                        + (isDue(words[i], today) ? " (DUE)" : "");
            }

            // 这里拼第 i 个词的块
            result
                .append(words[i].getSpelling()).append("\n")
                .append("  meanings : ").append(meaningText).append("\n")
                .append("  joined   : ").append(words[i].getJoinDay()).append("\n")
                .append("  reviewed : ").append(words[i].getReviewCount()).append("\n")
                .append(lastLine)
            ;
        }
        return result.toString();
    }

    private int dueDay(Word w) {
        int n = w.getReviewCount();
        int offset = (n <= 5) ? TABLE[n] : 30;
        return w.getLastDay() + offset;
    }
}
