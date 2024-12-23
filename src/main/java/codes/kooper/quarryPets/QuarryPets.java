package codes.kooper.quarryPets;

import codes.kooper.koopKore.database.tasks.DataSyncTask;
import codes.kooper.quarryPets.database.cache.EggStorageCache;
import codes.kooper.quarryPets.database.models.EggStorage;
import codes.kooper.quarryPets.database.services.EggService;
import codes.kooper.quarryPets.managers.EggManager;
import codes.kooper.quarryPets.managers.PetManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

@Getter
public final class QuarryPets extends JavaPlugin {
    private EggManager eggManager;
    private DataSyncTask<UUID, EggStorage> eggSyncTask;
    private EggService eggService;
    private EggStorageCache eggStorageCache;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // eggs
        eggManager = new EggManager();
        eggService = new EggService();
        eggStorageCache = new EggStorageCache();
        eggSyncTask =  new DataSyncTask<>(this, eggStorageCache.getAll(), (uuid, eggStorage) -> eggService.saveEggStorage(eggStorage), false);
        eggSyncTask.start(2400);
    }

    @Override
    public void onDisable() {
        if (eggSyncTask != null) eggSyncTask.stop();
    }

    public void onReload() {
        reloadConfig();
        petManager = new PetManager();
    }

    public static QuarryPets getInstance() {
        return QuarryPets.getPlugin(QuarryPets.class);
    }
}
