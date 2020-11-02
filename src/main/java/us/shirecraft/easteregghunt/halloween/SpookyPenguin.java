package us.shirecraft.easteregghunt.halloween;

import us.shirecraft.easteregghunt.TreasureItem;

public class SpookyPenguin extends TreasureItem {
    public SpookyPenguin() {
        super("Spooky Penguin",100, 2);
        setTexture(TEXTURE);
        setPlayerUuid(PLAYER_UUID);
    }

    private final String TEXTURE     = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTlhNjQ1MGY1ZTMyZTE1ZDkyZmJkMjg0MTZkYzJiMjQwNzBhZTZmMDYyMTIxNzk4YjNiYTU0OWM5YzkwNzM3MiJ9fX0=";
    private final String PLAYER_UUID = "7e858f48-3b6e-45db-8a85-8e9d527372e8";
}
