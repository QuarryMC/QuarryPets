package codes.kooper.quarryPets.guis;

import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.shaded.gui.builder.item.ItemBuilder;
import codes.kooper.shaded.gui.components.GuiType;
import codes.kooper.shaded.gui.guis.Gui;
import codes.kooper.shaded.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static codes.kooper.koopKore.KoopKore.numberUtils;
import static codes.kooper.koopKore.KoopKore.textUtils;

public class EggIndexGui {

    public EggIndexGui(Player player) {
        Gui gui = Gui.gui(GuiType.CHEST)
                .title(Component.text("Eggs Index"))
                .rows(3)
                .create();
        gui.getFiller().fillBorder(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());

        for (EggModel eggModel : QuarryPets.getInstance().getEggManager().getEggs().values()) {
            List<Component> lore = new ArrayList<>(List.of(textUtils.colorize(eggModel.getColor2() + "Hatch this egg by mining <white>" + numberUtils.commaFormat(eggModel.getBlocks()) + eggModel.getColor2() + " blocks."),
                    Component.empty(),
                    textUtils.colorize(eggModel.getColor2() + "This egg contains the following pets:")));
            eggModel.getPetChances().forEach((chance, pet) -> {
                lore.add(textUtils.colorize(" <dark_gray>┗ " + ));
            });
            GuiItem item = ItemBuilder.from(eggModel.getEgg())
                    .lore(lore).asGuiItem();
        }

        gui.open(player);
    }

}
