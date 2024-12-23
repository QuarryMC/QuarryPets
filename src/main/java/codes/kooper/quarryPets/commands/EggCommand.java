package codes.kooper.quarryPets.commands;

import codes.kooper.quarryPets.guis.EggGui;
import codes.kooper.quarryPets.guis.EggIndexGui;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.shaded.litecommands.annotations.argument.Arg;
import codes.kooper.shaded.litecommands.annotations.command.Command;
import codes.kooper.shaded.litecommands.annotations.context.Context;
import codes.kooper.shaded.litecommands.annotations.execute.Execute;
import codes.kooper.shaded.litecommands.annotations.optional.OptionalArg;
import codes.kooper.shaded.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@Command(name = "egg", aliases = {"eggs"})
public class EggCommand {

    @Execute
    public void onEggCommand(@Context Player player) {
        new EggGui(player);
    }

    @Execute(name = "index", aliases = {"list"})
    public void onEggList(@Context Player player) {
        new EggIndexGui(player);
    }

    @Execute(name = "give")
    @Permission("eggs.admin")
    public void giveEgg(@Arg Player target, @Arg EggModel eggModel, @OptionalArg Optional<Integer> amount) {
        ItemStack eggItem = eggModel.getPhysicalEgg();
        eggItem.setAmount(amount.orElse(1));
        target.getInventory().addItem(eggItem);
    }
}
