package codes.kooper.quarryPets.guis;

import codes.kooper.koopKore.KoopKore;
import codes.kooper.koopKore.database.models.User;
import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.models.Egg;
import codes.kooper.quarryPets.database.models.EggStorage;
import codes.kooper.quarryPets.models.EggModel;
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

public class EggGui {

    public EggGui(Player player) {
        PaginatedGui gui = new PaginatedGui(6, 28, "Egg Storage", Set.of(InteractionModifier.values()));

        // Egg storage
        Optional<EggStorage> eggStorageOptional = QuarryPets.getInstance().getEggStorageCache().get(player.getUniqueId());
        if (eggStorageOptional.isEmpty()) return;
        EggStorage eggStorage = eggStorageOptional.get();

        // User
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

        // Selected eggs
        int[] slots = {2, 3, 4, 5, 6};
        int index = 0;
        List<Integer> unlockedSlots = new ArrayList<>();
        List<Integer> miningLockedSlots = new ArrayList<>();
        List<Integer> buyLockedSlots = new ArrayList<>();

        for (int slot : slots) {
            try {
                eggStorage.getSelectedEggs().get(index);
                unlockedSlots.add(slot);
            } catch (Exception e) {
                // Handle the case where there is no egg selected for this slot
                if (index + 1 > eggStorage.getMaxSelected()) {
                    if (index == 3 || index == 4) { // Mining-unlocked slots (slot 4 and 5)
                        miningLockedSlots.add(slot);
                    } else { // /buy-unlocked slots (slot 1 and 2)
                        buyLockedSlots.add(slot);
                    }
                } else {
                    // Handle unselected eggs (No Egg Selected)
                    gui.setItem(slot, ItemBuilder.from(cancelIcon).name(textUtils.colorize("<red><bold>No Egg Selected")).asGuiItem());
                }
            }
            index++;
        }

        for (int slot : unlockedSlots) {
            Egg egg = eggStorage.getSelectedEggs().get(unlockedSlots.indexOf(slot));

            // Create the egg item with lore based on its status
            GuiItem eggItem = ItemBuilder.from(egg.getModel().getPhysicalEgg())
                    .lore(List.of(
                            textUtils.colorize(egg.getModel().getColor2() + "Mine <white>" + numberUtils.commaFormat(egg.getBlocksLeft()) + egg.getModel().getColor2() + " blocks to hatch!"),
                            Component.empty(),
                            textUtils.colorize("<green><bold>LEFT-CLICK TO UNSELECT"),
                            textUtils.colorize("<rainbow><bold>RIGHT-CLICK TO HATCH")
                    ))
                    .asGuiItem();

            // Set the actions for the item
            eggItem.setAction((action) -> {
                if (action.isLeftClick()) {
                    // Left-click: Unselect the egg and move it to storage
                    eggStorage.removeEggFromSelected(egg);
                    eggStorage.addEggToStorage(egg);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 5, 1);
                    new EggGui(player); // Refresh the GUI after unselecting
                } else if (action.isRightClick()) {
                    // Right-click: Hatch the egg if ready
                    if (!egg.canHatch()) {
                        // If egg is not ready to hatch, show an error
                        player.sendMessage(textUtils.error("This egg is not ready to hatch!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5, 1.2f);
                        return;
                    }
                    // If inventory is full, show an error
                    if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(textUtils.error("Your inventory is full!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5, 1.2f);
                        return;
                    }
                    // Hatch the egg
                    QuarryPets.getInstance().getEggManager().hatchEgg(player, egg);
                    eggStorage.removeEggFromSelected(egg);
                    player.closeInventory(); // Close the inventory after hatching
                }
            });

            // Set the egg item into the GUI at the correct slot
            gui.setItem(slots[unlockedSlots.indexOf(slot)], eggItem);
        }

        int i = 0;
        for (int slot : miningLockedSlots) {
            int requiredMined = (i == 0) ? 150000 : 500000; // Adjust mining requirements based on slot
            if (user.getMined() >= requiredMined) {
                gui.setItem(slot, ItemBuilder.from(cancelIcon).name(textUtils.colorize("<red><bold>No Egg Selected")).asGuiItem());
            } else {
                // Slot is locked and requires more mining
                gui.setItem(slot, ItemBuilder.from(Material.IRON_BARS).name(textUtils.colorize("<red><bold>Egg Slot Locked"))
                        .lore(textUtils.colorize("<gray>Unlock by mining <white>" + numberUtils.commaFormat(requiredMined) + " <gray>blocks.")).asGuiItem());
            }
            i++;
        }

        for (int slot : buyLockedSlots) {
            gui.setItem(slot, ItemBuilder.from(Material.IRON_BARS).name(textUtils.colorize("<red><bold>Egg Slot Locked"))
                    .lore(textUtils.colorize("<gray>Unlock in /buy.")).asGuiItem());
        }

        // Add eggs
        for (Egg egg : eggStorage.getEggsStorage().stream().sorted(Comparator.comparing((egg) -> egg.getModel().getIndex())).toList()) {
            final EggModel eggModel = egg.getModel();
            GuiItem eggItem = ItemBuilder.from(eggModel.getPhysicalEgg())
                    .lore(List.of(
                        textUtils.colorize(eggModel.getColor2() + "Mine <white>" + numberUtils.commaFormat(egg.getBlocksLeft()) + eggModel.getColor2() + " blocks to hatch!"),
                        Component.empty(),
                        textUtils.colorize("<green><bold>LEFT-CLICK TO SELECT"),
                        textUtils.colorize("<red><bold>RIGHT-CLICK TO WITHDRAW")
                    ))
                    .asGuiItem();
            eggItem.setAction((action) -> {
                if (action.isRightClick()) {
                    eggStorage.removeEggFromStorage(egg);
                    player.getInventory().addItem(egg.getPhysicalEgg());
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 5, 1.3f);
                } else {
                    int max = eggStorage.getMaxSelected();
                    if (user.getMined() >= 500000) {
                        max++;
                    }
                    if (user.getMined() >= 150000) {
                        max++;
                    }
                    if (eggStorage.getSelectedEggs().size() + 1 > max) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 5, 1);
                        player.sendMessage(textUtils.error("You have the maxed amount of eggs equipped."));
                        return;
                    }
                    eggStorage.removeEggFromStorage(egg);
                    eggStorage.addEggToSelected(egg);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 5, 1);
                }
                new EggGui(player);
            });
            gui.addItem(eggItem);
        }

        // Open to player
        gui.open(player);
    }

}
