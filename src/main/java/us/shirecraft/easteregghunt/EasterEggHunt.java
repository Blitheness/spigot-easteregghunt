package us.shirecraft.easteregghunt;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import us.shirecraft.easteregghunt.christmas.*;
import us.shirecraft.easteregghunt.easter.*;
import us.shirecraft.easteregghunt.halloween.*;

import java.io.IOException;
import java.util.*;


public class EasterEggHunt extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        _config = getConfig();

        // Create empty lists
        _hunts = new ArrayList<>();
        _tasks = new ArrayList<>();

        // Egg hunt enabled?
        if(!_config.getBoolean("eggHuntEnabled")) {
            getLogger().info("Treasure hunts globally disabled in config.yml. Plugin will disable itself.");
            getServer().getPluginManager().disablePlugin(this);
        } else {
            // Initialise egg hunts
            new EggListener(this);
            initialiseHunts();
        }
    }

    @Override
    public void onDisable() {
        stopHunts();
        saveConfig();
    }

    private void initialiseHunts() {
        // Get WG instance
        _worldGuard = WorldGuard.getInstance();

        // Get region container
        _regionContainer = getWg().getPlatform().getRegionContainer();

        // Get region(s) of egg hunt(s)
        ConfigurationSection hunts = _config.getConfigurationSection("hunts");
        // Get names of worlds in config file
        Set<String> worlds = hunts.getKeys(false);

        if(!worlds.isEmpty()) {
            for (String worldName : worlds) {
                // For each world, get the region(s) under it
                LinkedHashSet<String> regions = (LinkedHashSet) _config.getConfigurationSection("hunts." + worldName).getKeys(false);

                // Find object for this world
                World world = getServer().getWorld(worldName);

                if (null != world) {
                    // Get WG region manager for this world
                    RegionManager regionManager = getRegionContainer().get(BukkitAdapter.adapt(world));

                    if(null!=regions && !regions.isEmpty()) {
                        // Get regions
                        LinkedHashSet<String> regions_tmp = (LinkedHashSet) regions.clone();
                        for (String regionName : regions_tmp) {
                            // Check if region is enabled in config file
                            boolean regionEnabled = _config.getBoolean("hunts." + worldName + "." + regionName + ".enabled");

                            // If enabled and region is defined in WorldGuard, then create hunt object and store hunt reference in ArrayList
                            if (regionEnabled && regionManager.hasRegion(regionName)) {
                                // Check type of hunt
                                String huntType = ""; //_config.getString("hunts." + worldName + "." + regionName + ".type");
                                huntType = validateHuntType(huntType);

                                ProtectedRegion region = regionManager.getRegion(regionName);
                                Hunt hunt = new Hunt(this, world, regionManager, region, huntType);
                                getHunts().add(hunt);
                            }
                        }
                    }
                }
            }
        }
        if(!getHunts().isEmpty()) {
            registerEggs();
            startEggHunts();
        } else {
            getLogger().info("No treasure hunts are enabled in the plugin's config.yml file, or it contains spelling mistakes.");
        }
    }

    /**
     * This is done badly
     */
    private void registerEggs() {
        getLogger().info("The default hunt type is " + getDefaultHuntType());

        _treasure     = new HashMap<>();
        _balancedData = new HashMap<>();
        _data         = new HashMap<>();

        if(getDefaultHuntType().equals("easter")) {
            _treasure.put(PolkaDotEgg.class, 50);
            _treasure.put(RainbowEgg.class, 20);
            _treasure.put(SunflowerEgg.class, 15);
            _treasure.put(VioletEgg.class, 30);
            _treasure.put(RegularEgg.class, 70);
            _treasure.put(DragonEgg.class, 5);
            _treasure.put(BadEgg.class, 12);
        } else if(getDefaultHuntType().equals("halloween")) {
            _treasure.put(BloodSpider.class, 20);
            _treasure.put(CarvedPumpkin.class, 30);
            _treasure.put(Cupcake.class, 30);
            _treasure.put(CursedMummy.class, 15);
            _treasure.put(Gumballs.class, 30);
            _treasure.put(SpookyPenguin.class, 2);
            _treasure.put(TrickTreatBasket.class, 60);
        } else if(getDefaultHuntType().equals("christmas")) {
            _treasure.put(FestivePenguin.class, 2);
            _treasure.put(Gift.class, 90);
            _treasure.put(GingerbreadHouse.class, 20);
            _treasure.put(GingerbreadMan.class, 4);
            _treasure.put(PlateOfCookies.class, 40);
            _treasure.put(ReindeerPlushy.class, 12);
            _treasure.put(Snowman.class, 40);
        }

        float sum = (float) _treasure.values().stream().mapToDouble(i->i).sum();

        if(getDefaultHuntType().equals("easter")) {
            _data.put(PolkaDotEgg.class, 50f / sum);
            _data.put(RainbowEgg.class, 20f / sum);
            _data.put(SunflowerEgg.class, 15f / sum);
            _data.put(BadEgg.class, 12f / sum);
            _data.put(VioletEgg.class, 30f / sum);
            _data.put(RegularEgg.class, 70f / sum);
            _data.put(DragonEgg.class, 5f / sum);
        } else if(getDefaultHuntType().equals("halloween")) {
            _data.put(BloodSpider.class, 20f / sum);
            _data.put(CarvedPumpkin.class, 30f / sum);
            _data.put(Cupcake.class, 30f / sum);
            _data.put(CursedMummy.class, 15f / sum);
            _data.put(Gumballs.class, 30f / sum);
            _data.put(SpookyPenguin.class, 2f / sum);
            _data.put(TrickTreatBasket.class, 60f / sum);
        } else if(getDefaultHuntType().equals("christmas")) {
            _data.put(FestivePenguin.class, 2f / sum);
            _data.put(Gift.class, 90f / sum);
            _data.put(GingerbreadHouse.class, 20f / sum);
            _data.put(GingerbreadMan.class, 4f / sum);
            _data.put(PlateOfCookies.class, 40f / sum);
            _data.put(ReindeerPlushy.class, 12f / sum);
            _data.put(Snowman.class, 40f / sum);
        }

        float balancedSum = 0f;
        for(Class eggClass : _treasure.keySet()) {
            balancedSum += _data.get(eggClass);
            _balancedData.put(eggClass, balancedSum);
        }
    }

    private void startEggHunts() {
        for (Hunt hunt : getHunts()) {
            Runnable huntTask = new HuntTask(this, hunt);
            _tasks.add(this.getServer().getScheduler()
                    .runTaskTimerAsynchronously(this, huntTask, TASK_DELAY_TICKS, TASK_INTERVAL_TICKS));
            getLogger().info("Hunt started: " + hunt);
        }
    }

    public WorldGuard getWg() {
        return this._worldGuard;
    }

    public RegionContainer getRegionContainer() {
        return this._regionContainer;
    }

    public ArrayList<Hunt> getHunts() {
        return this._hunts;
    }

    public HashMap<Class<?>, Integer> getTreasure() {
        return _treasure;
    }

    public HashMap<Class<?>, Float> getBalancedData() {
        return _balancedData;
    }

    private void stopHunts() {
        if(!_tasks.isEmpty()) {
            for(BukkitTask task : _tasks) {
                task.cancel();
            }
        }
    }

    public String getDefaultHuntType() {
        String defaultHuntType = _config.getString("defaultHuntType");
        return defaultHuntType;
    }

    public String validateHuntType(String huntType) {
        if(null == huntType || huntType.equals("")) {
            huntType = getDefaultHuntType();
        }
        if(!Arrays.stream(VALID_HUNT_TYPES).anyMatch(huntType::equals)) {
            huntType = VALID_HUNT_TYPES[0];
        }
        return huntType;
    }

    public boolean sendToWebServer(Player player, final String eggType, final String regionName) {
        if(!getConfig().getString("apiKey").equals("")) {
            final String playerUuid = player.getUniqueId().toString();
            final String playerName = player.getName();
            Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    String payload
                            = "data={"
                            + "\"uuid\":\"" + playerUuid + "\","
                            + "\"name\":\""+ playerName +"\","
                            + "\"egg\":\"" + eggType + "\","
                            + "\"region\":\"" + regionName + "\""
                            + "}";

                    String endpoint = getConfig().getString("apiEndpoint");
                    String key = getConfig().getString("apiKey");

                    StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_FORM_URLENCODED);
                    HttpClient httpClient = HttpClientBuilder.create().build();
                    HttpPost request = new HttpPost(endpoint + key);
                    request.setEntity(entity);

                    try {
                        httpClient.execute(request);
                    } catch (ClientProtocolException e) {
                        getLogger().warning(" ! Encountered ClientProtocolException when attempting to transmit data");
                    } catch (IOException e) {
                        getLogger().warning(" ! Encountered IOException when attempting to transmit data");
                    }
                }
            });
        }
        return false;
    }

    private FileConfiguration _config;
    private WorldGuard _worldGuard;
    private RegionContainer _regionContainer;
    private ArrayList<Hunt> _hunts;
    private ArrayList<BukkitTask> _tasks;
    private HashMap<Class<?>, Integer> _treasure;
    private HashMap<Class<?>, Float> _data;
    private HashMap<Class<?>, Float> _balancedData;
    private final int  TICKS_PER_SECOND = 20; // in an ideal situation
    private final long TASK_DELAY_TICKS = (long) (TICKS_PER_SECOND * 3);
    private final long TASK_INTERVAL_TICKS = (long) (TICKS_PER_SECOND * 15);
    private final String[] VALID_HUNT_TYPES = new String[] {"easter", "halloween", "thanksgiving", "christmas"};
}
