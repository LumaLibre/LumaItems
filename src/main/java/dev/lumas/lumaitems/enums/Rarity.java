package dev.lumas.lumaitems.enums;

import dev.lumas.lumaitems.configuration.files.RelicsYml;
import dev.lumas.lumaitems.registry.Registry;
import org.bukkit.Material;

import java.util.List;

public enum Rarity {
    ASTRAL("#AC87FB", 14, "Astral"),
    LUNAR("#6255fb", 7, "Lunar"),
    NOVA("#75c3fb", 2, "Nova"),
    PULSAR("#c773fb", 1, "Pulsar"),
    SOLAR("#EEFB5F", 1, "Solar"),
    DELTA("#DE509D", 1, "Delta");

    // Data
    public static final List<Rarity> BOSS = List.of(NOVA);
    public static final List<Rarity> GENERIC = List.of(PULSAR, SOLAR, DELTA);

    public final String rgb;
    public final int algorithmWeight;
    public final String friendlyName;

    Rarity(String rgb, int algorithmWeight, String friendlyName) {
        this.rgb = rgb;
        this.algorithmWeight = algorithmWeight;
        this.friendlyName = friendlyName;
    }


    public String getRgb() {
        return rgb;
    }

    public int getAlgorithmWeight() {
        return algorithmWeight;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getTier() {
        return "<" + this.getRgb() + ">" + "<b>" + this.getFriendlyName();
    }


    public List<Material> getMaterials() {
        RelicsYml.RelicMaterialsSection relicMaterialsSection = Registry.CONFIGS.getOrThrow(RelicsYml.class).getRelicMaterials();

        return switch (this) {
            case ASTRAL -> List.of();
            case LUNAR -> relicMaterialsSection.getLunar();
            case NOVA -> relicMaterialsSection.getNova();
            case PULSAR -> relicMaterialsSection.getPulsar();
            case SOLAR -> relicMaterialsSection.getSolar();
            case DELTA -> relicMaterialsSection.getDelta();
        };
    }
}
