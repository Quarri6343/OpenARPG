package quarri6343.openarpg;

/**
 * 召喚用モンスター
 */
public enum SummonableMonsters {
    ZOMBIE("ゾンビ", "minecraft:zombie"),
    SKELETON("スケルトン", "minecraft:skeleton"),
    SPIDER("スパイダー", "minecraft:spider"),
    SILVERFISH("シルバーフィッシュ", "minecraft:silverfish"),
    ;
    
    private final String displayName;
    private final String registryName;

    SummonableMonsters(String displayName, String registryName) {
        this.displayName = displayName;
        this.registryName = registryName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRegistryName() {
        return registryName;
    }
}
