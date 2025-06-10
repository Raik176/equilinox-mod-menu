package de.rhm176.modmenu.util;

import de.rhm176.modmenu.ModMenu;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class LogUtil {
    public static void log(Object message) {
        System.out.printf("[%s] %s%n", ModMenu.MOD_MENU_CONTAINER.getMetadata().getName(), message);
    }

    public static void err(Object message) {
        System.err.printf("[%s] %s%n", ModMenu.MOD_MENU_CONTAINER.getMetadata().getName(), message);
    }

    public static void err(Object message, Throwable throwable) {
        System.err.printf("[%s] %s%n", ModMenu.MOD_MENU_CONTAINER.getMetadata().getName(), message);
        throwable.printStackTrace(System.err);
    }
}
