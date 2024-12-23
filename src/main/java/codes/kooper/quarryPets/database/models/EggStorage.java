package codes.kooper.quarryPets.database.models;

import codes.kooper.koopKore.database.models.BaseEntity;
import lombok.*;

import java.util.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class EggStorage extends BaseEntity {
    private UUID owner;
    private List<Egg> selectedEggs;
    private List<Egg> eggsStorage;
    private int maxSelected;
    private int maxStorage;

    public EggStorage() {
        eggsStorage = new ArrayList<>();
        selectedEggs = new ArrayList<>();
    }

    public EggStorage(UUID owner) {
        this.owner = owner;
        maxSelected = 1;
        eggsStorage = new ArrayList<>();
        selectedEggs = new ArrayList<>();
        maxStorage = 50;
    }

    @Override
    public UUID getId() {
        return owner;
    }

    @Override
    public void setId(UUID uuid) {
        this.owner = uuid;
    }

    public int getTotalCount() {
        return eggsStorage.size() + selectedEggs.size();
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