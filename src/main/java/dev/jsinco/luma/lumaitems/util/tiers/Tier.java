package dev.jsinco.luma.lumaitems.util.tiers;

import dev.jsinco.luma.lumaitems.util.MiniMessageUtil;
import net.kyori.adventure.text.Component;

public class Tier {

    public static final Tier DEPRECATED = new Tier("<b><yellow>Deprecated</yellow></b>");
    public static final Tier DEBUG = new Tier("<b><light_green>Debug</light_green></b>");
    public static final Tier ASTRAL = new Tier("<b><#AC87FB>Astral</#AC87FB></b>");
    public static final Tier CARNIVAL_2024 = new Tier("<b><#8EC4F7>C<#B4B7E8>a<#D9A9DA>r<#FF9CCB>n<#F2BAB6>i<#E4D7A2>v<#D7F58D>a<#E4F88C>l <#FFFE8A>2<#FFF07E>0<#FFE171>2<#FFD365>4</b>");
    public static final Tier WINTER_2024 = new Tier("<b><#CCD8E9>Winter 2024</b>");
    public static final Tier VALENTIDE_2025 = new Tier("<b><#FF3334>V<#FF474A>a<#FF5B61>l<#FF6F77>e<#FF8189>n<#FF939C>t<#FFA4AE>i<#FFB6C0>d<#FFA7B2>e <#FF8896>2<#FF6C75>0<#FF4F55>2<#FF3334>5");


    private final String mmTierString;

    protected Tier(String s) {
        this.mmTierString = s;
    }

    public String getTierString() {
        return mmTierString;
    }

    public Component toComponent() {
        return MiniMessageUtil.mm(mmTierString);
    }

    @Override
    public String toString() {
        return mmTierString;
    }
}
