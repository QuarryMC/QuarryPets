package codes.kooper.quarryPets.database.models;

import codes.kooper.koopKore.enums.TextEffect;
import codes.kooper.koopKore.item.ItemBuilder;
import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.quarryPets.models.PetModel;
import codes.kooper.shaded.nbtapi.NBT;
import dev.lone.itemsadder.api.CustomStack;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static codes.kooper.koopKore.KoopKore.textUtils;

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

    public Pet(String pet, String egg, double sellBoost, double fortuneBoost, int luckyBlockNuker, int level, int xp) {
        this.pet = pet;
        this.egg = egg;
        this.sellBoost = sellBoost;
        this.fortuneBoost = fortuneBoost;
        this.luckyBlockNuker = luckyBlockNuker;
        this.level = level;
        this.xp = xp;
    }

    public void addExp(int xp) {
        this.xp += xp;
    }

    public void addLevel() {
        level++;
        sellBoost *= ((level * 0.0001) + 1);
        fortuneBoost *= ((level * 0.0001) + 1);
    }

    @BsonIgnore
    public EggModel getEggModel() {
        return QuarryPets.getInstance().getEggManager().getEgg(egg);
    }

    @BsonIgnore
    public PetModel getPetModel() {
        return QuarryPets.getInstance().getPetManager().getPetModel(egg, pet);
    }

    @BsonIgnore
    public List<Component> getBaseLore() {
        PetModel petModel = getPetModel();
        EggModel eggModel = getEggModel();
        int cost = QuarryPets.getInstance().getPetManager().getXPCost(this);
        String progressBar;
        if (cost == -1) {
            progressBar = "<green>MAXED";
        } else {
            progressBar = textUtils.progressBar(xp, cost, 10, "┃", petModel.color1(), "<color:#b2ba90>");
        }
        DecimalFormat df = new DecimalFormat("#.##");
        List<Component> lore = new ArrayList<>(List.of(
            textUtils.colorize(petModel.color1() + "<bold>|</bold> <color:#1ebc73>Fortune Boost: <green>+" + df.format(fortuneBoost) + "%"),
            textUtils.colorize(petModel.color1() + "<bold>|</bold> <color:#1ebc73>Sell Boost: <green>+" + df.format(sellBoost) + "%"),
            textUtils.colorize(petModel.color1() + "<bold>|</bold> <color:#9babb2>Level " + level + "</color> " + petModel.color1() + "(" + progressBar + petModel.color1() + ")"),
            Component.empty()
        ));
        if (luckyBlockNuker > 0) {
            lore.add(textUtils.colorize("<bold>").append(textUtils.formatTextEffect(TextEffect.RAINBOW, "LUCKY BLOCK NUKER RADIUS:" )).append(textUtils.colorize(" <white>" + luckyBlockNuker)));
            lore.add(Component.empty());
        }
        lore.add(textUtils.colorize(eggModel.getColor1() + "<bold>HATCHED FROM A "  + textUtils.capitalize(eggModel.getKey()).toUpperCase() + " EGG"));
        return lore;
    }

    @BsonIgnore
    public ItemStack getPhysicalPet() {
        PetModel petModel = getPetModel();
        CustomStack stack = CustomStack.getInstance(petModel.model());
        ItemStack petIcon = stack != null ? stack.getItemStack() : new ItemStack(Material.BARRIER);
        List<Component> lore = getBaseLore();
        lore.addAll(List.of(Component.empty(), textUtils.colorize("<gray><italic>(( Click to deposit to your vault ))")));
        ItemStack item = new ItemBuilder(petIcon)
                .setName(textUtils.colorize(petModel.color1() + "<bold>" + textUtils.capitalize(petModel.name()).toUpperCase() + " PET"))
                .setLoreComponent(lore)
                .build();
        NBT.modify(item, (nbt) -> {
            nbt.setString("egg", egg);
            nbt.setString("pet", pet);
            nbt.setInteger("level", level);
            nbt.setInteger("xp", xp);
            nbt.setDouble("sell-boost", sellBoost);
            nbt.setDouble("fortune-boost", fortuneBoost);
            nbt.setInteger("lucky", luckyBlockNuker);
        });
        return item;
    }
}
