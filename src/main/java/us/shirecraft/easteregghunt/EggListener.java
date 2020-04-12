package us.shirecraft.easteregghunt;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class EggListener implements Listener {
    public EggListener(EasterEggHunt plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPickUpEgg(EntityPickupItemEvent ev) {
        ItemStack is = ev.getItem().getItemStack();
        if(isEasterEgg(is)) {
            if(ev.getEntity() instanceof Player) {
                Player player = (Player) ev.getEntity();
                NBTItem nbtItem = new NBTItem(is);
                String eggType = nbtItem.getString("EasterEgg");
                player.sendMessage("§6 ** You found a " + eggType + " Egg");
                player.playNote(player.getLocation(), Instrument.XYLOPHONE, Note.sharp(1, Note.Tone.F));
                player.spawnParticle(Particle.SPELL_INSTANT, ev.getItem().getLocation(), 5);
                ev.getItem().remove();
                plugin.sendToWebServer(player, eggType);
            }
            ev.setCancelled(true);
        }
    }

    public boolean isEasterEgg(ItemStack is) {
        if(is.getType() == Material.PLAYER_HEAD) {
            NBTItem nbtItem = new NBTItem(is);
            return nbtItem.getKeys().contains("EasterEgg");
        }
        return false;
    }

    private final EasterEggHunt plugin;
}
