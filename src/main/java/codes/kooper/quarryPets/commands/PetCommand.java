package codes.kooper.quarryPets.commands;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.models.Pet;
import codes.kooper.quarryPets.guis.PetGui;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.quarryPets.models.PetModel;
import codes.kooper.shaded.litecommands.annotations.argument.Arg;
import codes.kooper.shaded.litecommands.annotations.command.Command;
import codes.kooper.shaded.litecommands.annotations.context.Context;
import codes.kooper.shaded.litecommands.annotations.execute.Execute;
import codes.kooper.shaded.litecommands.annotations.optional.OptionalArg;
import codes.kooper.shaded.litecommands.annotations.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

import static codes.kooper.koopKore.KoopKore.textUtils;

@Command(name = "pet", aliases = {"pets"})
public class PetCommand {

    @Execute
    public void onPet(@Context Player player) {
        new PetGui(player);
    }

    @Execute(name = "give")
    @Permission("pets.admin")
    public void givePet(@Arg Player target, @Arg PetModel petModel, @OptionalArg Optional<Integer> amount) {
        Pet pet = petModel.getPet();
        ItemStack petItem = pet.getPhysicalPet();
        petItem.setAmount(amount.orElse(1));
        target.getInventory().addItem(petItem);
    }

    @Execute(name = "reload")
    @Permission("pets.reload")
    public void onReload(@Context CommandSender sender) {
        QuarryPets.getInstance().onReload();
        sender.sendMessage(textUtils.colorize("<green>Pets reloaded!"));
    }

}
