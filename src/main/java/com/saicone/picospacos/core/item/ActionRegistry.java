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
import java.util.Optional;

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
    public Optional<ItemAction> readAction(@Nullable Object object) {
        if (object == null) {
            return Optional.empty();
        }

        final List<ItemAction> actions = new ArrayList<>();
        if (object instanceof ConfigurationSection) {
            for (String id : ((ConfigurationSection) object).getKeys(false)) {
                readAction(id, ((ConfigurationSection) object).get(id)).ifPresent(actions::add);
            }
        } else if (object instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                readAction(String.valueOf(entry.getKey()), entry.getValue()).ifPresent(actions::add);
            }
        } else if (object instanceof List) {
            for (Object o : (List<?>) object) {
                readAction(o).ifPresent(action -> {
                    if (action instanceof ActionList) {
                        actions.addAll(((ActionList) action).getActions());
                    } else {
                        actions.add(action);
                    }
                });
            }
        } else {
            final String[] split = String.valueOf(object).split(":", 2);
            readAction(split[0].trim(), split.length > 1 ? split[1].trim() : null).ifPresent(actions::add);
        }

        if (actions.isEmpty()) {
            return Optional.empty();
        } else if (actions.size() == 1) {
            return Optional.of(actions.get(0));
        } else {
            return Optional.of(new ActionList(actions));
        }
    }

    @NotNull
    public Optional<ItemAction> readAction(@NotNull String id, @Nullable Object object) {
        for (var entry : actionTypes.entrySet()) {
            if (entry.getValue().matches(id)) {
                return Optional.of(entry.getValue().build(id, object));
            }
        }
        return Optional.empty();
    }
}
