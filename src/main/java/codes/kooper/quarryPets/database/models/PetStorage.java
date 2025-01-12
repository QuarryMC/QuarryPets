package codes.kooper.quarryPets.database.models;

import codes.kooper.koopKore.database.models.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class PetStorage extends BaseEntity {
    private UUID owner;
    private List<Pet> selectedPets;
    private List<Pet> petsStorage;
    private int maxStorage;

    public PetStorage(UUID uuid) {
        this.owner = uuid;
        selectedPets = new ArrayList<>();
        petsStorage = new ArrayList<>();
        maxStorage = 100;
    }

    public PetStorage() {
        selectedPets = new ArrayList<>();
        petsStorage = new ArrayList<>();
    }

    public int getMaxSelected() {
        Player player = Bukkit.getPlayer(owner);
        if (player == null) return 1;

        if (player.hasPermission("crittercollector.2")) {
            return 3;
        } else if (player.hasPermission("crittercollector.1")) {
            return 2;
        }

        return 1;
    }

    @Override
    public UUID getId() {
        return owner;
    }

    @Override
    public void setId(UUID uuid) {
        owner = uuid;
    }

    @BsonIgnore
    public int getTotalCount() {
        return petsStorage.size() + selectedPets.size();
    }

    @BsonIgnore
    public void addPetToStorage(Pet pet) {
        petsStorage.add(pet);
    }

    @BsonIgnore
    public void addPetToSelected(Pet pet) {
        selectedPets.add(pet);
    }

    @BsonIgnore
    public void removePetFromStorage(Pet pet) {
        petsStorage.remove(pet);
    }

    @BsonIgnore
    public void removePetFromSelected(Pet pet) {
        selectedPets.remove(pet);
    }
}
