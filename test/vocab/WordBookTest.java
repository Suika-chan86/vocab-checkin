package vocab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    @Test
    void addRejectsNullWord() {
        WordBook book = new WordBook();
        assertThrows(IllegalArgumentException.class, () -> book.add(null));
    }

    @Test
    void addRejectsDuplicateSpelling() {
        WordBook book = new WordBook();
        Word original = new Word("apple", new String[]{"m"}, 1);
        book.add(original);

        assertThrows(IllegalArgumentException.class,
                () -> book.add(new Word("apple", new String[]{"m"}, 2)));

        assertEquals(1, book.toArray().length);
        assertSame(original, book.find("apple"));
    }

    @Test
    void addAllowsDistinctSpellings() {
        WordBook book = new WordBook();
        book.add(new Word("apple", new String[]{"m"}, 1));
        book.add(new Word("banana", new String[]{"m"}, 1));

        assertEquals(2, book.toArray().length);
    }

    @Test
    void addStillWorksAfterDuplicateRejection() {
        WordBook book = new WordBook();
        book.add(new Word("apple", new String[]{"m"}, 1));
        assertThrows(IllegalArgumentException.class,
                () -> book.add(new Word("apple", new String[]{"m"}, 1)));

        book.add(new Word("banana", new String[]{"m"}, 1));

        assertEquals(2, book.toArray().length);
        assertNotNull(book.find("banana"));
    }

    @TempDir
    Path tempDir;

    @Test
    void loadsValidFile() throws IOException {
        File file = tempDir.resolve("words.txt").toFile();

        Files.writeString(file.toPath(),
                "# vocab-checkin v1\n"
                        + "apple|苹果/苹果树|1|1|0|false\n"
                        + "banana||3|3|0|true\n",
                StandardCharsets.UTF_8);

        WordBook book = new WordStorage(file).load();

        assertEquals(2, book.toArray().length);
    }

    @Test
    void saveThenLoadRestoresMeanings() throws IOException {
        File file = tempDir.resolve("words.txt").toFile();
        WordBook book = new WordBook();
        book.add(new Word("apple", new String[]{"苹果", "苹果树"}, 1));

        WordStorage storage = new WordStorage(file);
        storage.save(book);

        Word restored = storage.load().find("apple");
        assertArrayEquals(new String[]{"苹果", "苹果树"}, restored.getMeanings());
    }

    @Test
    void saveWritesEmptyMeaningsAsEmptyField() throws IOException {
        File file = tempDir.resolve("words.txt").toFile();

        WordBook book = new WordBook();
        book.add(new Word("banana", new String[]{}, 3));

        new WordStorage(file).save(book);

        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains("banana||3|3|0|false"));
        Word restored = new WordStorage(file).load().find("banana");
        assertEquals(0, restored.getMeanings().length);
    }

    @Test
    void saveThenLoadRestoresCompleteState() throws IOException {
        File file = tempDir.resolve("words.txt").toFile();

        WordBook original = new WordBook();
        original.add(new Word("apple", new String[]{"苹果", "苹果树"}, 1));
        original.add(new Word("banana", new String[]{"香蕉"}, 3, 10, 2, false));
        original.add(new Word("cherry", new String[]{"樱桃"}, 1, 5, 3, true));
        original.add(new Word("date", new String[]{}, 2));

        WordStorage storage = new WordStorage(file);
        storage.save(original);
        WordBook loaded = storage.load();

        Word[] before = original.toArray();
        Word[] after = loaded.toArray();

        assertEquals(before.length, after.length);

        for (int i = 0; i < before.length; i++) {
            assertWordEquals(before[i], after[i]);
        }
    }

    @Test
    void emptyBookRoundTrip() throws IOException {
        File file = tempDir.resolve("empty.txt").toFile();

        WordBook original = new WordBook();
        WordStorage storage = new WordStorage(file);

        storage.save(original);
        WordBook loaded = storage.load();

        assertEquals(0, loaded.toArray().length);
    }

    @Test
    void saveTwiceKeepsOnlyLatestState() throws IOException {
        // 第一次 save 两个词，第二次 save 一个词
        // load 后长度必须是 1
        File file = tempDir.resolve("twice.txt").toFile();
        WordStorage storage = new WordStorage(file);

        WordBook first = new WordBook();
        first.add(new Word("apple", new String[]{"苹果"}, 1));
        first.add(new Word("banana", new String[]{"香蕉"}, 1));
        storage.save(first);

        WordBook second = new WordBook();
        second.add(new Word("cherry", new String[]{"樱桃"}, 1));
        storage.save(second);

        WordBook loaded = storage.load();

        assertEquals(1, loaded.toArray().length);
        assertNotNull(loaded.find("cherry"));
        assertNull(loaded.find("apple"));
        assertNull(loaded.find("banana"));
    }

    private static void assertWordEquals(Word expected, Word actual) {
        assertEquals(expected.getSpelling(), actual.getSpelling());
        assertArrayEquals(expected.getMeanings(), actual.getMeanings());
        assertEquals(expected.getJoinDay(), actual.getJoinDay());
        assertEquals(expected.getLastDay(), actual.getLastDay());
        assertEquals(expected.getReviewCount(), actual.getReviewCount());
        assertEquals(expected.isArchived(), actual.isArchived());
    }
}