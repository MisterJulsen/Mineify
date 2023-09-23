package de.mrjulsen.mineify.commands;

import java.util.Collection;
import java.util.Iterator;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.mrjulsen.mineify.api.ServerApi;
import de.mrjulsen.mineify.sound.SoundFile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class SoundCommand {

    public SoundCommand(CommandDispatcher<CommandSourceStack> pDispatcher) {

        RequiredArgumentBuilder<CommandSourceStack, SoundFile> playArguments =
        Commands.argument("soundFile", SoundsArgument.soundsArg())
        .then(Commands.argument("targets", EntityArgument.players()).executes((command) -> {
            return playSound(command.getSource(),
                EntityArgument.getPlayers(command, "targets"),
                SoundsArgument.getSound(command, "soundFile"),
                command.getSource().getPosition(),
                1,
                1,
                0);
        }).then(Commands.argument("pos", Vec3Argument.vec3()).executes((command) -> {
            return playSound(command.getSource(),
                EntityArgument.getPlayers(command, "targets"),
                SoundsArgument.getSound(command, "soundFile"),
                Vec3Argument.getVec3(command, "pos"),
                1,
                1,
                0);
        }).then(Commands.argument("volume", FloatArgumentType.floatArg(0.0F)).executes((command) -> {
            return playSound(command.getSource(),
                EntityArgument.getPlayers(command, "targets"),
                SoundsArgument.getSound(command, "soundFile"),
                Vec3Argument.getVec3(command, "pos"),
                command.getArgument("volume", Float.class),
                1,
                0);
        }).then(Commands.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F)).executes((command) -> {
            return playSound(command.getSource(),
                EntityArgument.getPlayers(command, "targets"),
                SoundsArgument.getSound(command, "soundFile"),
                Vec3Argument.getVec3(command, "pos"),
                command.getArgument("volume", Float.class),
                command.getArgument("pitch", Float.class),
                0);
        }).then(Commands.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F)).executes((command) -> {
            return playSound(command.getSource(),
                EntityArgument.getPlayers(command, "targets"),
                SoundsArgument.getSound(command, "soundFile"),
                Vec3Argument.getVec3(command, "pos"),
                command.getArgument("volume", Float.class),
                command.getArgument("pitch", Float.class),
                command.getArgument("minVolume", Float.class));
        }))))));

        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> stopArguments =
        Commands.argument("targets", EntityArgument.players()).executes((command) -> {
            return stopSound(command.getSource(), null, EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new));
        }).then(Commands.argument("soundFile", SoundsArgument.soundsArg()).executes((command) -> {
            return stopSound(command.getSource(), SoundsArgument.getSound(command, "soundFile").buildShortPath(), EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new));
        }));


        pDispatcher.register(Commands.literal("sound").requires((sourceStack) -> {
            return sourceStack.hasPermission(2);
        }).then(Commands.literal("play").then(playArguments))
          .then(Commands.literal("stop").executes((command) -> {
            return stopSound(command.getSource(), null, new ServerPlayer[] { command.getSource().getPlayerOrException() });
        }).then(stopArguments))
          .then(Commands.literal("modify")
          .then(Commands.argument("targets", EntityArgument.players())
          .then(Commands.argument("soundFile", SoundsArgument.soundsArg())
          .then(Commands.literal("setVolume").then(Commands.argument("value", FloatArgumentType.floatArg(0.0F)).executes((command) -> {
            return modifySoundVolume(
                command.getSource(),
                SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                FloatArgumentType.getFloat(command, "value"));
          })))
          .then(Commands.literal("setPitch").then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, 2.0F)).executes((command) -> {
            return modifySoundPitch(
                command.getSource(),
                SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                FloatArgumentType.getFloat(command, "value"));
          })))
          .then(Commands.literal("setX").then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes((command) -> {
            return modifySoundX(
                command.getSource(),
                SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                DoubleArgumentType.getDouble(command, "value"));
          })))
          .then(Commands.literal("setY").then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes((command) -> {
            return modifySoundY(
                command.getSource(),
                SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                DoubleArgumentType.getDouble(command, "value"));
          })))
          .then(Commands.literal("setZ").then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes((command) -> {
            return modifySoundZ(
                command.getSource(),
                SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                DoubleArgumentType.getDouble(command, "value"));
          })))
          .then(Commands.literal("setPos").then(Commands.argument("value", Vec3Argument.vec3()).executes((command) -> {
            return modifySoundPos(
                command.getSource(),
                SoundsArgument.getSound(command, "soundFile").buildShortPath(),
                EntityArgument.getPlayers(command, "targets").toArray(ServerPlayer[]::new),
                Vec3Argument.getVec3(command, "value"));
          })))
        ))));
    }

    private static int modifySoundVolume(CommandSourceStack pSource, String shortPath, ServerPlayer[] pTargets, float volume) {
        ServerApi.modifySound(shortPath, pTargets, volume, null, null, null, null);
        return pTargets.length;
    }

    private static int modifySoundPitch(CommandSourceStack pSource, String shortPath, ServerPlayer[] pTargets, float pitch) {
        ServerApi.modifySound(shortPath, pTargets, null, pitch, null, null, null);
        return pTargets.length;
    }

    private static int modifySoundX(CommandSourceStack pSource, String shortPath, ServerPlayer[] pTargets, double x) {
        ServerApi.modifySound(shortPath, pTargets, null, null, x, null, null);
        return pTargets.length;
    }

    private static int modifySoundY(CommandSourceStack pSource, String shortPath, ServerPlayer[] pTargets, double y) {
        ServerApi.modifySound(shortPath, pTargets, null, null, null, y, null);
        return pTargets.length;
    }

    private static int modifySoundZ(CommandSourceStack pSource, String shortPath, ServerPlayer[] pTargets, double z) {
        ServerApi.modifySound(shortPath, pTargets, null, null, null, null, z);
        return pTargets.length;
    }

    private static int modifySoundPos(CommandSourceStack pSource, String shortPath, ServerPlayer[] pTargets, Vec3 v) {
        ServerApi.modifySound(shortPath, pTargets, null, null, v.x, v.y, v.z);
        return pTargets.length;
    }

    private static int stopSound(CommandSourceStack pSource, String shortPath, ServerPlayer[] pTargets) {
        ServerApi.stopSound(shortPath, pTargets);
        if (shortPath != null) {
            pSource.sendSuccess(new TranslatableComponent("commands.stopsound.success.sourceless.sound", SoundFile.fromShortPath(shortPath)), true);
        } else {
            pSource.sendSuccess(new TranslatableComponent("commands.stopsound.success.sourceless.any"), true);
        }
        return pTargets.length;
    }

    private static int playSound(CommandSourceStack pSource, Collection<ServerPlayer> pTargets, SoundFile pSound, Vec3 pPos, float pVolume, float pPitch, float pMinVolume) throws CommandSyntaxException {
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
                    vec3 = new Vec3(serverplayer.getX() + d1 / d5 * 2.0D, serverplayer.getY() + d2 / d5 * 2.0D,
                            serverplayer.getZ() + d3 / d5 * 2.0D);
                    f = pMinVolume;
                    break;
                }
            }

            ServerApi.playSound(pSound, new ServerPlayer[] { serverplayer }, new BlockPos(vec3), f, pPitch);
            ++i;
        }
    }

}
