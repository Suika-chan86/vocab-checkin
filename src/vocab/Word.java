package vocab;

public class Word {

    private String spelling;
    private String[] meanings;
    private int joinDay;
    private int lastDay;
    private int reviewCount;
    private boolean archived;

    public Word(String spelling, String[] meanings, int joinDay) {
        this.spelling = spelling;
        this.meanings = meanings;
        this.joinDay  = joinDay;
        lastDay = joinDay;
        reviewCount   = 0;
        archived      = false;
    }

    // getters
    public String getSpelling() {
        return spelling;
    }

    public String[] getMeanings() {
        return meanings;
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
}
