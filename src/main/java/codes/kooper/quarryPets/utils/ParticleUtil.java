package codes.kooper.quarryPets.utils;

import codes.kooper.quarryPets.QuarryPets;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleUtil {

    public static void createRainbowSpiral(Player player) {
        new BukkitRunnable() {
            double t = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                final Location center = player.getLocation();

                t += Math.PI / 16;
                double radius = 1.5;

                // Spiral 1
                double x1 = radius * Math.cos(t);
                double z1 = radius * Math.sin(t);
                Location loc1 = center.clone().add(x1, t / 16, z1);
                center.getWorld().spawnParticle(Particle.DUST, loc1, 0, getRainbowColor(t));

                // Spiral 2
                double x2 = radius * Math.cos(t + Math.PI);
                double z2 = radius * Math.sin(t + Math.PI);
                Location loc2 = center.clone().add(x2, t / 16, z2);
                center.getWorld().spawnParticle(Particle.DUST, loc2, 0, getRainbowColor(t + Math.PI));

                if (t > Math.PI * 16) {
                    this.cancel(); // Stop after a full spiral
                }
            }
        }.runTaskTimer(QuarryPets.getInstance(), 0, 1);
    }

    private static Particle.DustOptions getRainbowColor(double t) {
        // Generate colors in the rainbow spectrum
        float hue = (float) ((t % (2 * Math.PI)) / (2 * Math.PI)); // Normalize to 0-1
        java.awt.Color color = java.awt.Color.getHSBColor(hue, 1.0f, 1.0f);
        return new Particle.DustOptions(
                org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()),
                1 // Particle size
        );
    }

}
