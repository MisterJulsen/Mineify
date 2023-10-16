package de.mrjulsen.mineify.commands;

import com.google.gson.JsonObject;

import de.mrjulsen.mineify.sound.ESoundCategory;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class SoundArgumentInfo implements ArgumentTypeInfo<SoundsArgument, SoundArgumentInfo.Template> {
   public void serializeToNetwork(SoundArgumentInfo.Template pTemplate, FriendlyByteBuf pBuffer) {
        pBuffer.writeInt(pTemplate.category.getIndex());
   }

   public SoundArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
      ESoundCategory category = ESoundCategory.getCategoryByIndex(pBuffer.readInt());
      return new SoundArgumentInfo.Template(category);
   }

   public void serializeToJson(SoundArgumentInfo.Template pTemplate, JsonObject pJson) {
        pJson.addProperty("category", pTemplate.category.getIndex());

   }

   public SoundArgumentInfo.Template unpack(SoundsArgument pArgument) {
      return new SoundArgumentInfo.Template(pArgument.getCategory());
   }

   public final class Template implements ArgumentTypeInfo.Template<SoundsArgument> {
      final ESoundCategory category;

      Template(ESoundCategory category) {
         this.category = category;
      }

      public SoundsArgument instantiate(CommandBuildContext pContext) {
         return SoundsArgument.soundsArg(this.category);
      }

      public ArgumentTypeInfo<SoundsArgument, ?> type() {
         return SoundArgumentInfo.this;
      }
   }
}
