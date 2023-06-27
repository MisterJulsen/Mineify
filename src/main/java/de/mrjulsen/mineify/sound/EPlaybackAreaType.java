package de.mrjulsen.mineify.sound;

import de.mrjulsen.mineify.util.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum EPlaybackAreaType implements StringRepresentable, ITranslatableEnum {
	RADIUS((byte)1, "radius"),
    ZONE((byte)2, "zone");
	
	private String name;
	private byte index;
	
	private EPlaybackAreaType(byte index, String name) {
		this.name = name;
		this.index = index;
	}
	
	public String getChannelName() {
		return this.name;
	}

	public byte getIndex() {
		return this.index;
	}

	public static EPlaybackAreaType getPlaybackAreaTypeByIndex(byte count) {
		for (EPlaybackAreaType shape : EPlaybackAreaType.values()) {
			if (shape.getIndex() == count) {
				return shape;
			}
		}
		return EPlaybackAreaType.RADIUS;
	}

    @Override
    public String getSerializedName() {
        return name;
    }

	@Override
	public String getNameOfEnum() {
		return "playback_area_type";
	}

	@Override
	public String getValue() {
		return this.name;
	}
}
