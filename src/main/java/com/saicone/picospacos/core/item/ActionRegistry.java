package com.saicone.picospacos.core.item;

import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.util.jar.JarRuntime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ActionRegistry {

    private final Map<String, ItemAction.Builder<?>> actionTypes = new HashMap<>();

    public void load() {
        try {
            for (Class<? extends ItemAction> actionType : JarRuntime.of(ActionRegistry.class).reload().subClasses(ItemAction.class)) {
                for (Field field : actionType.getDeclaredFields()) {
                    if (field.getName().equalsIgnoreCase("BUILDER")
                            && ItemAction.Builder.class.isAssignableFrom(field.getType())
                            && Modifier.isStatic(field.getModifiers())
                            && Modifier.isFinal(field.getModifiers())
                    ) {
                        final ItemAction.Builder<?> builder = (ItemAction.Builder<?>) field.get(null);
                        actionTypes.put(builder.id(), builder);
                    }
                }
            }
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public ItemAction.Builder<?> put(@NotNull String id, @NotNull ItemAction.Builder<?> builder) {
        return actionTypes.put(id, builder);
    }

    @NotNull
    public ItemAction read(@NotNull Object object) {

    }
}
