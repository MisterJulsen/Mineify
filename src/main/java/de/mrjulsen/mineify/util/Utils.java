package de.mrjulsen.mineify.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mineify.ModMain;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
            e.printStackTrace();
            return "Unknown User";
        }
    }
    
    @SuppressWarnings("resource")
    public static <T extends Enum<T> & ITranslatableEnum> List<FormattedCharSequence> getEnumTooltipData(Screen s, Class<T> enumClass, int maxWidth) {
        List<FormattedCharSequence> c = new ArrayList<>();
        T enumValue = enumClass.getEnumConstants()[0];
        c.addAll(s.getMinecraft().font.split(new TranslatableComponent(enumValue.getDescriptionTranslationKey()), maxWidth));
        c.add(new TextComponent("").getVisualOrderText());
        c.addAll(Arrays.stream(enumClass.getEnumConstants()).map((tr) -> {
            return new TextComponent(String.format("§l> %s§r§7\n%s", new TranslatableComponent(tr.getTranslationKey()).getString(), new TranslatableComponent(tr.getInfoTranslationKey()).getString()));
        }).map((x) -> s.getMinecraft().font.split(x, maxWidth)).flatMap(List::stream).collect(Collectors.toList()));
        
        return c;
    }

    @SuppressWarnings("resource")
    public static List<FormattedCharSequence> getTooltipData(Screen s, Component c, int maxWidth) {
        return s.getMinecraft().font.split(c, maxWidth);
    }

    @SuppressWarnings("resource")
    public static <W extends AbstractWidget> void renderTooltip(Screen s, W w, Supplier<List<FormattedCharSequence>> lines, PoseStack stack, int mouseX, int mouseY) {
        if (w.isMouseOver(mouseX, mouseY)) {
            s.renderTooltip(stack, lines.get(), mouseX, mouseY, s.getMinecraft().font);
        }
    }

    

    public static double calculateOggDuration(final String filePath) throws IOException {
        final File oggFile = new File(filePath);
        
        int size = (int) oggFile.length();
        byte[] t = new byte[size];
        
        try (FileInputStream stream = new FileInputStream(oggFile)) {
            stream.read(t);
        }

        return calculateOggDuration(t);
    }

    public static double calculateOggDuration(final byte[] data) {
        int rate = -1;
        int length = -1;

        for (int i = data.length - 1 - 8 - 2 - 4; i >= 0 && length < 0; i--) {
            if (isMatch(data, i, "OggS")) {
                byte[] byteArray = extractByteArray(data, i + 6, 8);
                length = extractIntLittleEndian(byteArray);
            }
        }

        for (int i = 0; i < data.length - 8 - 2 - 4 && rate < 0; i++) {
            if (isMatch(data, i, "vorbis")) {
                byte[] byteArray = extractByteArray(data, i + 11, 4);
                rate = extractIntLittleEndian(byteArray);
            }
        }

        double duration = (double) length / (double) rate;
        return duration;
    }

    public static final LocalTime formattedDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        return LocalTime.of(hours, minutes, secs);
    }

    private static boolean isMatch(byte[] array, int startIndex, String pattern) {
        for (int i = 0; i < pattern.length(); i++) {
            if (array[startIndex + i] != pattern.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static byte[] extractByteArray(byte[] array, int startIndex, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, startIndex, result, 0, length);
        return result;
    }

    private static int extractIntLittleEndian(byte[] byteArray) {
        ByteBuffer bb = ByteBuffer.wrap(byteArray);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public static void giveAdvancement(ServerPlayer player, String name, String criteriaKey) {
        Advancement adv = player.getServer().getAdvancements().getAdvancement(new ResourceLocation(ModMain.MOD_ID, name));
        player.getAdvancements().award(adv, criteriaKey);
    }

}
