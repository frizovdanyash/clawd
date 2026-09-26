package com.frizovdanya.clawd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* loaded from: classes.dex */
public class BootReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (("android.intent.action.BOOT_COMPLETED".equals(action) || "android.intent.action.LOCKED_BOOT_COMPLETED".equals(action) || "android.intent.action.MY_PACKAGE_REPLACED".equals(action)) && new Cfg(context).b("show", true) && SettingsUi.overlayGranted(context)) {
            try {
                context.startForegroundService(new Intent(context, (Class<?>) PetService.class));
            } catch (Exception unused) {
            }
        }
    }
}
