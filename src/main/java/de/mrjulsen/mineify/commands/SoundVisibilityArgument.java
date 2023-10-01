package de.mrjulsen.mineify.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.mrjulsen.mineify.client.ESoundVisibility;
import net.minecraft.commands.SharedSuggestionProvider;

public class SoundVisibilityArgument implements ArgumentType<ESoundVisibility> {
    private static final Collection<String> EXAMPLES = Arrays.asList("ESoundVisibility.PRIVATE");

    private SoundVisibilityArgument() {
    }

    public static SoundVisibilityArgument visibilityArg() {
        return new SoundVisibilityArgument();
    }

    public static ESoundVisibility getVisibility(final CommandContext<?> context, final String name) {
        return context.getArgument(name, ESoundVisibility.class);
    }

    @Override
    public ESoundVisibility parse(final StringReader reader) throws CommandSyntaxException {
        final String result = reader.readString();
        return ESoundVisibility.getVisibilityByName(result);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        if (!(pContext.getSource() instanceof SharedSuggestionProvider)) {
           return Suggestions.empty();
        } else {  
           return SharedSuggestionProvider.suggest(Arrays.stream(ESoundVisibility.values()).map(x -> x.getName()), pBuilder);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
