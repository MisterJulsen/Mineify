package de.mrjulsen.mineify.client;

import de.mrjulsen.mineify.util.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum EUserSoundVisibility implements StringRepresentable, ITranslatableEnum {
    PRIVATE(0, "private"),
	SHARED(1, "shared"),
	PUBLIC(2, "public");
	
	private String name;
	private int index;
	
	private EUserSoundVisibility(int index, String name) {
		this.name = name;
		this.index = index;
	}
	
	public String getName() {
		return this.name;
	}

	public int getIndex() {
		return this.index;
	}

	public static EUserSoundVisibility getVisibilityByIndex(int index) {
		for (EUserSoundVisibility shape : EUserSoundVisibility.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return EUserSoundVisibility.PRIVATE;
	}

	public ESoundVisibility toESoundVisibility() {
		return ESoundVisibility.getVisibilityByIndex(this.getIndex());
	}

    @Override
    public String getSerializedName() {
        return name;
    }

	@Override
	public String getNameOfEnum() {
		return "sound_visibility";
	}

	@Override
	public String getValue() {
		return this.name;
	}
}
