package dev.jsinco.luma.util.tiers;

import java.util.List;

public class ThanksgivingEventTier extends Tier {


    public static final ThanksgivingEventTier THANKSGIVING_2024 = new ThanksgivingEventTier(
            "<b><#536C40>T<#737D4B>h<#948D57>a<#B49E62>n<#D4AE6D>k<#D3A462>s<#D29B57>g<#D0914C>i<#CF8741>v<#C97C41>i<#C47241>n<#BE6740>g <#B05E3E>2<#A95F3B>0<#A16139>2<#996236>4</b>"
    );

    protected ThanksgivingEventTier(String s) {
        super(s);
    }

    public List<String> getConsumeMessages() {
        return List.of(
                "<gray>:3 <#fddeaf>Yum!",
                "<gray>:o <#fddeaf>Delicious!",
                "<gray>:* <#B2E55D>I think I'm gonna be sick...",
                "<gray>:) <#fddeaf>That was tasty!",
                "<gray>:| <#fddeaf>That tasted kind of funny...?"
        );
    }

    public String getCannotConsumeMessage() {
        return "<gray>›.‹ <#E55DB0>I'm too full right now!";
    }

}
