package codes.kooper.quarryPets.database.events;

import codes.kooper.quarryPets.database.models.PetStorage;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PetStorageLoadEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PetStorage petStorage;

    public PetStorageLoadEvent(PetStorage petStorage) {
        this.petStorage = petStorage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
