package dev.lumas.lumaitems.util;

import dev.lumas.lumaitems.model.item.AttributeContainer;
import org.bukkit.attribute.Attribute;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface SharedContainers {
    AttributeContainer.Builder SCALE = AttributeContainer.builder().setKey("scale").setAttribute(Attribute.SCALE);
    AttributeContainer.Builder AIR_DRAG_MODIFIER = AttributeContainer.builder().setKey("air_drag_modifier").setAttribute(Attribute.AIR_DRAG_MODIFIER);
}
