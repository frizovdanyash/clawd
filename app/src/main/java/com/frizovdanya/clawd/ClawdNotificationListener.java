package com.frizovdanya.clawd;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class ClawdNotificationListener extends NotificationListenerService {
    private static volatile ClawdNotificationListener instance;

    public static ClawdNotificationListener get() {
        return instance;
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
    }

    @Override // android.app.Service
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    @Override // android.service.notification.NotificationListenerService, android.app.Service
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        String packageName;
        Bundle bundle;
        CharSequence[] charSequenceArray;
        if (statusBarNotification != null) {
            try {
                if (statusBarNotification.getNotification() == null || (packageName = statusBarNotification.getPackageName()) == null || packageName.equals(getPackageName()) || (statusBarNotification.getNotification().flags & 2) != 0 || packageName.startsWith("com.android.") || packageName.equals("android") || (bundle = statusBarNotification.getNotification().extras) == null) {
                    return;
                }
                CharSequence charSequence = bundle.getCharSequence("android.title");
                CharSequence charSequence2 = bundle.getCharSequence("android.text");
                if (charSequence2 == null) {
                    charSequence2 = bundle.getCharSequence("android.bigText");
                }
                if (charSequence2 == null && (charSequenceArray = bundle.getCharSequenceArray("android.textLines")) != null && charSequenceArray.length > 0) {
                    charSequence2 = charSequenceArray[0];
                }
                if (charSequence2 != null && charSequence2.length() != 0) {
                    ArrayList arrayList = new ArrayList();
                    if (charSequence != null && charSequence.length() > 0) {
                        arrayList.add(charSequence.toString().toLowerCase());
                    }
                    arrayList.add(charSequence2.toString().toLowerCase());
                    PetService petService = PetService.get();
                    if (petService != null) {
                        petService.onIncomingMessage(packageName, arrayList);
                    }
                }
            } catch (Exception unused) {
            }
        }
    }
}
