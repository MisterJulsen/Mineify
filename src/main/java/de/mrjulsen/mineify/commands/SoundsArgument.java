package de.mrjulsen.mineify.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.SoundUtils;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;

public class SoundsArgument implements ArgumentType<SoundFile> {
    private static final Collection<String> EXAMPLES = Arrays.asList("\"Server.test\"");

    private static final DynamicCommandExceptionType PATH_TOO_SHORT = new DynamicCommandExceptionType((obj) -> {
        return new TextComponent("Invalid path. Too few arguments.");
    });
    private static final DynamicCommandExceptionType INVALID_PATH = new DynamicCommandExceptionType((obj) -> {
        return new TextComponent("Invalid path. File does not exist.");
    });

    private SoundsArgument() {
    }

    public static SoundsArgument soundsArg() {
        return new SoundsArgument();
    }

    public static SoundFile getSound(final CommandContext<?> context, final String name) {
        return context.getArgument(name, SoundFile.class);
    }

    @Override
    public SoundFile parse(final StringReader reader) throws CommandSyntaxException {
        String result = reader.readQuotedString();
        String[] data = result.split("/");

        if (data.length < 2) {
            throw PATH_TOO_SHORT.createWithContext(reader, result);
        }

        SoundFile file = null;
        if (data.length == 3) {
            file = new SoundFile(data[2], data[1], ESoundVisibility.SERVER, ESoundCategory.getCategoryByName(data[0]));
        } else {
            file = new SoundFile(data[3], data[1], ESoundVisibility.getVisibilityByName(data[2]), ESoundCategory.getCategoryByName(data[0]));
        }

        if (!file.exists()) {
            throw INVALID_PATH.createWithContext(reader, result);
        }

        return file;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        if (!(pContext.getSource() instanceof SharedSuggestionProvider)) {
            return Suggestions.empty();
        } else {
            return SharedSuggestionProvider.suggest(Arrays.stream(SoundUtils.readSoundsFromDisk(null, null, null)).map(x -> {
                return String.format("\"%s\"", x.buildShortPath());
            }), pBuilder);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
