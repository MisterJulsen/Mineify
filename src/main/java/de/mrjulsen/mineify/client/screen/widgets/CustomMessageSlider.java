package de.mrjulsen.mineify.client.screen.widgets;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;

public class CustomMessageSlider extends ForgeSlider {

    private final Consumer<CustomMessageSlider> onUpdateMessage;

    public CustomMessageSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString, Consumer<CustomMessageSlider> onUpdateMessage) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, drawString);
        this.onUpdateMessage = onUpdateMessage;
    }

    public CustomMessageSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString, Consumer<CustomMessageSlider> onUpdateMessage) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize, precision, drawString);
        this.onUpdateMessage = onUpdateMessage;
        this.updateMessage();
    }
    
    @Override
    protected void updateMessage() {
        if (onUpdateMessage == null) {
            super.updateMessage();
            return;
        }
        
        onUpdateMessage.accept(this);
    }
    
}
