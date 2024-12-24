package codes.kooper.quarryPets.managers;

import codes.kooper.koopKore.utils.Tasks;
import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.models.Egg;
import codes.kooper.quarryPets.database.models.EggStorage;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.quarryPets.models.PetModel;
import codes.kooper.shaded.nbtapi.NBT;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import dev.lone.itemsadder.api.CustomStack;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.ItemDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static codes.kooper.koopKore.KoopKore.textUtils;

@Getter
public class EggManager {
    private final LinkedHashMap<String, EggModel> eggs;
    private final Set<UUID> eggOpening;
    private final static Location eggLocation = new Location(Bukkit.getWorld("mine"), -8.5, -27.5, 65.5, 180, 0);
    private final static Location playerLocation = new Location(Bukkit.getWorld("mine"), -8.5, -28, 58.5, 0, 0);

    public EggManager() {
        eggs = new LinkedHashMap<>();
        eggOpening = new HashSet<>();
        loadEggs();
    }

    public void loadEggs() {
        ConfigurationSection eggsSection = QuarryPets.getInstance().getConfig().getConfigurationSection("eggs");
        if (eggsSection == null) return;
        int index = eggsSection.getKeys(false).size();
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
                    index,
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
            index--;
        }
    }

    public void progressEggs(Player player, int amount) {
        Optional<EggStorage> eggStorageOptional = QuarryPets.getInstance().getEggStorageCache().get(player.getUniqueId());
        if (eggStorageOptional.isEmpty()) return;
        EggStorage eggStorage = eggStorageOptional.get();
        for (Egg egg : eggStorage.getSelectedEggs()) {
            if (egg.progressEgg(amount)) {
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

    public void hatchEgg(Player player, Egg egg) {
        player.playSound(player.getLocation(), Sound.BLOCK_END_GATEWAY_SPAWN, 5, 1.2f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35, 5, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false, false));
        eggOpening.add(player.getUniqueId());
        final Location previousLoc = player.getLocation();
        player.teleport(playerLocation);
        Tasks.runAsyncLater(() -> {
            EggModel eggModel = egg.getModel();

            // Spawn Egg
            ItemStack eggItem = eggModel.getPhysicalEgg();
            WrapperLivingEntity eggEntity = new WrapperLivingEntity(EntityTypes.ITEM_DISPLAY);
            ItemDisplayMeta itemDisplayMeta = (ItemDisplayMeta) eggEntity.getEntityMeta();
            itemDisplayMeta.setItem(SpigotConversionUtil.fromBukkitItemStack(eggItem));
            itemDisplayMeta.setScale(new Vector3f(5, 5, 5));

            // Add viewer and spawn the egg entity
            eggEntity.addViewerSilently(player.getUniqueId());
            eggEntity.spawn(SpigotConversionUtil.fromBukkitLocation(eggLocation.clone().add(0, 4, 0)));

            new BukkitRunnable() {
                int shakes = 0;

                @Override
                public void run() {
                    if (shakes > 15) cancel();
                    if (shakes == 15) {
                        eggEntity.despawn();

                        String pet = eggModel.getRandomPet();
                        PetModel petModel = QuarryPets.getInstance().getPetManager().getPetModel(eggModel.getKey(), pet);

                        if (petModel.chance() < 0.1) {
                            WrapperEntity lightning = new WrapperEntity(EntityTypes.LIGHTNING_BOLT);
                            lightning.addViewerSilently(player.getUniqueId());
                            lightning.spawn(SpigotConversionUtil.fromBukkitLocation(eggLocation));
                        }

                        WrapperEntity petTextDisplay = new WrapperEntity(EntityTypes.TEXT_DISPLAY);
                        TextDisplayMeta textDisplayMeta = (TextDisplayMeta) petTextDisplay.getEntityMeta();
                        textDisplayMeta.setTranslation(new Vector3f(0, 5f, 0));
                        textDisplayMeta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.VERTICAL);
                        textDisplayMeta.setScale(new Vector3f(3, 3, 3));
                        textDisplayMeta.setSeeThrough(true);
                        textDisplayMeta.setText(textUtils.colorize(petModel.color1() + "<bold>" + textUtils.capitalize(petModel.name()).toUpperCase() + " PET<newline><reset>" + petModel.color2() + "Chance: <green>" + petModel.chance() + "%"));
                        petTextDisplay.addViewerSilently(player.getUniqueId());
                        petTextDisplay.spawn(SpigotConversionUtil.fromBukkitLocation(eggLocation));

                        WrapperLivingEntity petDisplay = new WrapperLivingEntity(EntityTypes.ITEM_DISPLAY);
                        ItemStack petIcon;
                        CustomStack stack = CustomStack.getInstance(petModel.model());

                        if (stack != null) {
                            petIcon = stack.getItemStack();
                        } else {
                            petIcon = new ItemStack(Material.BARRIER);
                        }

                        ItemDisplayMeta itemDisplayMeta1 = (ItemDisplayMeta) petDisplay.getEntityMeta();
                        itemDisplayMeta1.setItem(SpigotConversionUtil.fromBukkitItemStack(petIcon));
                        itemDisplayMeta1.setTranslation(petModel.offset());
                        itemDisplayMeta1.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.VERTICAL);
                        itemDisplayMeta1.setScale(new Vector3f(4.5f, 4.5f, 4.5f));
                        petDisplay.addViewerSilently(player.getUniqueId());
                        petDisplay.addPassenger(petTextDisplay.getEntityId());
                        Location petLoc = eggLocation.clone();
                        petLoc.setYaw(90);
                        petDisplay.spawn(SpigotConversionUtil.fromBukkitLocation(petLoc));

                        player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 5, 1.3f);

                        cancel();
                        Tasks.runSyncLater(() -> {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35, 5, false, false, false));
                            player.teleport(previousLoc);
                            player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 5, 1.3f);
                        }, 75);
                        Tasks.runSyncLater(() -> {
                            petDisplay.despawn();
                            petTextDisplay.despawn();
                            eggOpening.remove(player.getUniqueId());
                            player.removePotionEffect(PotionEffectType.INVISIBILITY);
                        }, 100L);
                        return;
                    }

                    // Shake
                    double shakeAmount = 8.0;
                    float randomYaw = (float) ((Math.random() - 0.5) * shakeAmount * 2);
                    float randomPitch = (float) ((Math.random() - 0.5) * shakeAmount * 2);

                    // Update egg position and particles
                    player.playSound(player.getLocation(), Sound.ENTITY_TURTLE_EGG_HATCH, 5, 1.4f);

                    eggEntity.rotateHead(randomYaw, randomPitch);

                    player.spawnParticle(Particle.BLOCK, eggLocation, 10, 0.3, 0.3, 0.3, eggItem.getType().createBlockData());

                    shakes++;
                }
            }.runTaskTimerAsynchronously(QuarryPets.getInstance(), 5L, 3L);
        }, 15L);
    }
}
