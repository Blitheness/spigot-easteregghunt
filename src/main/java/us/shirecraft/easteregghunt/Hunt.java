package us.shirecraft.easteregghunt;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

public class Hunt {
    public Hunt(World world, ProtectedRegion region) {
        this.world = world;
        this.region = region;
    }

    public boolean isEnabled() { return enabled; }

    @Override
    public String toString() {
        return region.getId() + " (" + world.getName() + ")";
    }

    private World world;
    private ProtectedRegion region;
    private boolean enabled;
}
