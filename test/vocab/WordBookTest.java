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
}