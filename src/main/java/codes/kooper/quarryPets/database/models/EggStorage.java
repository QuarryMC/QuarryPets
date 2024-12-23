package codes.kooper.quarryPets.database.models;

import codes.kooper.koopKore.database.models.BaseEntity;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class EggStorage extends BaseEntity {
    private UUID owner;
    private Set<Egg> selectedEggs;
    private Set<Egg> eggsStorage;
    private int maxSelected;

    public EggStorage() {
        eggsStorage = new HashSet<>();
        selectedEggs = new HashSet<>();
    }

    public EggStorage(UUID owner) {
        this.owner = owner;
        maxSelected = 1;
        eggsStorage = new HashSet<>();
        selectedEggs = new HashSet<>();
    }

    @Override
    public UUID getId() {
        return owner;
    }

    @Override
    public void setId(UUID uuid) {
        this.owner = uuid;
    }

    public void addEggToStorage(Egg egg) {
        eggsStorage.add(egg);
    }

    public void addEggToSelected(Egg egg) {
        selectedEggs.add(egg);
    }

    public void removeEggFromStorage(Egg egg) {
        eggsStorage.remove(egg);
    }

    public void removeEggFromSelected(Egg egg) {
        selectedEggs.remove(egg);
    }
}