package codes.kooper.quarryPets;

import codes.kooper.koopKore.database.tasks.DataSyncTask;
import codes.kooper.quarryPets.commands.EggCommand;
import codes.kooper.quarryPets.commands.arguments.EggModelArgument;
import codes.kooper.quarryPets.database.cache.EggStorageCache;
import codes.kooper.quarryPets.database.listeners.EggStorageLoadListener;
import codes.kooper.quarryPets.database.models.EggStorage;
import codes.kooper.quarryPets.database.services.EggService;
import codes.kooper.quarryPets.managers.EggManager;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.shaded.litecommands.LiteCommands;
import codes.kooper.shaded.litecommands.bukkit.LiteBukkitFactory;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

@Getter
public final class QuarryPets extends JavaPlugin {
    private EggManager eggManager;
    private DataSyncTask<UUID, EggStorage> eggSyncTask;
    private EggService eggService;
    private EggStorageCache eggStorageCache;
    private LiteCommands<CommandSender> liteCommands;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Pet/Egg Command
        this.liteCommands = LiteBukkitFactory.builder("quarrypets", this)
                .commands(new EggCommand())
                .argument(EggModel.class, new EggModelArgument())
                .build();

        // eggs
        eggManager = new EggManager();
        eggService = new EggService();
        eggStorageCache = new EggStorageCache();
        getServer().getPluginManager().registerEvents(new EggStorageLoadListener(), this);
        eggSyncTask =  new DataSyncTask<>(this, eggStorageCache.getAll(), (uuid, eggStorage) -> eggService.saveEggStorage(eggStorage), false);
        eggSyncTask.start(2400);
    }

    @Override
    public void onDisable() {
        if (eggSyncTask != null) eggSyncTask.stop();

        // Shutdown LiteCommands
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }
    }

    public void onReload() {
        reloadConfig();
        eggManager = new EggManager();
    }

    public static QuarryPets getInstance() {
        return QuarryPets.getPlugin(QuarryPets.class);
    }
}
