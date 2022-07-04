package de.chojo.shepquotes.config.elements;

public class Links {
    private String tos = "";
    private String invite = "https://discord.com/api/oauth2/authorize?client_id=952974000177446922&permissions=139586817088&scope=bot%20applications.commands";
    private String support = "";
    private String website = "https://rainbowdashlabs.github.io/shep-quotes/";
    private String faq = "https://rainbowdashlabs.github.io/shep-quotes/faq";

    public String tos() {
        return tos;
    }

    public String invite() {
        return invite;
    }

    public String support() {
        return support;
    }

    public String website() {
        return website;
    }

    public String faq() {
        return faq;
    }
}
