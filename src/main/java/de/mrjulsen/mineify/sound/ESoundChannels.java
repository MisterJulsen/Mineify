package de.mrjulsen.mineify.sound;

import de.mrjulsen.mineify.util.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum ESoundChannels implements StringRepresentable, ITranslatableEnum {
	MONO(1, "mono"),
    STEREO(2, "stereo");
	
	private String name;
	private int count;
	
	private ESoundChannels(int count, String name) {
		this.name = name;
		this.count = count;
	}
	
	public String getChannelName() {
		return this.name;
	}

	public int getCount() {
		return this.count;
	}	

	public static ESoundChannels getChannelByCount(int count) {
		for (ESoundChannels shape : ESoundChannels.values()) {
			if (shape.getCount() == count) {
				return shape;
			}
		}
		return ESoundChannels.MONO;
	}

    @Override
    public String getSerializedName() {
        return name;
    }

	@Override
	public String getNameOfEnum() {
		return "sound_channels";
	}

	@Override
	public String getValue() {
		return this.name;
	}
}
