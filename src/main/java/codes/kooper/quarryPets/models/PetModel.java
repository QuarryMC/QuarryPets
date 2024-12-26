package codes.kooper.quarryPets.models;

import codes.kooper.quarryPets.database.models.Pet;
import codes.kooper.shaded.packetevents.util.Vector3f;

import java.util.concurrent.ThreadLocalRandom;

public record PetModel(int index, String name, EggModel egg, String color1, String color2, double chance, String model, Vector3f offset) {

    public Pet getPet() {
        // Calculate dynamic minimum for sellBoost based on chance
        double dynamicMinSell = egg.getSellBoostMin() + (1.0 - chance) * (egg.getSellBoostMax() - egg.getSellBoostMin());
        dynamicMinSell = Math.min(dynamicMinSell, egg.getSellBoostMax());

        // Generate the sellBoost value within the adjusted range
        double sellBoost = ThreadLocalRandom.current().nextDouble(dynamicMinSell, egg.getSellBoostMax() + Double.MIN_VALUE);

        // Calculate dynamic minimum for fortuneBoost based on chance
        double dynamicMinFortune = egg.getFortuneBoostMin() + (1.0 - chance) * (egg.getFortuneBoostMax() - egg.getFortuneBoostMin());
        dynamicMinFortune = Math.min(dynamicMinFortune, egg.getFortuneBoostMax());

        // Generate the fortuneBoost value within the adjusted range
        double fortuneBoost = ThreadLocalRandom.current().nextDouble(dynamicMinFortune, egg.getFortuneBoostMax() + Double.MIN_VALUE);

        // Calculate dynamic minimum for luckyBlockNuker based on chance
        int luckyBlockNuker = 0;
        if (egg.getLuckyBlockNukerMax() > 0) {
            int dynamicMinLuckyBlock = (int) (egg.getLuckyBlockNukerMin() + (1.0 - chance) * (egg.getLuckyBlockNukerMax() - egg.getLuckyBlockNukerMin()));
            dynamicMinLuckyBlock = Math.min(dynamicMinLuckyBlock, egg.getLuckyBlockNukerMax());

            // Generate the luckyBlockNuker value within the adjusted range
            luckyBlockNuker = ThreadLocalRandom.current().nextInt(dynamicMinLuckyBlock, egg.getLuckyBlockNukerMax() + 1);
        }

        // Return a Pet object using sellBoost, fortuneBoost, and luckyBlockNuker
        return new Pet(name, egg.getKey(), sellBoost, fortuneBoost, luckyBlockNuker, 1, 0);
    }

}
