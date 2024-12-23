package codes.kooper.quarryPets.models;

import codes.kooper.koopKore.item.ItemBuilder;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ThreadLocalRandom;

import static codes.kooper.koopKore.KoopKore.numberUtils;
import static codes.kooper.koopKore.KoopKore.textUtils;

@Data
public class EggModel {
    private final String key;
    private final double fortuneBoostMin;
    private final double fortuneBoostMax;
    private final double sellBoostMin;
    private final double sellBoostMax;
    private final int blocks;
    private final NavigableMap<Double, String> petChances;
    private final String color1, color2, texture;

    public EggModel(String key, int blocks, double fortuneBoostMin, double fortuneBoostMax, double sellBoostMin, double sellBoostMax, NavigableMap<Double, String> petChances, String color1, String color2, String texture) {
        this.key = key;
        this.blocks = blocks;
        this.fortuneBoostMin = fortuneBoostMin;
        this.fortuneBoostMax = fortuneBoostMax;
        this.sellBoostMin = sellBoostMin;
        this.sellBoostMax = sellBoostMax;
        this.petChances = petChances;
        this.color1 = color1;
        this.color2 = color2;
        this.texture = texture;
    }

    public String getRandomPet() {
        double randomValue = ThreadLocalRandom.current().nextDouble();
        Map.Entry<Double, String> entry = petChances.higherEntry(randomValue);
        return (entry != null) ? entry.getValue() : null;
    }

    public ItemStack getEgg() {
        return new ItemBuilder(Material.PLAYER_HEAD)
            .setName(textUtils.colorize(color1 + "<bold>" + textUtils.capitalize(key).toUpperCase() + " EGG"))
            .setLore(List.of(
                color2 + "Mine <white>" + numberUtils.commaFormat(blocks) + color2 + " blocks to hatch!",
                "<gray><italic>(( View all hatchable pets with /petindex ))"
            ))
            .hideFlags(true)
            .setTexture(texture)
            .build();
    }
}
