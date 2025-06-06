package dev.jsinco.luma.lumaitems.util.tiers;

import dev.jsinco.luma.lumaitems.util.MiniMessageUtil;
import net.kyori.adventure.text.Component;

public class Tier {

    public static final Tier DEPRECATED = new Tier("<b><yellow>Deprecated</yellow></b>");
    public static final Tier DEBUG = new Tier("<b><light_green>Debug</light_green></b>");
    public static final Tier ASTRAL = new Tier("<b><#AC87FB>Astral</#AC87FB></b>");
    public static final Tier CARNIVAL_2024 = new Tier("<b><#8EC4F7>C<#B4B7E8>a<#D9A9DA>r<#FF9CCB>n<#F2BAB6>i<#E4D7A2>v<#D7F58D>a<#E4F88C>l <#FFFE8A>2<#FFF07E>0<#FFE171>2<#FFD365>4</b>");
    public static final Tier WINTER_2024 = new Tier("<b><#CCD8E9>Winter 2024</b>");
    public static final Tier VALENTIDE_2025 = new Tier("<b><#954381>V<#AB4A8D>a<#C15299>l<#D659A4>e<#EC60B0>n<#ED68B5>t<#ED70BB>i<#EE78C0>d<#EE80C6>e <#D977B9>2<#C266A6>0<#AC5494>2<#954381>5");
    public static final Tier EASTER_2025 = new Tier("<b><#A687CA>E<#6EABD3>a<#36CEDC>s<#63DBA3>t<#8FE86A>e<#C7E961>r <#FFD054>2<#FFB750>0<#FF9866>2<#FF787C>5</b>");
    public static final Tier PRIDE_2025 = new Tier("<b><#FF6666>P<#FF925E>r<#FFBD55>i<#FFD35B>d<#FFE960>e <#CEF15B>2<#9DE24F>0<#92D8A5>2<#87CEFA>5</b>");


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
