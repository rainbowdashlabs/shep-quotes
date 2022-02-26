package de.chojo.shepquotes.listener.page;

public class ScrollPage implements Page {
    private final int total;
    long owner;
    int currentQuote = 1;

    public ScrollPage(int start, int total, long owner) {
        this.currentQuote = start;
        this.total = total;
        this.owner = owner;
    }

    @Override
    public int pages() {
        return total;
    }

    @Override
    public int pageValue() {
        return currentQuote;
    }

    @Override
    public int current() {
        return currentQuote;
    }

    @Override
    public int previous() {
        if (currentQuote == 1) {
            currentQuote = total;
        } else {
            currentQuote--;
        }
        return current();
    }

    @Override
    public int next() {
        if (currentQuote == total) {
            currentQuote = 1;
        } else {
            currentQuote++;
        }
        return current();
    }
}
