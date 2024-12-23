package codes.kooper.quarryPets.database.models;

import codes.kooper.koopKore.item.ItemBuilder;
import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.shaded.nbtapi.NBT;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static codes.kooper.koopKore.KoopKore.numberUtils;
import static codes.kooper.koopKore.KoopKore.textUtils;

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

    public ItemStack getPhysicalEgg() {
        EggModel eggModel = getModel();
        ItemStack item = new ItemBuilder(Material.PLAYER_HEAD)
                .setName(textUtils.colorize(eggModel.getColor1() + "<bold>" + textUtils.capitalize(eggModel.getKey()).toUpperCase() + " EGG"))
                .setLore(List.of(
                        eggModel.getColor2() + "Mine <white>" + numberUtils.commaFormat(blocksLeft) + eggModel.getColor2() + " blocks to hatch!",
                        "<gray><italic>(( Click to deposit to your vault ))"
                ))
                .hideFlags(true)
                .setTexture(eggModel.getTexture())
                .build();
        NBT.modify(item, (nbt) -> {
            nbt.setString("egg", eggModel.getKey());
        });
        return item;
    }
}
