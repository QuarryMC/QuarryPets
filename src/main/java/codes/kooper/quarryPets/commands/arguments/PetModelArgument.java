package codes.kooper.quarryPets.commands.arguments;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.quarryPets.models.PetModel;
import codes.kooper.shaded.litecommands.argument.Argument;
import codes.kooper.shaded.litecommands.argument.parser.ParseResult;
import codes.kooper.shaded.litecommands.argument.resolver.ArgumentResolver;
import codes.kooper.shaded.litecommands.invocation.Invocation;
import codes.kooper.shaded.litecommands.suggestion.SuggestionContext;
import codes.kooper.shaded.litecommands.suggestion.SuggestionResult;
import org.bukkit.command.CommandSender;

public class PetModelArgument extends ArgumentResolver<CommandSender, PetModel> {

    @Override
    protected ParseResult<PetModel> parse(Invocation<CommandSender> invocation, Argument<PetModel> argument, String s) {
        try {
            PetModel petModel = QuarryPets.getInstance().getPetManager().getPetModel(s);
            if (petModel == null) {
                return ParseResult.failure("Pet model could not be found.");
            }
            return ParseResult.success(petModel);
        } catch (Exception e) {
            return ParseResult.failure("An error has occurred while parsing this pet model.");
        }
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<PetModel> argument, SuggestionContext context) {
        return SuggestionResult.of(QuarryPets.getInstance().getPetManager().getPetNames());
    }
}
