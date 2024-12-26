package codes.kooper.quarryPets.database.models;

import codes.kooper.koopKore.database.models.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.util.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class PetStorage extends BaseEntity {
    private UUID owner;
    private List<Pet> selectedPets;
    private List<Pet> petsStorage;
    private int maxSelected;
    private int maxStorage;

    public PetStorage(UUID uuid) {
        this.owner = uuid;
        selectedPets = new ArrayList<>();
        petsStorage = new ArrayList<>();
        maxSelected = 1;
        maxStorage = 100;
    }

    public PetStorage() {
        selectedPets = new ArrayList<>();
        petsStorage = new ArrayList<>();
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
