package de.mrjulsen.mineify.util.exceptions;

public class ConfigException extends Exception {
 
    private final String description;

    public ConfigException(String title, String description) {
        super(title);
        this.description = description;
    }

    public String getDetails() {
        return this.description;
    }
}
