package de.chojo.shepquotes.config.elements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class BaseSettings {
    private String token = "";
    private List<Long> botOwner = new ArrayList<>();

    public String token() {
        return token;
    }

    public boolean isOwner(long id) {
        return botOwner.contains(id);
    }
}
