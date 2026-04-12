package dev.lumas.lumaitems.util;

import dev.lumas.lumaitems.model.item.AttributeContainer;
import org.bukkit.attribute.Attribute;

public interface SharedContainers {

    AttributeContainer.Builder SCALE = AttributeContainer.builder().setKey("scale").setAttribute(Attribute.SCALE);
}
