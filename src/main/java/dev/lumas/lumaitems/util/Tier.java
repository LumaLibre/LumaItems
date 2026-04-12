package dev.lumas.lumaitems.util;

import dev.lumas.core.util.Text;
import net.kyori.adventure.text.Component;

public class Tier {

    public static final Tier BLANK = new Tier("");
    public static final Tier DEPRECATED = new Tier("<b><yellow>Deprecated</yellow></b>");
    public static final Tier DEBUG = new Tier("<b><green>Debug</green></b>");
    public static final Tier ASTRAL = new Tier("<b><#AC87FB>Astral</#AC87FB></b>");
    public static final Tier COLLECTIBLE = new Tier("<b><aqua>Collectible</aqua></b>");
    public static final Tier CARNIVAL_2024 = new Tier("<b><#8EC4F7>C<#B4B7E8>a<#D9A9DA>r<#FF9CCB>n<#F2BAB6>i<#E4D7A2>v<#D7F58D>a<#E4F88C>l <#FFFE8A>2<#FFF07E>0<#FFE171>2<#FFD365>4</b>");
    public static final Tier THANKSGIVING_2024 = new Tier("<b><#536C40>T<#737D4B>h<#948D57>a<#B49E62>n<#D4AE6D>k<#D3A462>s<#D29B57>g<#D0914C>i<#CF8741>v<#C97C41>i<#C47241>n<#BE6740>g <#B05E3E>2<#A95F3B>0<#A16139>2<#996236>4</b>");
    public static final Tier WINTER_2024 = new Tier("<b><#CCD8E9>Winter 2024</b>");
    public static final Tier VALENTIDE_2025 = new Tier("<b><#954381>V<#AB4A8D>a<#C15299>l<#D659A4>e<#EC60B0>n<#ED68B5>t<#ED70BB>i<#EE78C0>d<#EE80C6>e <#D977B9>2<#C266A6>0<#AC5494>2<#954381>5");
    public static final Tier EASTER_2025 = new Tier("<b><#A687CA>E<#6EABD3>a<#36CEDC>s<#63DBA3>t<#8FE86A>e<#C7E961>r <#FFD054>2<#FFB750>0<#FF9866>2<#FF787C>5</b>");
    public static final Tier PRIDE_2025 = new Tier("<b><#FF6666>P<#FF925E>r<#FFBD55>i<#FFD35B>d<#FFE960>e <#CEF15B>2<#9DE24F>0<#92D8A5>2<#87CEFA>5</b>");
    public static final Tier SUMMER_2025 = new Tier("<b><gradient:#ff4e50:#fc913a:#f9d62e:#eae374:#97c753>Lumalympics 2025</gradient></b>");
    public static final Tier HALLOWEEN_2025 = new Tier("<b><gradient:#602749:#b14623:#f6921d>Lumaween 2025</gradient></b>");
    public static final Tier CHRISTMAS_2025 = new Tier("<b><gradient:#1d7240:#5e9f52:#e4ba58:#f2b054:#f06f3f:#ee4631:#e4352b:#a61e20>Christmas 2025</gradient></b>");
    public static final Tier VALENTIDE_2026 = new Tier("<b><gradient:#954381:#ee78c0:#ec6e95:#cb354e>Valentide 2026</gradient></b>");
    public static final Tier WONDERLAND_2026 = new Tier("<b><gradient:#5d85dc:#E56A91:#F3AA4C:#CA51CB>Wonderland '26</gradient></b>").alt("<b><gradient:#7B859D:#996779:#A7957B:#6B496B>Wonderland '26</gradient></b>");


    private final String main;
    private String alt;

    public Tier(String s) {
        this.main = s;
        this.alt = s;
    }

    public String getTierString() {
        return main;
    }

    public Component toComponent() {
        return Text.mm(main);
    }

    @Override
    public String toString() {
        return main;
    }

    public Tier alt(String s) {
        this.alt = s;
        return this;
    }

    public Tier alt() {
        return new Tier(alt);
    }
}
