package de.chojo.shepquotes.listener.page;

public interface Page {
    int pages();
    int pageValue();
    int current();
    int next();
    int previous();
}
