package codes.kooper.quarryPets.models;

import com.github.retrooper.packetevents.util.Vector3f;

public record PetModel(String name, EggModel egg, String color1, String color2, double chance, String model, Vector3f offset) { }
