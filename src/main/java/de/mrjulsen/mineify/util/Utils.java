package de.mrjulsen.mineify.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.TriConsumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import de.mrjulsen.mineify.ModMain;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;

public class Utils {

    public static void shiftByteArray(byte[] array, int n) {
        int length = array.length;
        byte[] temp = new byte[length];
        
        for (int i = 0; i < length; i++) {
            temp[(i + n) % length] = array[i];
        }
        
        System.arraycopy(temp, 0, array, 0, length);
    }

    public static String getUUID(String playername) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playername);
            Scanner scan = new Scanner(url.openStream());
            String str = "";
            while (scan.hasNext())
                str += scan.nextLine();
            scan.close();
            JsonObject player = new Gson().fromJson(str, JsonObject.class);
            return player.get("id").getAsString();
        } catch (Exception e) {
            return "null";
        }
    }

    public static String getPlayerName(String uuid) {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            Scanner scan = new Scanner(url.openStream());
            String str = "";
            while (scan.hasNext())
                str += scan.nextLine();
            scan.close();
            JsonObject player = new Gson().fromJson(str, JsonObject.class);
            String username = player.get("name").getAsString();
            return username;
        } catch (Exception e) {
            ModMain.LOGGER.warn("Could not get username for player with uuid " + uuid);
            return "Unknown User";
        }
    }
    
    @SuppressWarnings("resource")
    public static <T extends Enum<T> & ITranslatableEnum> List<FormattedCharSequence> getEnumTooltipData(Screen s, Class<T> enumClass, int maxWidth) {
        List<FormattedCharSequence> c = new ArrayList<>();
        T enumValue = enumClass.getEnumConstants()[0];
        c.addAll(s.getMinecraft().font.split(Component.translatable(enumValue.getDescriptionTranslationKey()), maxWidth));
        c.add(Component.literal("").getVisualOrderText());
        c.addAll(Arrays.stream(enumClass.getEnumConstants()).map((tr) -> {
            return Component.literal(String.format("§l> %s§r§7\n%s", Component.translatable(tr.getTranslationKey()).getString(), Component.translatable(tr.getInfoTranslationKey()).getString()));
        }).map((x) -> s.getMinecraft().font.split(x, maxWidth)).flatMap(List::stream).collect(Collectors.toList()));
        
        return c;
    }

    @SuppressWarnings("resource")
    public static List<FormattedCharSequence> getTooltipData(Screen s, Component c, int maxWidth) {
        return s.getMinecraft().font.split(c, maxWidth);
    }

    @SuppressWarnings("resource")
    public static <W extends AbstractWidget> void renderTooltip(Screen s, W w, Supplier<List<FormattedCharSequence>> lines, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (w.isMouseOver(mouseX, mouseY)) {
            guiGraphics.renderTooltip(s.getMinecraft().font, lines.get(), mouseX, mouseY);
        }
    }    

    public static void giveAdvancement(ServerPlayer player, String name, String criteriaKey) {
        Advancement adv = player.getServer().getAdvancements().getAdvancement(new ResourceLocation(ModMain.MOD_ID, name));
        player.getAdvancements().award(adv, criteriaKey);
    }

    public static void executeIfNotNull(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    public static <T> void executeIfNotNull(Consumer<T> consumer, T object) {
        if (consumer != null) {
            consumer.accept(object);
        }
    }

    public static <A, B> void executeIfNotNull(BiConsumer<A, B> consumer, A objectA, B objectB) {
        if (consumer != null) {
            consumer.accept(objectA, objectB);
        }
    }

    public static <A, B, C> void executeIfNotNull(TriConsumer<A, B, C> consumer, A objectA, B objectB, C objectC) {
        if (consumer != null) {
            consumer.accept(objectA, objectB, objectC);
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
    
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


}
