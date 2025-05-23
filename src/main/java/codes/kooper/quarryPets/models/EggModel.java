package codes.kooper.quarryPets.models;

import codes.kooper.koopKore.item.ItemBuilder;
import codes.kooper.shaded.nbtapi.NBT;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import static codes.kooper.koopKore.KoopKore.numberUtils;
import static codes.kooper.koopKore.KoopKore.textUtils;

@Data
public class EggModel {
    private final String key;
    private final int index;
    private final double fortuneBoostMin;
    private final double fortuneBoostMax;
    private final double sellBoostMin;
    private final double sellBoostMax;
    private final int luckyBlockNukerMin;
    private final int luckyBlockNukerMax;
    private final int blocks;
    private final NavigableMap<Double, String> petChances;
    private final String color1, color2, texture;
    private final Location playerLocation;
    private final Location eggLocation;

    public EggModel(String key, int index, int blocks, double fortuneBoostMin, double fortuneBoostMax, double sellBoostMin, double sellBoostMax, int luckyBlockNukerMin, int luckyBlockNukerMax, NavigableMap<Double, String> petChances, String color1, String color2, String texture, Location playerLocation, Location eggLocation) {
        this.key = key;
        this.index = index;
        this.blocks = blocks;
        this.fortuneBoostMin = fortuneBoostMin;
        this.fortuneBoostMax = fortuneBoostMax;
        this.sellBoostMin = sellBoostMin;
        this.sellBoostMax = sellBoostMax;
        this.luckyBlockNukerMin = luckyBlockNukerMin;
        this.luckyBlockNukerMax = luckyBlockNukerMax;
        this.petChances = petChances;
        this.color1 = color1;
        this.color2 = color2;
        this.texture = texture;
        this.playerLocation = playerLocation;
        this.eggLocation = eggLocation;
    }

    public String getRandomPet(Player player) {
        if (petChances == null || petChances.isEmpty()) {
            throw new IllegalStateException("The petChances map is not initialized or is empty.");
        }

        double multi = 1.0;
        if (player.hasPermission("petwhisperer.5")) {
            multi *= 50;
        } else if (player.hasPermission("petwhisperer.4")) {
            multi *= 20;
        } else if (player.hasPermission("petwhisperer.3")) {
            multi *= 10;
        } else if (player.hasPermission("petwhisperer.2")) {
            multi *= 5;
        } else if (player.hasPermission("petwhisperer.1")) {
            multi *= 2;
        }

        // Create a scaled map of chances
        NavigableMap<Double, String> scaledPetChances = new TreeMap<>();
        double scaledTotal = 0.0;

        for (Map.Entry<Double, String> entry : petChances.entrySet()) {
            double scaledChance = entry.getKey() * multi;
            scaledTotal += scaledChance;
            scaledPetChances.put(scaledTotal, entry.getValue());
        }

        // Generate a random value between 0.0 and the total scaled range
        double randomValue = ThreadLocalRandom.current().nextDouble(0.0, scaledTotal);

        // Get the entry corresponding to the random value
        Map.Entry<Double, String> entry = scaledPetChances.ceilingEntry(randomValue);

        // If no entry is found, fallback to the first entry in the map
        if (entry == null) {
            throw new IllegalStateException("No matching pet found for the generated random value.");
        }

        return entry.getValue();
    }

    public ItemStack getPhysicalEgg() {
        ItemStack item = new ItemBuilder(Material.PLAYER_HEAD)
            .setName(textUtils.colorize(color1 + "<bold>" + textUtils.capitalize(key).toUpperCase() + " EGG"))
            .setLore(List.of(
                color2 + "Mine <white>" + numberUtils.commaFormat(blocks) + color2 + " blocks to hatch!",
                "<gray><italic>(( Click to deposit to your vault ))"
            ))
            .hideFlags(true)
            .setTexture(texture)
            .build();
        NBT.modify(item, (nbt) -> {
            nbt.setString("egg-item", key);
        });
        return item;
    }
}
