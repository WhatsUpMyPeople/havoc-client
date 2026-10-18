/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.gui.hud;

import havoc.client.features.gui.hud.draggable.DraggableHudElement;
import havoc.client.features.gui.hud.draggable.DraggableHudElementRegistry;
import havoc.client.features.gui.hud.nondraggable.StaticHudElementRegistry;
import havoc.client.runtime.discovery.HavocIndex;
import havoc.client.util.logging.DebugLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public enum HudElementAutoLoader {
    ;

    public static void load(String basePackage) {
        load(basePackage, true, true);
    }

    public static void loadDraggable(String basePackage) {
        load(basePackage, true, false);
    }

    public static void loadStatic(String basePackage) {
        load(basePackage, false, true);
    }

    private static void load(String basePackage, boolean includeDraggable, boolean includeStatic) {
        List<RegisteredHudElement> elements = new ArrayList<>();

        for (String className : HavocIndex.classNames(HavocIndex.Kind.HUD, basePackage)) {
            addCandidate(elements, className);
        }

        elements.sort(Comparator
                .comparingInt(RegisteredHudElement::order)
                .thenComparing(element -> element.type().getName()));

        for (RegisteredHudElement element : elements) {
            register(element.type(), includeDraggable, includeStatic);
        }
    }

    private static void register(Class<?> type, boolean includeDraggable, boolean includeStatic) {
        try {
            Object instance = instanceFor(type);

            if (instance instanceof DraggableHudElement draggable) {
                if (!includeDraggable) return;
                DraggableHudElementRegistry.register(draggable);
                return;
            }

            if (instance instanceof AbstractHudElement hudElement) {
                if (!includeStatic) return;
                StaticHudElementRegistry.register(hudElement);
                return;
            }

            DebugLog.config("Skipping @HudElementRegister non-HUD element: %s", type.getName());
        } catch (Throwable t) {
            DebugLog.error("Failed to load HUD element: %s", t, type.getName());
        }
    }

    private static void addCandidate(List<RegisteredHudElement> elements, String className) {
        try {
            Class<?> type = Class.forName(className, false, HudElementAutoLoader.class.getClassLoader());
            if (Modifier.isAbstract(type.getModifiers())) return;
            int order = 0;
            HudElementInfo elementInfo = type.getAnnotation(HudElementInfo.class);
            if (elementInfo != null) {
                order = elementInfo.order();
            } else {
                HudElementRegister register = type.getAnnotation(HudElementRegister.class);
                if (register == null) return;
                order = register.order();
            }

            elements.add(new RegisteredHudElement(type, order));
        } catch (Throwable t) {
            DebugLog.error("Failed to inspect HUD element: %s", t, className);
        }
    }

    private static Object instanceFor(Class<?> type) throws ReflectiveOperationException {
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (!type.isAssignableFrom(field.getType())) continue;

            field.setAccessible(true);
            Object instance = field.get(null);
            if (instance != null) return instance;
        }

        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private record RegisteredHudElement(Class<?> type, int order) {
    }
}
