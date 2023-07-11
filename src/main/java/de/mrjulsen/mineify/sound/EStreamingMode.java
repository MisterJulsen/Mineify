package de.mrjulsen.mineify.sound;

import net.minecraft.util.StringRepresentable;

public enum EStreamingMode implements StringRepresentable {
	ALL_AT_ONCE((byte)1, "all_at_once"),
    ON_REQUEST((byte)2, "on_request");
	
	private String name;
	private byte index;
	
	private EStreamingMode(byte index, String name) {
		this.name = name;
		this.index = index;
	}
	
	public String getName() {
		return this.name;
	}

	public byte getIndex() {
		return this.index;
	}

	public static EStreamingMode getStreamingModeByIndex(byte count) {
		for (EStreamingMode shape : EStreamingMode.values()) {
			if (shape.getIndex() == count) {
				return shape;
			}
		}
		return EStreamingMode.ALL_AT_ONCE;
	}

    @Override
    public String getSerializedName() {
        return name;
    }
}
