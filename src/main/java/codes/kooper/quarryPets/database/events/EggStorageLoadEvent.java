package codes.kooper.quarryPets.database.events;

import codes.kooper.quarryPets.database.models.EggStorage;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class EggStorageLoadEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final EggStorage eggStorage;

    public EggStorageLoadEvent(EggStorage eggStorage) {
        this.eggStorage = eggStorage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
