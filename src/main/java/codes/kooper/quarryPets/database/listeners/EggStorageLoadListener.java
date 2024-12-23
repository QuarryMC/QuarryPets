package codes.kooper.quarryPets.database.listeners;

import codes.kooper.koopKore.utils.Tasks;
import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.events.EggStorageLoadEvent;
import codes.kooper.quarryPets.database.models.EggStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class EggStorageLoadListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (QuarryPets.getInstance().getEggStorageCache().containsKey(uuid)) {
            return;
        }

        Tasks.runAsync(() -> {
            EggStorage eggStorage = QuarryPets.getInstance().getEggService().findById(uuid).orElseGet(() -> new EggStorage(uuid));
            QuarryPets.getInstance().getEggStorageCache().put(uuid, eggStorage);
            Tasks.runSync(() -> new EggStorageLoadEvent(eggStorage).callEvent());
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        QuarryPets.getInstance().getEggStorageCache().get(uuid).ifPresent(storage -> {
            Tasks.runAsync(() -> QuarryPets.getInstance().getEggService().saveEggStorage(storage));
            QuarryPets.getInstance().getEggStorageCache().invalidate(uuid);
        });
    }
}