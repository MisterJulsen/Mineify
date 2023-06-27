package de.mrjulsen.mineify.network;

import net.minecraft.network.FriendlyByteBuf;

public class ToastMessage {
    public final String titleTranslationKey;
    public final String descriptionTranslationKey;
    public final String[] data;

    public ToastMessage(String titleTranslationKey, String descriptionTranslationKey, String[] data) {
        this.titleTranslationKey = titleTranslationKey;
        this.descriptionTranslationKey = descriptionTranslationKey;
        this.data = data;
    }

    public ToastMessage(String titleTranslationKey, String descriptionTranslationKey) {
        this(titleTranslationKey, descriptionTranslationKey, new String[0]);
    }

    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeUtf(titleTranslationKey);
        buffer.writeUtf(descriptionTranslationKey);        
        buffer.writeInt(data.length);
        for (int i = 0; i < data.length; i++) {
            buffer.writeUtf(data[i]);
        }
    }

    public static ToastMessage deserialize(FriendlyByteBuf buffer) {
        String titleTranslationKey = buffer.readUtf();
        String descriptionTranslationKey = buffer.readUtf();
        int l = buffer.readInt();
        String[] data = new String[l];
        for (int i = 0; i < data.length; i++) {
            data[i] = buffer.readUtf();
        }

        return new ToastMessage(titleTranslationKey, descriptionTranslationKey, data);
    }
}
