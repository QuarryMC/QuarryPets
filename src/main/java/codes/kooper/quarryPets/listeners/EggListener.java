package codes.kooper.quarryPets.listeners;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.models.Egg;
import codes.kooper.quarryPets.database.models.EggStorage;
import codes.kooper.quarryPets.models.EggModel;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

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
        if (eggStorage.getTotalCount() + 1 > eggStorage.getMaxStorage()) {
            player.sendMessage(textUtils.error("You have reached the max egg storage!"));
            return;
        }
        if (tool == null) return;
        Component name = tool.displayName();
        tool.setAmount(tool.getAmount() - 1);
        player.sendMessage(textUtils.success("You have deposited a ").append(name).append(textUtils.colorize("<#1ebc73> to your egg storage!")));
        eggStorage.addEggToStorage(new Egg(eggModel));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        QuarryPets.getInstance().getEggManager().getEggOpening().remove(event.getPlayer().getUniqueId());
        if (event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            event.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (QuarryPets.getInstance().getEggManager().getEggOpening().contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (QuarryPets.getInstance().getEggManager().getEggOpening().contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}