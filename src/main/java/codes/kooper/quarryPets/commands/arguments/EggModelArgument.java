package codes.kooper.quarryPets.commands.arguments;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.shaded.litecommands.argument.Argument;
import codes.kooper.shaded.litecommands.argument.parser.ParseResult;
import codes.kooper.shaded.litecommands.argument.resolver.ArgumentResolver;
import codes.kooper.shaded.litecommands.invocation.Invocation;
import codes.kooper.shaded.litecommands.suggestion.SuggestionContext;
import codes.kooper.shaded.litecommands.suggestion.SuggestionResult;
import org.bukkit.command.CommandSender;

public class EggModelArgument extends ArgumentResolver<CommandSender, EggModel> {

    @Override
    protected ParseResult<EggModel> parse(Invocation<CommandSender> invocation, Argument<EggModel> argument, String s) {
        try {
            EggModel eggModel = QuarryPets.getInstance().getEggManager().getEgg(s.toLowerCase());
            if (eggModel == null) {
                return ParseResult.failure("Egg model could not be found.");
            }
            return ParseResult.success(eggModel);
        } catch (Exception e) {
            return ParseResult.failure("An error has occurred while parsing this egg model.");
        }
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<EggModel> argument, SuggestionContext context) {
        return SuggestionResult.of(QuarryPets.getInstance().getEggManager().getEggs().keySet());
    }
}
