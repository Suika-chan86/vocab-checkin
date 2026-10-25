package vocab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WordBookTest {

    private static Word wordJoinedOn(String spelling, int joinDay) {
        return new Word(spelling, new String[]{"m"}, joinDay);
    }

    @Test
    public void firstReviewBoundary() {
        WordBook book = new WordBook();
        Word w = wordJoinedOn("apple", 5);
        book.add(w);

        assertFalse(book.isDue(w, 5));
        assertTrue(book.isDue(w, 6));
    }

    @Test
    public void onTimeScheduleFollowsIntervals() {
        WordBook book = new WordBook();
        Word w = wordJoinedOn("apple", 1);
        book.add(w);

        int[] dueDays = {2, 4, 8, 15, 30, 60, 90, 120};
        for (int day : dueDays) {
            assertFalse(book.isDue(w, day - 1));
            assertTrue(book.isDue(w, day));
            book.markReviewed(w, day);
        }
    }

    @Test
    public void lateReviewResetsNextDue() {
        WordBook book = new WordBook();
        Word w = wordJoinedOn("apple", 1);
        book.add(w);
        book.markReviewed(w, 10);

        assertFalse(book.isDue(w, 11));
        assertTrue(book.isDue(w, 12));
    }

    @Test
    public void archivedWordIsNeverDue() {
        WordBook book = new WordBook();
        Word w = wordJoinedOn("apple", 1);
        book.add(w);
        book.archive(w);

        assertFalse(book.isDue(w, 2));
        assertFalse(book.isDue(w, 1000));
    }

    @Test
    public void addAndFindSurviveGrowth() {
        WordBook book = new WordBook();
        Word w = wordJoinedOn("apple", 1);
        book.add(w);

        assertSame(w, book.find("apple"));
        assertNull(book.find("banana"));
        for (int i = 0; i < 20; i++) {
            book.add(wordJoinedOn("w" + i, 1));
        }
        assertNotNull(book.find("w19"));
    }

    @Test
    public void displaySmokeTest() {
        WordBook book1 = new WordBook();
        String emptyOut = book1.display(1);

        WordBook book2 = new WordBook();
        Word w1 = wordJoinedOn("apple", 1);
        book2.add(w1);
        Word w2 = new Word("banana", new String[]{}, 1);
        book2.add(w2);
        book2.archive(w2);
        String occupiedOut = book2.display(1);

        assertTrue(emptyOut.contains("Mark Twain"));
        assertTrue(occupiedOut.contains("apple"));
        assertTrue(occupiedOut.contains("n/a"));
        assertTrue(occupiedOut.contains("archived"));
        assertFalse(occupiedOut.endsWith("\n"));
    }

    @Test
    void nullSpellingIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Word(null, new String[]{"m"}, 1));
    }

    @Test
    void blankSpellingIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Word("   ", new String[]{"m"}, 1));
    }

    @Test
    void nullMeaningsIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Word("apple", null, 1));
    }

    @Test
    void joinDayBelowOneIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Word("apple", new String[]{"m"}, 0));
    }

    @Test
    void emptyMeaningsIsAllowed() {
        assertDoesNotThrow(
                () -> new Word("apple", new String[]{}, 1));
    }

    @Test
    void restoreKeepsFullState() {
        Word w = new Word("apple", new String[]{"m"}, 5, 12, 2, true);

        assertEquals(5, w.getJoinDay());
        assertEquals(12, w.getLastDay());
        assertEquals(2, w.getReviewCount());
        assertTrue(w.isArchived());
    }

    @Test
    void restoreRejectsLastDayBeforeJoinDay() {
        assertThrows(IllegalArgumentException.class,
                () -> new Word("apple", new String[]{"m"}, 5, 4, 0, false));
    }

    @Test
    void restoreRejectsNegativeReviewCount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Word("apple", new String[]{"m"}, 5, 5, -1, false));
    }

    @Test
    void restoreRejectsLastDayMismatchWhenNoReviews() {
        assertThrows(IllegalArgumentException.class,
                () -> new Word("apple", new String[]{"m"}, 5, 6, 0, false));
    }

    @Test
    void restoreRejectsUnchangedLastDayWhenReviewed() {
        assertThrows(IllegalArgumentException.class,
                () -> new Word("apple", new String[]{"m"}, 5, 5, 1, false));
    }

    @Test
    void restoreAcceptsFreshState() {
        assertDoesNotThrow(
                () -> new Word("apple", new String[]{"m"}, 5, 5, 0, false));
    }

    @Test
    void toArrayReturnsSnapshotInInsertionOrder() {
        WordBook book = new WordBook();
        Word a = new Word("apple", new String[]{"m"}, 1);
        Word b = new Word("banana", new String[]{"m"}, 2);
        book.add(a);
        book.add(b);

        Word[] snapshot = book.toArray();

        assertEquals(2, snapshot.length);
        assertSame(a, snapshot[0]);
        assertSame(b, snapshot[1]);
    }

    @Test
    void modifyingSnapshotDoesNotAffectBook() {
        WordBook book = new WordBook();
        Word a = new Word("apple", new String[]{"m"}, 1);
        book.add(a);

        Word[] snapshot = book.toArray();
        snapshot[0] = null;

        assertSame(a, book.find("apple"));
    }

    @Test
    void toArrayIncludesWordsAfterGrowth() {
        WordBook book = new WordBook();
        for (int i = 0; i < 15; i++) {
            book.add(new Word("w" + i, new String[]{"m"}, 1));
        }

        Word[] snapshot = book.toArray();

        assertEquals(15, snapshot.length);
        assertEquals("w0", snapshot[0].getSpelling());
        assertEquals("w14", snapshot[14].getSpelling());
    }

    @Test
    void emptyBookReturnsEmptyArray() {
        assertEquals(0, new WordBook().toArray().length);
    }
}