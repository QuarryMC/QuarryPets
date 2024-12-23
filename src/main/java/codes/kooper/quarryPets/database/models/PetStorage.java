package codes.kooper.quarryPets.database.models;

import codes.kooper.koopKore.database.models.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class PetStorage extends BaseEntity {
    private UUID owner;
    private Set<Pet> selectedPets;
    private Set<Pet> petsStorage;
    private int maxSelected;
    private int maxStorage;

    public PetStorage(UUID uuid) {
        this.owner = uuid;
        selectedPets = new HashSet<>();
        petsStorage = new HashSet<>();
        maxSelected = 1;
        maxStorage = 100;
    }

    public PetStorage() {
        selectedPets = new HashSet<>();
        petsStorage = new HashSet<>();
    }

    @Override
    public UUID getId() {
        return owner;
    }

    @Override
    public void setId(UUID uuid) {
        owner = uuid;
    }

    public void addPetToStorage(Pet pet) {
        petsStorage.add(pet);
    }

    public void addPetToSelected(Pet pet) {
        selectedPets.add(pet);
    }

    public void removePetFromStorage(Pet pet) {
        petsStorage.remove(pet);
    }

    public void removePetFromSelected(Pet pet) {
        selectedPets.remove(pet);
    }
}
