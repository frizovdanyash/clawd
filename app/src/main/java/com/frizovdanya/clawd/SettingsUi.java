package com.frizovdanya.clawd;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/* loaded from: classes.dex */
public final class SettingsUi {
    private SettingsUi() {
    }

    public static boolean overlayGranted(Context context) {
        return Settings.canDrawOverlays(context);
    }

    public static void askOverlay(Context context) {
        Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(268435456);
        context.startActivity(intent);
    }

    public static void openAccessibility(Context context) {
        try {
            context.startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS").addFlags(268435456));
        } catch (Exception unused) {
        }
    }

    public static void openNotificationAccess(Context context) {
        try {
            context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(268435456));
        } catch (Exception unused) {
        }
    }

    public static boolean isAccessibilityEnabled(Context context) {
        String string = Settings.Secure.getString(context.getContentResolver(), "enabled_accessibility_services");
        if (string == null) {
            return false;
        }
        String str = context.getPackageName() + "/" + ClawdAccessibilityService.class.getName();
        String str2 = context.getPackageName() + "/.ClawdAccessibilityService";
        for (String str3 : string.split(":")) {
            if (str3.equalsIgnoreCase(str) || str3.equalsIgnoreCase(str2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotificationAccessEnabled(Context context) {
        String string = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (string == null) {
            return false;
        }
        String str = context.getPackageName() + "/" + ClawdNotificationListener.class.getName();
        String str2 = context.getPackageName() + "/.ClawdNotificationListener";
        for (String str3 : string.split(":")) {
            if (str3.equalsIgnoreCase(str) || str3.equalsIgnoreCase(str2)) {
                return true;
            }
        }
        return false;
    }
}
