package codes.kooper.quarryPets.managers;

import codes.kooper.koopKore.utils.TextUtils;
import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.models.Egg;
import codes.kooper.quarryPets.database.models.EggStorage;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.shaded.nbtapi.NBT;
import lombok.Getter;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static codes.kooper.koopKore.KoopKore.textUtils;

@Getter
public class EggManager {
    private final LinkedHashMap<String, EggModel> eggs;

    public EggManager() {
        eggs = new LinkedHashMap<>();
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

    public void progressEggs(Player player, int amount) {
        Optional<EggStorage> eggStorageOptional = QuarryPets.getInstance().getEggStorageCache().get(player.getUniqueId());
        if (eggStorageOptional.isEmpty()) return;
        EggStorage eggStorage = eggStorageOptional.get();
        for (Egg egg : eggStorage.getSelectedEggs()) {
            egg.progressEgg(amount);
            if (egg.getBlocksLeft() == 0) {
                EggModel eggModel = egg.getModel();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5, 1.2f);
                player.sendTitlePart(TitlePart.TITLE, textUtils.colorize("<rainbow><bold>EGG READY"));
                player.sendTitlePart(TitlePart.SUBTITLE, textUtils.colorize(eggModel.getColor2() + "Your " + eggModel.getColor1() + textUtils.capitalize(eggModel.getKey()) + eggModel.getColor2() + " is ready to be hatched!"));
            }
        }
    }

    public EggModel getEggModel(ItemStack item) {
        if (item == null || item.isEmpty()) return null;
        return NBT.get(item, (nbt) -> {
            return eggs.get(nbt.getString("egg"));
        });
    }

    public EggModel getEgg(String egg) {
        return eggs.get(egg);
    }
}
