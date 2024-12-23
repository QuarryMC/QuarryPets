package codes.kooper.quarryPets.managers;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.models.EggModel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

@Getter
public class PetManager {
    private final Map<String, EggModel> eggs;

    public PetManager() {
        eggs = new HashMap<>();
        loadEggs();
    }

    public void loadEggs() {
        ConfigurationSection eggsSection = QuarryPets.getInstance().getConfig().getConfigurationSection("eggs");
        if (eggsSection == null) return;
        for (String key : eggsSection.getKeys(false)) {
            ConfigurationSection section = eggsSection.getConfigurationSection(key);
            if (section == null) continue;
            int blocks = section.getInt("blocks");
            double fortuneBoostMin = section.getDouble("fortune-boost-min");
            double fortuneBoostMax = section.getDouble("fortune-boost-max");
            double sellBoostMin = section.getDouble("sell-boost-min");
            double sellBoostMax = section.getDouble("sell-boost-max");
            String color1 = section.getString("color1");
            String color2 = section.getString("color2");
            String texture = section.getString("texture");

            NavigableMap<Double, String> petChances = new TreeMap<>();
            ConfigurationSection petsSection = QuarryPets.getInstance().getConfig().getConfigurationSection("pets." + key);
            if (petsSection == null) return;
            for (String pet : petsSection.getKeys(false)) {
                double chance = petsSection.getDouble(pet + ".chance");
                petChances.put(chance, pet);
            }

            eggs.put(key, new EggModel(
                key,
                blocks,
                fortuneBoostMin,
                fortuneBoostMax,
                sellBoostMin,
                sellBoostMax,
                petChances,
                color1,
                color2,
                texture
            ));
        }
    }

    public EggModel getEgg(String egg) {
        return eggs.get(egg);
    }
}
