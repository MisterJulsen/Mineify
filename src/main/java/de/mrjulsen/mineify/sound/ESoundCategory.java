package de.mrjulsen.mineify.sound;

import de.mrjulsen.mineify.util.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum ESoundCategory implements StringRepresentable, ITranslatableEnum {
	DEFAULT(0, "default"),
    SOUND_BOARD(1, "sound_board");
	
	private String name;
	private int index;
	
	private ESoundCategory(int index, String name) {
		this.name = name;
		this.index = index;
	}
	
	public String getCategoryName() {
		return this.name;
	}

	public int getIndex() {
		return this.index;
	}

	public static ESoundCategory getCategoryByIndex(int count) {
		for (ESoundCategory shape : ESoundCategory.values()) {
			if (shape.getIndex() == count) {
				return shape;
			}
		}
		return ESoundCategory.DEFAULT;
	}

    public static ESoundCategory getCategoryByName(String name) {
		for (ESoundCategory shape : ESoundCategory.values()) {
			if (shape.getCategoryName().equals(name)) {
				return shape;
			}
		}
		return ESoundCategory.DEFAULT;
	}

    public String getPath() {
        return this == DEFAULT ? "" : this.getCategoryName();
    }

    public String getPathWithSeparatorSuffix() {
        return this == DEFAULT ? "" : this.getCategoryName() + "/";
    }

	public String getPathWithSeparatorPrefix() {
        return this == DEFAULT ? "" :  "/" + this.getCategoryName();
    }

    @Override
    public String getSerializedName() {
        return name;
    }

	@Override
	public String getNameOfEnum() {
		return "sound_category";
	}

	@Override
	public String getValue() {
		return this.name;
	}
}
