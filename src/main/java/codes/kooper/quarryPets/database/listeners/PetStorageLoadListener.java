package codes.kooper.quarryPets.database.listeners;

import codes.kooper.koopKore.utils.Tasks;
import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.events.PetStorageLoadEvent;
import codes.kooper.quarryPets.database.events.PetStorageUnloadEvent;
import codes.kooper.quarryPets.database.models.PetStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PetStorageLoadListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (QuarryPets.getInstance().getPetStorageCache().containsKey(uuid)) {
            return;
        }

        Tasks.runAsync(() -> {
            PetStorage petStorage = QuarryPets.getInstance().getPetService().findById(uuid).orElseGet(() -> new PetStorage(uuid));
            QuarryPets.getInstance().getPetStorageCache().put(uuid, petStorage);
            Tasks.runSync(() -> new PetStorageLoadEvent(petStorage).callEvent());
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        QuarryPets.getInstance().getPetStorageCache().get(uuid).ifPresent(storage -> {
            Tasks.runAsync(() -> QuarryPets.getInstance().getPetService().savePetStorage(storage));
            new PetStorageUnloadEvent(storage).callEvent();
            QuarryPets.getInstance().getPetStorageCache().invalidate(uuid);
        });
    }
}