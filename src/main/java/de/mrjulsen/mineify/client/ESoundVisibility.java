package de.mrjulsen.mineify.client;

import de.mrjulsen.mineify.util.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum ESoundVisibility implements StringRepresentable, ITranslatableEnum {
	SERVER(-1, "server", "textures/item/music_disc_chirp.png"),
    PRIVATE(0, "private", "textures/item/music_disc_far.png"),
	SHARED(1, "shared", "textures/item/music_disc_mall.png"),
	PUBLIC(2, "public", "textures/item/music_disc_13.png");
	
	private String name;
	private int index;
	private String icon;
	
	private ESoundVisibility(int index, String name, String icon) {
		this.name = name;
		this.index = index;
		this.icon = icon;
	}
	
	public String getName() {
		return this.name;
	}

	public int getIndex() {
		return this.index;
	}

	public String getIconFileName() {
		return this.icon;
	}

	public static ESoundVisibility getVisibilityByIndex(int index) {
		for (ESoundVisibility shape : ESoundVisibility.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return ESoundVisibility.PRIVATE;
	}

	public EUserSoundVisibility toEUserSoundVisibility() {
		return this.getIndex() > -1 ? EUserSoundVisibility.getVisibilityByIndex(this.getIndex()) : null;
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
