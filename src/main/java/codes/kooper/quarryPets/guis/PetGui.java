package codes.kooper.quarryPets.guis;

import codes.kooper.koopKore.KoopKore;
import codes.kooper.koopKore.database.models.User;
import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.models.Pet;
import codes.kooper.quarryPets.database.models.PetStorage;
import codes.kooper.shaded.gui.builder.item.ItemBuilder;
import codes.kooper.shaded.gui.components.InteractionModifier;
import codes.kooper.shaded.gui.guis.GuiItem;
import codes.kooper.shaded.gui.guis.PaginatedGui;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.*;

import static codes.kooper.koopKore.KoopKore.numberUtils;
import static codes.kooper.koopKore.KoopKore.textUtils;

public class PetGui {

    public PetGui(Player player) {
        PaginatedGui gui = new PaginatedGui(6, 28, "Pet Storage", Set.of(InteractionModifier.values()));

        // Pet storage
        Optional<PetStorage> petStorageOptional = QuarryPets.getInstance().getPetStorageCache().get(player.getUniqueId());
        if (petStorageOptional.isEmpty()) return;
        PetStorage petStorage = petStorageOptional.get();

        // User storage
        Optional<User> userOptional = KoopKore.getInstance().getUserAPI().getUser(player.getUniqueId());
        if (userOptional.isEmpty()) return;
        User user = userOptional.get();

        // Border
        gui.getFiller().fillBorder(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());

        // Next page button
        ItemStack forwardArrow = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta resultMeta = (PotionMeta) forwardArrow.getItemMeta();
        resultMeta.setBasePotionType(PotionType.NIGHT_VISION);
        forwardArrow.setItemMeta(resultMeta);
        GuiItem forwardItemGui = ItemBuilder.from(forwardArrow)
                .name(textUtils.colorize("<green><bold>NEXT PAGE"))
                .flags(ItemFlag.values())
                .asGuiItem();
        forwardItemGui.setAction((action) -> {
            if (!gui.next()) {
                player.sendMessage(textUtils.error("You are on the last page!"));
            }
        });
        gui.setItem(51, forwardItemGui);

        // Back page button
        ItemStack backArrow = new ItemStack(Material.TIPPED_ARROW);
        resultMeta = (PotionMeta) backArrow.getItemMeta();
        resultMeta.setBasePotionType(PotionType.HEALING);
        backArrow.setItemMeta(resultMeta);
        GuiItem backItemGui = ItemBuilder.from(backArrow)
                .name(textUtils.colorize("<red><bold>PREVIOUS PAGE"))
                .flags(ItemFlag.values())
                .asGuiItem();
        backItemGui.setAction((action) -> {
            if (!gui.previous()) {
                player.sendMessage(textUtils.error("You are on the first page!"));
            }
        });
        gui.setItem(47, backItemGui);

        // Unselected Item
        ItemStack cancelIcon;
        CustomStack stack = CustomStack.getInstance("icon_cancel");
        if (stack != null) {
            cancelIcon = stack.getItemStack();
        } else {
            return;
        }

        // Selected pet slots
        int[] slots = {2, 3, 4, 5, 6};
        int index = 0;
        List<Integer> unlockedSlots = new ArrayList<>();
        List<Integer> miningLockedSlots = new ArrayList<>();
        List<Integer> buyLockedSlots = new ArrayList<>();

        for (int slot : slots) {
            try {
                petStorage.getSelectedPets().get(index);
                unlockedSlots.add(slot);
            } catch (Exception e) {
                // No pet selected for this slot
                if (index + 1 > petStorage.getMaxSelected()) {
                    if (index == 3 || index == 4) {
                        miningLockedSlots.add(slot); // Add to mining locked slots
                    } else {
                        buyLockedSlots.add(slot); // Add to buy locked slots
                    }
                } else {
                    // If slot isn't selected and it's under the max selected, show "No Pet Selected"
                    gui.setItem(slot, ItemBuilder.from(cancelIcon).name(textUtils.colorize("<red><bold>No Pet Selected")).asGuiItem());
                }
                index++;
                continue;
            }
            index++;
        }

        for (int slot : unlockedSlots) {
            Pet pet = petStorage.getSelectedPets().get(unlockedSlots.indexOf(slot)); // Get the selected pet for the slot
            List<String> lore = pet.getBaseLore();
            lore.add(""); // Add space between lore
            lore.add("<green><bold>CLICK TO UNSELECT");

            GuiItem petItem = ItemBuilder.from(pet.getPhysicalPet())
                    .lore(textUtils.colorize(lore))
                    .asGuiItem();

            petItem.setAction((action) -> {
                petStorage.removePetFromSelected(pet);
                petStorage.addPetToStorage(pet);
                QuarryPets.getInstance().getPetManager().equipPets(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 5, 1);
                new PetGui(player); // Refresh GUI after unselecting the pet
            });

            gui.setItem(slot, petItem);
        }

        int i = 0;
        for (int slot : miningLockedSlots) {
            int requiredMined = (i == 0) ? 1000000 : 5000000;
            if (user.getMined() >= requiredMined) {
                gui.setItem(slot, ItemBuilder.from(cancelIcon).name(textUtils.colorize("<red><bold>No Pet Selected")).asGuiItem());
            } else {
                gui.setItem(slot, ItemBuilder.from(Material.IRON_BARS).name(textUtils.colorize("<red><bold>Pet Slot Locked"))
                        .lore(textUtils.colorize("<gray>Unlock by mining <white>" + numberUtils.commaFormat(requiredMined) + " <gray>blocks.")).asGuiItem());
            }
            i++;
        }

        for (int slot : buyLockedSlots) {
            gui.setItem(slot, ItemBuilder.from(Material.IRON_BARS).name(textUtils.colorize("<red><bold>Pet Slot Locked"))
                    .lore(textUtils.colorize("<gray>Unlock in /buy.")).asGuiItem());
        }

        // Add pets
        for (Pet pet : petStorage.getPetsStorage().stream().sorted(Comparator.comparingInt(pet -> ((Pet) pet).getPetModel().index()).reversed()).toList()) {
            List<String> lore = pet.getBaseLore();
            lore.add("");
            lore.add("<green><bold>LEFT-CLICK TO SELECT");
            lore.add("<red><bold>RIGHT-CLICK TO WITHDRAW");
            GuiItem eggItem = ItemBuilder.from(pet.getPhysicalPet())
                    .lore(textUtils.colorize(lore))
                    .asGuiItem();
            eggItem.setAction((action) -> {
                if (action.isRightClick()) {
                    petStorage.removePetFromStorage(pet);
                    player.getInventory().addItem(pet.getPhysicalPet());
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 5, 1.3f);
                } else {
                    if (petStorage.getSelectedPets().size() + 1 > petStorage.getMaxSelected()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5, 1);
                        player.sendMessage(textUtils.error("You have the maxed amount of pets equipped."));
                        return;
                    }
                    petStorage.removePetFromStorage(pet);
                    petStorage.addPetToSelected(pet);
                    QuarryPets.getInstance().getPetManager().equipPets(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 5, 1);
                }
                new PetGui(player);
            });
            gui.addItem(eggItem);
        }

        gui.open(player);
    }
}