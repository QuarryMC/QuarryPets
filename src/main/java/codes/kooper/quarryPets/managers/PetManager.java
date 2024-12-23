package codes.kooper.quarryPets.managers;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.quarryPets.models.PetModel;
import codes.kooper.shaded.gui.builder.item.ItemBuilder;
import dev.lone.itemsadder.api.CustomStack;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static codes.kooper.koopKore.KoopKore.textUtils;

@Getter
public class PetManager {
    private final LinkedHashMap<String, List<PetModel>> pets;

    public PetManager() {
        pets = new LinkedHashMap<>();
        loadPets();
    }

    public void loadPets() {
        ConfigurationSection section = QuarryPets.getInstance().getConfig().getConfigurationSection("pets");
        if (section == null) return;
        for (String rarity : section.getKeys(false)) {
            ConfigurationSection raritySection = section.getConfigurationSection(rarity);
            if (raritySection == null) continue;
            EggModel eggModel = QuarryPets.getInstance().getEggManager().getEgg(rarity);
            List<PetModel> models = new ArrayList<>();
            for (String pet : raritySection.getKeys(false)) {
                ConfigurationSection petSection = raritySection.getConfigurationSection(pet);
                if (petSection == null) continue;
                String color1 = petSection.getString("color1");
                String color2 = petSection.getString("color2");
                double chance = petSection.getDouble("chance");
                String model = petSection.getString("model");
                models.add(new PetModel(pet, eggModel, color1, color2, chance, model));
            }
            pets.put(rarity, models);
        }
    }

    public ItemStack getPet() {
        CustomStack stack = CustomStack.getInstance(model);
        String progressBar = textUtils.progressBar(0, cost, 10, "┃", rarity.getColor(), "<color:#b2ba90>");
        if (stack != null) {
            ItemStack itemStack = stack.getItemStack();
            return ItemBuilder.from(itemStack)
                    .name(textUtils.colorize(color1 + "<bold>" + textUtils.capitalize(name).toUpperCase() + " PET"))
                    .lore(List.of(
                            Component.empty(),
                            color1 + "<bold>|</bold>"
                    ))
                    .build();
        } else {
            QuarryPets.getInstance().getLogger().severe(name + "'s pet model could not be found in itemsadder.");
        }
    }
}