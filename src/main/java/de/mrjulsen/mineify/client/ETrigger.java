package de.mrjulsen.mineify.client;

import de.mrjulsen.mineify.util.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum ETrigger implements StringRepresentable, ITranslatableEnum {
    NONE((byte)0, "none"),
	CONTINUOUS_REDSTONE((byte)1, "continuous_redstone"),
	REDSTONE_IMPULSE((byte)2, "redstone_impulse");
	
	private String name;
	private byte index;
	
	private ETrigger(byte index, String name) {
		this.name = name;
		this.index = index;
	}
	
	public String getName() {
		return this.name;
	}

	public byte getIndex() {
		return this.index;
	}

	public static ETrigger getTriggerByIndex(byte index) {
		for (ETrigger shape : ETrigger.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return ETrigger.NONE;
	}

    @Override
    public String getSerializedName() {
        return name;
    }

	@Override
	public String getNameOfEnum() {
		return "trigger";
	}

	@Override
	public String getValue() {
		return this.name;
	}
}
