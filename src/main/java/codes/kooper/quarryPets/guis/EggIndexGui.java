package codes.kooper.quarryPets.guis;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.quarryPets.models.PetModel;
import codes.kooper.shaded.gui.builder.item.ItemBuilder;
import codes.kooper.shaded.gui.components.GuiType;
import codes.kooper.shaded.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static codes.kooper.koopKore.KoopKore.numberUtils;
import static codes.kooper.koopKore.KoopKore.textUtils;

public class EggIndexGui {

    public EggIndexGui(Player player) {
        Gui gui = Gui.gui(GuiType.HOPPER)
                .disableAllInteractions()
                .title(Component.text("Eggs Index"))
                .create();

        for (EggModel eggModel : QuarryPets.getInstance().getEggManager().getEggs().values()) {
            List<Component> lore = new ArrayList<>(List.of(textUtils.colorize(eggModel.getColor2() + "Hatch this egg by mining <white>" + numberUtils.commaFormat(eggModel.getBlocks()) + eggModel.getColor2() + " blocks."),
                    Component.empty(),
                    textUtils.colorize(eggModel.getColor2() + "This egg contains the following pets:")));
            eggModel.getPetChances()
                    .entrySet()
                    .stream()
                    .sorted((entry1, entry2) -> Double.compare(entry2.getKey(), entry1.getKey()))
                    .forEach(entry -> {
                        String pet = entry.getValue();
                        double chance = entry.getKey();
                        PetModel petModel = QuarryPets.getInstance().getPetManager().getPetModel(eggModel.getKey(), pet);
                        lore.add(textUtils.colorize(" <dark_gray>┗ " + petModel.color1() + textUtils.capitalize(petModel.name()) + " <green>(" + chance + "%)"));
                    });

            gui.addItem(ItemBuilder.from(eggModel.getPhysicalEgg())
                    .lore(lore).asGuiItem());
        }

        gui.open(player);
    }

}
