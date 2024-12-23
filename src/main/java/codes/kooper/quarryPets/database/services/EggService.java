package codes.kooper.quarryPets.database.services;

import codes.kooper.koopKore.database.managers.AbstractDataManager;
import codes.kooper.quarryPets.database.models.EggStorage;
import codes.kooper.shaded.mongodb.client.model.Filters;

public class EggService extends AbstractDataManager<EggStorage> {

    public EggService() {
        super("eggs", EggStorage.class);
    }

    /**
     * Save an egg storage to the database.
     *
     * @param eggStorage The Egg Storage to save
     */
    public void saveEggStorage(EggStorage eggStorage) {
        findById(eggStorage.getId()).ifPresentOrElse((u) -> collection.replaceOne(Filters.eq("_id", eggStorage.getId()), eggStorage), () -> collection.insertOne(eggStorage));
    }
}
