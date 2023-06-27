package de.mrjulsen.mineify.util;

public interface ITranslatableEnum {
    String getNameOfEnum();
    String getValue();

    default String getTranslationKey() {
        return String.format("gui.mineify.%s.%s", this.getNameOfEnum(), this.getValue());
    }
    default String getDescriptionTranslationKey() {
        return String.format("gui.mineify.%s.description", this.getNameOfEnum());
    }
    default String getInfoTranslationKey() {
        return String.format("gui.mineify.%s.info.%s", this.getNameOfEnum(), this.getValue());
    }
}
