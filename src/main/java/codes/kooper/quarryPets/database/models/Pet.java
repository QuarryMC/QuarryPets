package codes.kooper.quarryPets.database.models;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.quarryPets.models.PetModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Pet {
    private String egg;
    private String pet;
    private int level;
    private int xp;
    private double sellBoost;
    private double fortuneBoost;
    private int luckyBlockNuker;

    public Pet() {}

    public Pet(String pet, String egg, double sellBoost, double fortuneBoost, int luckyBlockNuker) {
        this.pet = pet;
        this.egg = egg;
        this.level = 1;
        this.sellBoost = sellBoost;
        this.fortuneBoost = fortuneBoost;
        this.luckyBlockNuker = luckyBlockNuker;
    }

    public void addExp(int xp) {
        this.xp += xp;
    }

    public void addLevel() {
        level++;
    }

    public void addLevels(int level) {
        this.level += level;
    }

    public EggModel getEggModel() {
        return QuarryPets.getInstance().getEggManager().getEgg(egg);
    }

    public PetModel getPetModel() {
        return QuarryPets.getInstance().getPetManager().getPetModel(egg, pet);
    }
}
