package vocab;

public class Main {
    public static void main(String[] args) {

        int today = 10;

        WordBook book = new WordBook();

        // 添加 2–3 个词
        Word w1 = new Word("terrain", new String[]{"地带", "地形"}, 1);
        Word w2 = new Word("precipitation", new String[]{"降雨量"}, 1);

        // 可选：先复习/归档几个，制造不同状态
        book.add(w1);
        book.add(w2);

        book.find("terrain");
        book.find("apple");

        book.markReviewed(w1, today);
        book.archive(w2);

        System.out.print(book.display(today));
    }
}