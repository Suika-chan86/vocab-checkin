package vocab;

import java.util.Arrays;

public class Word {

    private final String spelling;
    private final String[] meanings;
    private final int joinDay;
    private int lastDay;
    private int reviewCount;
    private boolean archived;

    public Word(String spelling, int joinDay) {
        this(spelling, new String[]{}, joinDay);
    }

    public Word(String spelling, String[] meanings, int joinDay) {
        this.spelling = normalizeSpelling(spelling);
        this.meanings  = copyMeanings(meanings);
        this.joinDay   = normalizeJoinDay(joinDay);
        this.lastDay     = this.joinDay;
        reviewCount   = 0;
        archived      = false;
    }

    // getters
    public String getSpelling() {
        return spelling;
    }

    public String[] getMeanings() {
        return Arrays.copyOf(meanings, meanings.length);
    }

    public int getJoinDay() {
        return joinDay;
    }

    public int getLastDay() {
        return lastDay;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public boolean isArchived() {
        return archived;
    }

    // updaters
    void recordReviewed(int today) {
        lastDay = today;
        reviewCount++;
    }

    void markArchived() {
        archived = true;
    }

    // assistants
    private static String normalizeSpelling(String s) {
        if (s == null || s.isBlank()) return "unknown";
        return s;
    }

    private static String[] copyMeanings(String[] m) {
        if (m == null) return new String[]{};
        return Arrays.copyOf(m, m.length);
    }

    private static int normalizeJoinDay(int day) {
        return Math.max(day, 1);
    }
}
