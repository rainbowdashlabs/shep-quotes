package de.chojo.shepquotes.listener.page;

import java.util.List;

public class QuotePage implements Page {
    List<Integer> quotes;
    long owner;
    int currentPage = 0;

    public QuotePage(List<Integer> quotes, long owner) {
        this.quotes = quotes;
        this.owner = owner;
    }

    @Override
    public int current() {
        return currentPage;
    }

    @Override
    public int pages() {
        return quotes.size();
    }

    public int pageValue() {
        return quotes.get(currentPage);
    }

    @Override
    public int next() {
        if (currentPage == 0) {
            currentPage = quotes.size() - 1;
        } else {
            currentPage--;
        }
        return current();
    }

    @Override
    public int previous() {
        if (currentPage + 1 >= quotes.size()) {
            currentPage = 0;
        } else {
            currentPage++;
        }
        return current();
    }
}
