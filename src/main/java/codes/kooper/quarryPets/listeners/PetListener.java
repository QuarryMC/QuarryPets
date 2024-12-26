package codes.kooper.quarryPets.listeners;

import codes.kooper.koopKore.utils.Tasks;
import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.models.Pet;
import codes.kooper.quarryPets.database.models.PetStorage;
import codes.kooper.shaded.nbtapi.NBT;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

import static codes.kooper.koopKore.KoopKore.textUtils;

public class PetListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Tasks.runSyncLater(() -> QuarryPets.getInstance().getPetManager().equipPets(event.getPlayer()), 40L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        QuarryPets.getInstance().getPetManager().getSpawnedPets().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPetUse(PlayerInteractEvent event) {
        if (!QuarryPets.getInstance().getPetManager().isPetItem(event.getItem())) return;
        Player player = event.getPlayer();
        ItemStack tool = event.getItem();
        Optional<PetStorage> petStorageOptional = QuarryPets.getInstance().getPetStorageCache().get(player.getUniqueId());
        if (petStorageOptional.isEmpty()) return;
        final PetStorage petStorage = petStorageOptional.get();
        if (petStorage.getTotalCount() + 1 > petStorage.getMaxStorage()) {
            player.sendMessage(textUtils.error("You have reached the max pet storage!"));
            return;
        }
        if (tool == null) return;
        ItemStack petTool = tool.clone();
        Component name = tool.displayName();
        tool.setAmount(tool.getAmount() - 1);
        player.sendMessage(textUtils.success("You have deposited a ").append(name).append(textUtils.colorize("<#1ebc73> to your pet storage!")));
        NBT.get(petTool, (nbt) -> {
            petStorage.addPetToStorage(new Pet(
                nbt.getString("pet"),
                nbt.getString("egg"),
                nbt.getDouble("sell-boost"),
                nbt.getDouble("fortune-boost"),
                nbt.getInteger("lucky"),
                nbt.getInteger("level"),
                nbt.getInteger("xp")
            ));
        });
    }
}