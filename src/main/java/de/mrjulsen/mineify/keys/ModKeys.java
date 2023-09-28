package de.mrjulsen.mineify.keys;

import com.mojang.blaze3d.platform.InputConstants;

import de.mrjulsen.mineify.ModMain;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;

public class ModKeys {
    private ModKeys() {

    }

    public static KeyMapping soundBoardKeyMapping;

    private static KeyMapping registerKey(String name, String category, int keycode) {
        KeyMapping key = new KeyMapping("key." + ModMain.MOD_ID + "." + name, keycode, "key." + ModMain.MOD_ID + ".category." + category);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }

    public static void init() {
        soundBoardKeyMapping = registerKey("sound_board", "mineify", InputConstants.KEY_Y);
    }
}
