package com.saicone.picospacos.core.item;

import com.saicone.picospacos.api.item.ItemAction;
import com.saicone.picospacos.core.item.action.ActionList;
import com.saicone.picospacos.util.jar.JarRuntime;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                        register(builder.id(), builder);
                    }
                }
            }
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public ItemAction.Builder<?> register(@NotNull String id, @NotNull ItemAction.Builder<?> builder) {
        return actionTypes.put(id, builder);
    }

    @NotNull
    public ItemAction read(@Nullable Object object) {
        if (object == null) {
            return ItemAction.empty();
        }

        final List<ItemAction> actions = new ArrayList<>();
        if (object instanceof ConfigurationSection) {
            for (String id : ((ConfigurationSection) object).getKeys(false)) {
                final ItemAction action = read(id, ((ConfigurationSection) object).get(id));
                if (action != null) {
                    actions.add(action);
                }
            }
        } else if (object instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                final ItemAction action = read(String.valueOf(entry.getKey()), entry.getValue());
                if (action != null) {
                    actions.add(action);
                }
            }
        } else if (object instanceof List) {
            for (Object o : (List<?>) object) {
                final ItemAction action = read(o);
                if (action.isEmpty()) {
                    continue;
                }
                if (action instanceof ActionList) {
                    actions.addAll(((ActionList) action).getActions());
                } else {
                    actions.add(action);
                }
            }
        } else {
            final String[] split = String.valueOf(object).split(":", 2);
            final ItemAction action = read(split[0].trim(), split.length > 1 ? split[1].trim() : null);
            if (action != null) {
                actions.add(action);
            }
        }

        if (actions.isEmpty()) {
            return ItemAction.empty();
        } else if (actions.size() == 1) {
            return actions.get(0);
        } else {
            return new ActionList(actions);
        }
    }

    @Nullable
    public ItemAction read(@NotNull String id, @Nullable Object object) {
        for (var entry : actionTypes.entrySet()) {
            if (entry.getValue().matches(id)) {
                return entry.getValue().build(id, object);
            }
        }
        return null;
    }
}
