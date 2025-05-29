package com.saicone.picospacos.api.item;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@FunctionalInterface
public interface ItemPredicate extends Predicate<ItemHolder> {

    @Override
    boolean test(@NotNull ItemHolder holder);

    
}
