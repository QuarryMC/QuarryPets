package codes.kooper.quarryPets.commands;

import codes.kooper.quarryPets.guis.EggIndexGui;
import codes.kooper.shaded.litecommands.annotations.command.Command;
import codes.kooper.shaded.litecommands.annotations.context.Context;
import codes.kooper.shaded.litecommands.annotations.execute.Execute;
import org.bukkit.entity.Player;

@Command(name = "petindex", aliases = {"petlist", "petsindex", "listpets"})
public class PetIndexCommand {

    @Execute
    public void onPetIndex(@Context Player player) {
        new EggIndexGui(player);
    }
}