package codes.kooper.quarryPets.listeners;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.models.Egg;
import codes.kooper.quarryPets.database.models.EggStorage;
import codes.kooper.quarryPets.models.EggModel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

import static codes.kooper.koopKore.KoopKore.textUtils;

public class EggListener implements Listener {

    @EventHandler
    public void onEggUse(PlayerInteractEvent event) {
        EggModel eggModel = QuarryPets.getInstance().getEggManager().getEggModel(event.getItem());
        if (eggModel == null) return;
        Player player = event.getPlayer();
        ItemStack tool = event.getItem();
        Optional<EggStorage> eggStorageOptional = QuarryPets.getInstance().getEggStorageCache().get(player.getUniqueId());
        if (eggStorageOptional.isEmpty()) return;
        EggStorage eggStorage = eggStorageOptional.get();
        if (eggStorage.getTotalCount() + 1 > eggStorage.getMaxSelected()) {
            player.sendMessage(textUtils.error("You have reached the max egg storage!"));
            return;
        }
        if (tool == null) return;
        tool.setAmount(1);
        player.getInventory().remove(tool);
        player.sendMessage(textUtils.success("You have deposited a ").append(tool.displayName()).append(textUtils.colorize("<#1ebc73> to your egg storage!")));
        eggStorage.addEggToStorage(new Egg(eggModel));
    }

}
