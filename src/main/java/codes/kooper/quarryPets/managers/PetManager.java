package codes.kooper.quarryPets.managers;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.models.Pet;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.quarryPets.models.PetModel;
import codes.kooper.shaded.gui.builder.item.ItemBuilder;
import dev.lone.itemsadder.api.CustomStack;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static codes.kooper.koopKore.KoopKore.textUtils;

@Getter
public class PetManager {
    private final LinkedHashMap<String, Map<String, PetModel>> pets;
    private final Map<Integer, Integer> levelingCosts;

    public PetManager() {
        pets = new LinkedHashMap<>();
        levelingCosts = new HashMap<>();
        loadPets();
    }

    public void loadPets() {
        ConfigurationSection levelSection = QuarryPets.getInstance().getConfig().getConfigurationSection("levels");
        if (levelSection == null) return;
        for (String key : levelSection.getKeys(false)) {
            int level = Integer.parseInt(key);
            int cost = levelSection.getInt(key);
            levelingCosts.put(level, cost);
        }

        ConfigurationSection section = QuarryPets.getInstance().getConfig().getConfigurationSection("pets");
        if (section == null) return;
        for (String rarity : section.getKeys(false)) {
            ConfigurationSection raritySection = section.getConfigurationSection(rarity);
            if (raritySection == null) continue;
            EggModel eggModel = QuarryPets.getInstance().getEggManager().getEgg(rarity);
            Map<String, PetModel> models = new HashMap<>();
            for (String pet : raritySection.getKeys(false)) {
                ConfigurationSection petSection = raritySection.getConfigurationSection(pet);
                if (petSection == null) continue;
                String color1 = petSection.getString("color1");
                String color2 = petSection.getString("color2");
                double chance = petSection.getDouble("chance");
                String model = petSection.getString("model");
                models.put(pet, new PetModel(pet, eggModel, color1, color2, chance, model));
            }
            pets.put(rarity, models);
        }
    }

    public PetModel getPetModel(String egg, String pet) {
        try {
            return pets.get(egg).get(pet);
        } catch (Exception e) {
            return null;
        }
    }

    public int getXPCost(Pet pet) {
        return levelingCosts.getOrDefault(pet.getLevel(), -1);
    }

    public ItemStack getPetItem(Pet pet) {
        PetModel petModel = pet.getPetModel();
        int cost = getXPCost(pet);
        String progressBar;
        if (cost == -1) {
            progressBar = "<green><bold>MAXED";
        } else {
            progressBar = textUtils.progressBar(0, cost, 10, "┃", petModel.egg().getColor1(), "<color:#b2ba90>");
        }
        CustomStack stack = CustomStack.getInstance(petModel.model());
        if (stack == null) {
            QuarryPets.getInstance().getLogger().severe(petModel.name() + "'s pet model could not be found in itemsadder.");
            return null;
        }
        ItemStack itemStack = stack.getItemStack();
        List<Component> lore = new ArrayList<>(List.of(
                Component.empty(),
                textUtils.colorize(petModel.color1() + "<bold>|</bold> <color:#9babb2>Level " + pet.getLevel() + "</color> " + petModel.color1() + "(" + progressBar + petModel.color1() + ")"),
                textUtils.colorize(petModel.color1() + "<bold>|</bold> " + petModel.color2() + "Fortune Boost: <white>" + pet.getFortuneBoost()),
                textUtils.colorize(petModel.color1() + "<bold>|</bold> " + petModel.color2() + "Sell Boost: <white>" + pet.getSellBoost())
        ));
        if (pet.getLuckyBlockNuker() != 0) {
            lore.add(Component.empty());
            lore.add(textUtils.colorize(petModel.color1() + "<bold>|</bold> <rainbow>Lucky Block Nuker Radius: <white>" + pet.getLuckyBlockNuker()));
        }
        return ItemBuilder.from(itemStack)
                .name(textUtils.colorize(petModel.color1() + "<bold>" + textUtils.capitalize(petModel.name()).toUpperCase() + " PET"))
                .lore(lore)
                .build();
    }
}