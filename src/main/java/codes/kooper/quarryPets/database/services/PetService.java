package codes.kooper.quarryPets.database.services;

import codes.kooper.koopKore.database.managers.AbstractDataManager;
import codes.kooper.quarryPets.database.models.PetStorage;
import codes.kooper.shaded.mongodb.client.model.Filters;

public class PetService extends AbstractDataManager<PetStorage> {

    public PetService() {
        super("pets", PetStorage.class);
    }

    /**
     * Save a pet storage to the database.
     *
     * @param petStorage The Pet Storage to save
     */
    public void savePetStorage(PetStorage petStorage) {
        findById(petStorage.getId()).ifPresentOrElse((u) -> collection.replaceOne(Filters.eq("_id", petStorage.getId()), petStorage), () -> collection.insertOne(petStorage));
    }
}
