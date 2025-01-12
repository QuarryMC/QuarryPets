package codes.kooper.quarryPets.managers;

import codes.kooper.koopKore.database.models.User;
import codes.kooper.koopKore.utils.Tasks;
import codes.kooper.quarryPets.QuarryPets;
import codes.kooper.quarryPets.database.models.Pet;
import codes.kooper.quarryPets.database.models.PetStorage;
import codes.kooper.quarryPets.models.EggModel;
import codes.kooper.quarryPets.models.PetModel;
import codes.kooper.shaded.entitylib.wrapper.WrapperEntity;
import codes.kooper.shaded.entitylib.wrapper.WrapperLivingEntity;
import codes.kooper.shaded.nbtapi.NBT;
import codes.kooper.shaded.packetevents.api.util.SpigotConversionUtil;
import codes.kooper.shaded.packetevents.protocol.attribute.Attributes;
import codes.kooper.shaded.packetevents.protocol.entity.type.EntityTypes;
import codes.kooper.shaded.packetevents.util.Vector3f;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static codes.kooper.koopKore.KoopKore.textUtils;

@Getter
public class PetManager {
    private final LinkedHashMap<String, Map<String, PetModel>> pets;
    private final Map<Integer, Integer> levelingCosts;
    private final Map<UUID, List<WrapperLivingEntity>> spawnedPets;
    private final ExecutorService petUpdateService = Executors.newCachedThreadPool();

    public PetManager() {
        pets = new LinkedHashMap<>();
        levelingCosts = new HashMap<>();
        spawnedPets = new ConcurrentHashMap<>();
        Tasks.runSyncLater(this::loadPets, 50L);

        Tasks.runSyncTimer(() -> spawnedPets.forEach((uuid, pets) -> {
            final Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                spawnedPets.remove(uuid);
            }

            petUpdateService.submit(() -> teleportPetsBehindPlayer(player, pets));
        }), 0L, 1L);
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
        int index = 0;
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
                float offsetX = (float) petSection.getDouble("egg-offset.x");
                float offsetY = (float) petSection.getDouble("egg-offset.y");
                float offsetZ = (float) petSection.getDouble("egg-offset.z");
                models.put(pet, new PetModel(index, pet, eggModel, color1, color2, chance, model, new Vector3f(offsetX, offsetY, offsetZ)));
                index++;
            }
            pets.put(rarity, models);
        }
    }

    public boolean isPetItem(ItemStack item) {
        if (item == null || item.isEmpty()) return false;
        return NBT.get(item, (NBT) -> {
            return NBT.hasTag("pet");
        });
    }

    public List<String> getPetNames() {
        List<String> pets = new ArrayList<>();
        for (Map<String, PetModel> petMap : getPets().values()) {
            pets.addAll(petMap.keySet());
        }
        return pets;
    }

    public PetModel getPetModel(String pet) {
        for (String egg : getPets().keySet()) {
            PetModel petModel = getPetModel(egg, pet);
            if (petModel != null) return petModel;
        }
        return null;
    }

    public void addXPToPets(Player player, int xp, User user) {
        for (Pet pet : getSelectedPets(player)) {
            int cost = getXPCost(pet);
            if (pet.getXp() + xp >= cost) {
                int remaining = (pet.getXp() + xp) - cost;
                levelUpPet(pet, player, user);
                if (remaining <= 0) return;
                addXPToPets(player, remaining, user);
                return;
            }
            pet.addExp(xp);
        }
    }

    public void levelUpPet(Pet pet, Player player, User user) {
        pet.addLevel();
        pet.setXp(0);
        if (user.hasOption("pet_level_up_notifications")) return;
        player.playSound(player.getLocation(), Sound.ENTITY_WOLF_HOWL, 3, 1.5f);
        player.sendMessage(textUtils.colorize("<#91db69><bold>PET LEVEL UP!<reset> <green>Your " + pet.getPetModel().color1() + textUtils.capitalize(pet.getPet()) + " Pet<reset><green> has reached level " + pet.getLevel() + "!"));
    }

    public List<Pet> getSelectedPets(Player player) {
        Optional<PetStorage> petStorageOptional = QuarryPets.getInstance().getPetStorageCache().get(player.getUniqueId());
        if (petStorageOptional.isEmpty()) return null;
        final PetStorage petStorage = petStorageOptional.get();
        return petStorage.getSelectedPets();
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

    public void equipPets(Player player) {
        Optional<PetStorage> petStorageOptional = QuarryPets.getInstance().getPetStorageCache().get(player.getUniqueId());
        if (petStorageOptional.isEmpty()) return;
        final PetStorage petStorage = petStorageOptional.get();

        if (spawnedPets.containsKey(player.getUniqueId())) {
            spawnedPets.get(player.getUniqueId()).forEach(WrapperLivingEntity::despawn);
            spawnedPets.remove(player.getUniqueId());
        }

        if (petStorage.getSelectedPets().isEmpty()) return;

        final List<WrapperLivingEntity> entities = new ArrayList<>();
        final List<WrapperEntity> textDisplays = new ArrayList<>();
        final Location playerLocation = player.getLocation();

        for (Pet pet : petStorage.getSelectedPets()) {
            spawnPetEntity(pet, player, playerLocation, entities, textDisplays);
        }

        spawnedPets.put(player.getUniqueId(), entities);
    }

    private void spawnPetEntity(Pet pet, Player player, Location spawnLocation, List<WrapperLivingEntity> entities, List<WrapperEntity> textDisplays) {
        WrapperLivingEntity petEntity = new WrapperLivingEntity(EntityTypes.ARMOR_STAND);
        petEntity.getEntityMeta().setInvisible(true);
        petEntity.addViewerSilently(player.getUniqueId());
        petEntity.spawn(SpigotConversionUtil.fromBukkitLocation(spawnLocation));
        petEntity.getEquipment().setHelmet(SpigotConversionUtil.fromBukkitItemStack(pet.getPhysicalPet()));
        petEntity.getAttributes().setAttribute(Attributes.GENERIC_SCALE, 4);
        entities.add(petEntity);
    }

    public static void teleportPetsBehindPlayer(Player player, List<WrapperLivingEntity> pets) {
        if (pets.isEmpty()) return;

        // Get the player's yaw and snap it to the nearest cardinal direction
        float playerYaw = snapToCardinal(player.getLocation().getYaw());

        // Convert yaw to radians
        double yawRad = Math.toRadians(playerYaw);

        // Determine the direction to handle edge cases
        String facingDirection = getFacingDirection(playerYaw);

        // Base position directly behind the player
        Location playerLocation = player.getLocation().clone();
        double baseOffsetX = 0;
        double baseOffsetZ = 0;

        // Handle offsets for each cardinal direction
        switch (facingDirection) {
            case "WEST" -> {
                baseOffsetX = 3.0;
                baseOffsetZ = -1.2;
            }
            case "NORTH" -> {
                baseOffsetX = 1.2;
                baseOffsetZ = 3.0;
            }
            case "SOUTH" -> {
                baseOffsetX = -1.2;
                baseOffsetZ = -3.0;
            }
            case "EAST" -> {
                baseOffsetX = -3.0;
                baseOffsetZ = 1.2;
            }
        }

        Location baseLocation = playerLocation.clone().add(baseOffsetX, -5.2, baseOffsetZ);

        // Grid settings
        int gridSize = 3; // Number of pets per row
        double gridSpacing = 2.5; // Horizontal spacing between pets
        double rowSpacing = 2.0; // Vertical spacing between rows

        int index = 0;

        for (WrapperLivingEntity entity : pets) {

            // Calculate the grid row and column
            int row = index / gridSize;
            int col = index % gridSize;

            // Adjust the center offset for rows with fewer pets
            int petsInRow = Math.min(gridSize, pets.size() - row * gridSize);
            double rowCenterOffset = (petsInRow - 1) / 2.0 * gridSpacing;

            // Center the grid horizontally
            double localXOffset = (col * gridSpacing) - rowCenterOffset;

            // Calculate the depth offset for rows
            double localZOffset = -(row * rowSpacing);

            // Rotate the offsets relative to the snapped yaw
            double rotatedX = localXOffset * Math.cos(yawRad) - localZOffset * Math.sin(yawRad);
            double rotatedZ = localXOffset * Math.sin(yawRad) + localZOffset * Math.cos(yawRad);

            // Calculate the final position
            Location targetLocation = baseLocation.clone().add(rotatedX, 0, rotatedZ);

            // Create a PacketEvents location for teleporting the entity
            codes.kooper.shaded.packetevents.protocol.world.Location packetLocation = new codes.kooper.shaded.packetevents.protocol.world.Location(
                    targetLocation.getX(),
                    targetLocation.getY(),
                    targetLocation.getZ(),
                    playerYaw,
                    0
            );

            // Teleport the pet
            entity.teleport(packetLocation);

            index++;
        }
    }

    /**
     * Snap the player's yaw to the nearest cardinal direction (0, 90, 180, 270).
     */
    private static float snapToCardinal(float yaw) {
        // Normalize yaw to the range [0, 360)
        yaw = yaw % 360; // Keep within -360 to 360
        if (yaw < 0) yaw += 360; // Convert negative values to positive (e.g., -90 -> 270)

        // Snap to the nearest cardinal direction
        if (yaw >= 315 || yaw < 45) return 0;     // South
        else if (yaw >= 45 && yaw < 135) return 90;  // West
        else if (yaw >= 135 && yaw < 225) return 180; // North
        else return 270;  // East
    }

    /**
     * Determine the facing direction based on the snapped yaw.
     */
    private static String getFacingDirection(float yaw) {
        if (yaw == 0) return "SOUTH";
        else if (yaw == 90) return "WEST";
        else if (yaw == 180) return "NORTH";
        else return "EAST";
    }
}
