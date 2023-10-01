package de.mrjulsen.mineify.commands;

import java.util.Collection;
import java.util.Iterator;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.api.ServerApi;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.sound.SoundFile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class SoundCommand {

    public SoundCommand(CommandDispatcher<CommandSourceStack> pDispatcher) {

        LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("sound").requires((stack) -> {
            return stack.hasPermission(2);
        });
        LiteralArgumentBuilder<CommandSourceStack> literalPlay = Commands.literal("play");
        LiteralArgumentBuilder<CommandSourceStack> literalModify = Commands.literal("modify");
        LiteralArgumentBuilder<CommandSourceStack> literalStop = Commands.literal("stop");

        for (ESoundCategory category : ESoundCategory.values()) {
            literalPlay.then(Commands.literal(category.getCategoryName())
            .then(Commands.argument("soundFile", SoundsArgument.soundsArg(category))
            .then(Commands.argument("targets", EntityArgument.players()).executes((command) -> {
                return playSound(command.getSource(),
                    EntityArgument.getPlayers(command, "targets"),
                    SoundsArgument.getSound(command, "soundFile"),
                    command.getSource().getPosition(),
                    Constants.ATTENUATION_DISTANCE_DEFAULT,
                    Constants.VOLUME_DEFAULT,
                    Constants.PITCH_DEFAULT,
                    0);
            }).then(Commands.argument("pos", Vec3Argument.vec3()).executes((command) -> {
                return playSound(command.getSource(),
                    EntityArgument.getPlayers(command, "targets"),
                    SoundsArgument.getSound(command, "soundFile"),
                    Vec3Argument.getVec3(command, "pos"),
                    Constants.ATTENUATION_DISTANCE_DEFAULT,
                    Constants.VOLUME_DEFAULT,
                    Constants.PITCH_DEFAULT,
                    0);
            }).then(Commands.argument("volume", FloatArgumentType.floatArg(0.0F)).executes((command) -> {
                return playSound(command.getSource(),
                    EntityArgument.getPlayers(command, "targets"),
                    SoundsArgument.getSound(command, "soundFile"),
                    Vec3Argument.getVec3(command, "pos"),
                    Constants.ATTENUATION_DISTANCE_DEFAULT,
                    command.getArgument("volume", Float.class),
                    Constants.PITCH_DEFAULT,
                    0);
            }).then(Commands.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F)).executes((command) -> {
                return playSound(command.getSource(),
                    EntityArgument.getPlayers(command, "targets"),
                    SoundsArgument.getSound(command, "soundFile"),
                    Vec3Argument.getVec3(command, "pos"),
                    Constants.ATTENUATION_DISTANCE_DEFAULT,
                    command.getArgument("volume", Float.class),
                    command.getArgument("pitch", Float.class),
                    0);
            }).then(Commands.argument("attenuationDistance", IntegerArgumentType.integer(1)).executes((command) -> {
                return playSound(command.getSource(),
                    EntityArgument.getPlayers(command, "targets"),
                    SoundsArgument.getSound(command, "soundFile"),
                    Vec3Argument.getVec3(command, "pos"),
                    command.getArgument("attenuationDistance", Integer.class),
                    command.getArgument("volume", Float.class),
                    command.getArgument("pitch", Float.class),
                    0);
            }).then(Commands.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F)).executes((command) -> {
                return playSound(command.getSource(),
                    EntityArgument.getPlayers(command, "targets"),
                    SoundsArgument.getSound(command, "soundFile"),
                    Vec3Argument.getVec3(command, "pos"),
                    command.getArgument("attenuationDistance", Integer.class),
                    command.getArgument("volume", Float.class),
                    command.getArgument("pitch", Float.class),
                    command.getArgument("minVolume", Float.class));
            })))))))));
            
            // Stop
            literalStop.then(Commands.argument("targets", EntityArgument.players()).executes((command) -> {
                return stopSound(command.getSource(), null, null, EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new));
            }).then(Commands.literal(category.getCategoryName()).then(Commands.argument("soundFile", SoundsArgument.soundsArg(category)).executes((command) -> {
                return stopSound(command.getSource(), SoundsArgument.getSound(command, "soundFile").buildShortPath(), category, EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new));
            }))));

            // Modify
            literalModify.then(Commands.argument("targets", EntityArgument.players())
            .then(Commands.literal(category.getCategoryName())
            .then(Commands.argument("soundFile", SoundsArgument.soundsArg(category))
            .then(Commands.literal("setAttenuationDistance").then(Commands.argument("value", IntegerArgumentType.integer(1)).executes((command) -> {
                return modifySoundAttenuationDistance(
                    command.getSource(),
                    SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                    category,
                    EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                    IntegerArgumentType.getInteger(command, "value"));
            })))
            .then(Commands.literal("setPitch").then(Commands.argument("value", FloatArgumentType.floatArg(Constants.PITCH_MIN, Constants.PITCH_MAX)).executes((command) -> {
                return modifySoundPitch(
                    command.getSource(),
                    SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                    category,
                    EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                    FloatArgumentType.getFloat(command, "value"));
            })))
            .then(Commands.literal("setVolume").then(Commands.argument("value", FloatArgumentType.floatArg(Constants.VOLUME_MIN, Constants.VOLUME_MAX)).executes((command) -> {
                return modifySoundVolume(
                    command.getSource(),
                    SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                    category,
                    EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                    FloatArgumentType.getFloat(command, "value"));
            })))
            .then(Commands.literal("setX").then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes((command) -> {
                return modifySoundX(
                    command.getSource(),
                    SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                    category,
                    EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                    DoubleArgumentType.getDouble(command, "value"));
            })))
            .then(Commands.literal("setY").then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes((command) -> {
                return modifySoundY(
                    command.getSource(),
                    SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                    category,
                    EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                    DoubleArgumentType.getDouble(command, "value"));
            })))
            .then(Commands.literal("setZ").then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes((command) -> {
                return modifySoundZ(
                    command.getSource(),
                    SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                    category,
                    EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                    DoubleArgumentType.getDouble(command, "value"));
            })))
            .then(Commands.literal("setPos").then(Commands.argument("value", Vec3Argument.vec3()).executes((command) -> {
                return modifySoundPos(
                    command.getSource(),
                    SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                    category,
                    EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                    Vec3Argument.getVec3(command, "value"));
            }))))));
        }

       


        pDispatcher.register(literalargumentbuilder.then(literalPlay)
          .then(literalStop)
          .then(literalModify)          
        );
    }

    private static int modifySoundAttenuationDistance(CommandSourceStack pSource, String shortPath, ESoundCategory category, ServerPlayer[] pTargets, int attenuationDistance) {
        ServerApi.modifySound(shortPath, pTargets, attenuationDistance, null, null, null, null, null);
        pSource.sendSuccess(new TranslatableComponent("commands.mineify.sound.modify", SoundFile.fromShortPath(shortPath, category)), true);
        return pTargets.length;
    }

    private static int modifySoundVolume(CommandSourceStack pSource, String shortPath, ESoundCategory category, ServerPlayer[] pTargets, float volume) {
        ServerApi.modifySound(shortPath, pTargets, null, volume, null, null, null, null);
        pSource.sendSuccess(new TranslatableComponent("commands.mineify.sound.modify", SoundFile.fromShortPath(shortPath, category)), true);
        return pTargets.length;
    }

    private static int modifySoundPitch(CommandSourceStack pSource, String shortPath, ESoundCategory category, ServerPlayer[] pTargets, float pitch) {
        ServerApi.modifySound(shortPath, pTargets, null, null, pitch, null, null, null);
        pSource.sendSuccess(new TranslatableComponent("commands.mineify.sound.modify", SoundFile.fromShortPath(shortPath, category)), true);
        return pTargets.length;
    }

    private static int modifySoundX(CommandSourceStack pSource, String shortPath, ESoundCategory category, ServerPlayer[] pTargets, double x) {
        ServerApi.modifySound(shortPath, pTargets, null, null, null, x, null, null);
        pSource.sendSuccess(new TranslatableComponent("commands.mineify.sound.modify", SoundFile.fromShortPath(shortPath, category)), true);
        return pTargets.length;
    }

    private static int modifySoundY(CommandSourceStack pSource, String shortPath, ESoundCategory category, ServerPlayer[] pTargets, double y) {
        ServerApi.modifySound(shortPath, pTargets, null, null, null, null, y, null);
        pSource.sendSuccess(new TranslatableComponent("commands.mineify.sound.modify", SoundFile.fromShortPath(shortPath, category)), true);
        return pTargets.length;
    }

    private static int modifySoundZ(CommandSourceStack pSource, String shortPath, ESoundCategory category, ServerPlayer[] pTargets, double z) {
        ServerApi.modifySound(shortPath, pTargets, null, null, null, null, null, z);
        pSource.sendSuccess(new TranslatableComponent("commands.mineify.sound.modify", SoundFile.fromShortPath(shortPath, category)), true);
        return pTargets.length;
    }

    private static int modifySoundPos(CommandSourceStack pSource, String shortPath, ESoundCategory category, ServerPlayer[] pTargets, Vec3 v) {
        ServerApi.modifySound(shortPath, pTargets, null, null, null, v.x, v.y, v.z);
        pSource.sendSuccess(new TranslatableComponent("commands.mineify.sound.modify", SoundFile.fromShortPath(shortPath, category)), true);
        return pTargets.length;
    }

    private static int stopSound(CommandSourceStack pSource, String shortPath, ESoundCategory category, ServerPlayer[] pTargets) {
        ServerApi.stopSound(shortPath, pTargets);
        if (shortPath != null) {
            pSource.sendSuccess(new TranslatableComponent("commands.stopsound.success.sourceless.sound", SoundFile.fromShortPath(shortPath, category)), true);
        } else {
            pSource.sendSuccess(new TranslatableComponent("commands.stopsound.success.sourceless.any"), true);
        }
        return pTargets.length;
    }

    private static int playSound(CommandSourceStack pSource, Collection<ServerPlayer> pTargets, SoundFile pSound, Vec3 pPos, int attenuationDistance, float pVolume, float pPitch, float pMinVolume) throws CommandSyntaxException {
        double d0 = Math.pow(pVolume > 1.0F ? (double) (pVolume * 16.0F) : 16.0D, 2.0D);
        int i = 0;
        Iterator<?> iterator = pTargets.iterator();

        while (true) {
            ServerPlayer serverplayer;
            Vec3 vec3;
            float f;
            while (true) {
                if (!iterator.hasNext()) {
                    if (i == 0) {
                        return 0;
                    }

                    if (pTargets.size() == 1) {
                        pSource.sendSuccess(new TranslatableComponent("commands.playsound.success.single", pSound, pTargets.iterator().next().getDisplayName()), true);
                    } else {
                        pSource.sendSuccess(new TranslatableComponent("commands.playsound.success.multiple", pSound, pTargets.size()), true);
                    }

                    return i;
                }

                serverplayer = (ServerPlayer) iterator.next();
                double d1 = pPos.x - serverplayer.getX();
                double d2 = pPos.y - serverplayer.getY();
                double d3 = pPos.z - serverplayer.getZ();
                double d4 = d1 * d1 + d2 * d2 + d3 * d3;
                vec3 = pPos;
                f = pVolume;
                if (!(d4 > d0)) {
                    break;
                }

                if (!(pMinVolume <= 0.0F)) {
                    double d5 = Math.sqrt(d4);
                    vec3 = new Vec3(serverplayer.getX() + d1 / d5 * 2.0D, serverplayer.getY() + d2 / d5 * 2.0D, serverplayer.getZ() + d3 / d5 * 2.0D);
                    f = pMinVolume;
                    break;
                }
            }

            ServerApi.playSound(pSound, new ServerPlayer[] { serverplayer }, new BlockPos(vec3), attenuationDistance, f, pPitch);
            ++i;
        }
    }

}
