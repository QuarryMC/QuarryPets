package codes.kooper.quarryPets.commands;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.guis.EggIndexGui;
import codes.kooper.shaded.litecommands.annotations.command.Command;
import codes.kooper.shaded.litecommands.annotations.context.Context;
import codes.kooper.shaded.litecommands.annotations.execute.Execute;
import codes.kooper.shaded.litecommands.annotations.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static codes.kooper.koopKore.KoopKore.textUtils;

@Command(name = "petindex", aliases = {"petlist", "petsindex", "listpets"})
public class PetIndexCommand {

    @Execute
    public void onPetIndex(@Context Player player) {
        new EggIndexGui(player);
    }

    @Execute(name = "reload")
    @Permission("pets.reload")
    public void onReload(@Context CommandSender sender) {
        QuarryPets.getInstance().onReload();
        sender.sendMessage(textUtils.colorize("<green>Pets reloaded!"));
    }
}