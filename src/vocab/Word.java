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
        this.spelling = requireValidSpelling(spelling);
        this.meanings = requireValidMeanings(meanings);
        this.joinDay  = requireValidJoinDay(joinDay);
        this.lastDay  = this.joinDay;
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
    private static String requireValidSpelling(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("spelling must not be blank");
        }
        return s;
    }

    private static String[] requireValidMeanings(String[] m) {
        if (m == null) {
            throw new IllegalArgumentException("meanings must not be null");
        }
        return Arrays.copyOf(m, m.length);
    }

    private static int requireValidJoinDay(int day) {
        if (day < 1) {
            throw new IllegalArgumentException("joinDay must be >= 1, got " + day);
        }
        return day;
    }
}
