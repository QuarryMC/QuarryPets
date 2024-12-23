package codes.kooper.quarryPets;

import codes.kooper.koopKore.database.tasks.DataSyncTask;
import codes.kooper.quarryPets.commands.EggCommand;
import codes.kooper.quarryPets.commands.PetIndexCommand;
import codes.kooper.quarryPets.commands.arguments.EggModelArgument;
import codes.kooper.quarryPets.database.cache.EggStorageCache;
import codes.kooper.quarryPets.database.cache.PetStorageCache;
import codes.kooper.quarryPets.database.listeners.EggStorageLoadListener;
import codes.kooper.quarryPets.database.listeners.PetStorageLoadListener;
import codes.kooper.quarryPets.database.models.EggStorage;
import codes.kooper.quarryPets.database.models.PetStorage;
import codes.kooper.quarryPets.database.services.EggService;
import codes.kooper.quarryPets.database.services.PetService;
import codes.kooper.quarryPets.listeners.EggListener;
import codes.kooper.quarryPets.managers.EggManager;
import codes.kooper.quarryPets.managers.PetManager;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.shaded.litecommands.LiteCommands;
import codes.kooper.shaded.litecommands.bukkit.LiteBukkitFactory;
import com.github.retrooper.packetevents.PacketEvents;
import lombok.Getter;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
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
    private PetManager petManager;
    private PetStorageCache petStorageCache;
    private PetService petService;
    private DataSyncTask<UUID, PetStorage> petSyncTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // EntityLib
        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);
        APIConfig settings = new APIConfig(PacketEvents.getAPI())
                .tickTickables()
                .trackPlatformEntities()
                .usePlatformLogger();
        EntityLib.init(platform, settings);

        // Pet/Egg Command
        this.liteCommands = LiteBukkitFactory.builder("quarrypets", this)
                .commands(new EggCommand(), new PetIndexCommand())
                .argument(EggModel.class, new EggModelArgument())
                .build();

        // eggs
        eggManager = new EggManager();
        eggService = new EggService();
        eggStorageCache = new EggStorageCache();
        getServer().getPluginManager().registerEvents(new EggListener(), this);
        getServer().getPluginManager().registerEvents(new EggStorageLoadListener(), this);
        eggSyncTask =  new DataSyncTask<>(this, eggStorageCache.getAll(), (uuid, eggStorage) -> eggService.saveEggStorage(eggStorage), false);
        eggSyncTask.start(2400);

        // pets
        petManager = new PetManager();
        petService = new PetService();
        petStorageCache = new PetStorageCache();
        getServer().getPluginManager().registerEvents(new PetStorageLoadListener(), this);
        petSyncTask = new DataSyncTask<>(this, petStorageCache.getAll(), (uuid, petStorage) -> petService.savePetStorage(petStorage), false);
        petSyncTask.start(2400);
    }

    @Override
    public void onDisable() {
        if (eggSyncTask != null) eggSyncTask.stop();
        if (petSyncTask != null) petSyncTask.stop();

        // Shutdown LiteCommands
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }
    }

    public void onReload() {
        reloadConfig();
        eggManager = new EggManager();
        petManager = new PetManager();
    }

    public static QuarryPets getInstance() {
        return QuarryPets.getPlugin(QuarryPets.class);
    }
}
