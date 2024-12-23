package codes.kooper.quarryPets.database.models;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.models.EggModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Egg {
    private String egg;
    private int blocksLeft;

    public Egg() {}

    public Egg(EggModel eggModel) {
        egg = eggModel.getKey();
        blocksLeft = eggModel.getBlocks();
    }

    public void progressEgg(int amount) {
        blocksLeft -= amount;
    }

    public boolean canHatch() {
        return blocksLeft <= 0;
    }

    public EggModel getModel() {
        return QuarryPets.getInstance().getEggManager().getEgg(egg);
    }
}
