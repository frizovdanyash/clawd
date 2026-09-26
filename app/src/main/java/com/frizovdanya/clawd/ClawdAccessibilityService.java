package com.frizovdanya.clawd;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class ClawdAccessibilityService extends AccessibilityService {
    private static volatile ClawdAccessibilityService instance;
    private int lastEditLen = 0;

    public static ClawdAccessibilityService get() {
        return instance;
    }

    @Override // android.accessibilityservice.AccessibilityService
    public void onInterrupt() {
    }

    @Override // android.accessibilityservice.AccessibilityService
    protected void onServiceConnected() {
        instance = this;
    }

    @Override // android.app.Service
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    @Override // android.accessibilityservice.AccessibilityService
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        PetService petService;
        try {
            int eventType = accessibilityEvent.getEventType();
            if (eventType != 32 && eventType != 2048) {
                CharSequence charSequence = null;
                if (eventType == 16) {
                    if (accessibilityEvent.getText() != null && !accessibilityEvent.getText().isEmpty()) {
                        charSequence = accessibilityEvent.getText().get(0);
                    }
                    int length = charSequence != null ? charSequence.length() : 0;
                    if (this.lastEditLen > 0 && length == 0 && (petService = PetService.get()) != null) {
                        petService.onUserSent(this.lastEditLen);
                    }
                    this.lastEditLen = length;
                    return;
                }
                if (eventType == 1) {
                    if (accessibilityEvent.getText() != null && !accessibilityEvent.getText().isEmpty()) {
                        charSequence = accessibilityEvent.getText().get(0);
                    }
                    CharSequence contentDescription = accessibilityEvent.getContentDescription();
                    StringBuilder sb = new StringBuilder();
                    if (charSequence == null) {
                        charSequence = "";
                    }
                    StringBuilder append = sb.append((Object) charSequence).append(" ");
                    if (contentDescription == null) {
                        contentDescription = "";
                    }
                    if (append.append((Object) contentDescription).toString().trim().toLowerCase().matches(".*(send|отправить|отправл.*|➤|✈|paper.?plane).*")) {
                        PetService petService2 = PetService.get();
                        if (petService2 != null) {
                            petService2.onUserSent(Math.max(this.lastEditLen, 1));
                        }
                        this.lastEditLen = 0;
                        return;
                    }
                    return;
                }
                return;
            }
            PetService petService3 = PetService.get();
            if (petService3 != null) {
                petService3.onWindowChanged();
            }
        } catch (Exception unused) {
        }
    }

    private AccessibilityNodeInfo root() {
        AccessibilityNodeInfo root;
        AccessibilityNodeInfo root2;
        try {
            AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
            if (rootInActiveWindow != null) {
                return rootInActiveWindow;
            }
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows == null) {
                return null;
            }
            for (AccessibilityWindowInfo accessibilityWindowInfo : windows) {
                if (accessibilityWindowInfo.isActive() && accessibilityWindowInfo.isFocused() && (root2 = accessibilityWindowInfo.getRoot()) != null) {
                    return root2;
                }
            }
            for (AccessibilityWindowInfo accessibilityWindowInfo2 : windows) {
                if (accessibilityWindowInfo2.isActive() && (root = accessibilityWindowInfo2.getRoot()) != null) {
                    return root;
                }
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    public String activePkg() {
        AccessibilityNodeInfo root = root();
        if (root == null) {
            return null;
        }
        String obj = root.getPackageName() != null ? root.getPackageName().toString() : null;
        root.recycle();
        return obj;
    }

    public String activeChatTitle(int i) {
        AccessibilityNodeInfo root = root();
        String str = null;
        if (root == null) {
            return null;
        }
        int i2 = (int) (i * 0.2f);
        try {
            ArrayList arrayList = new ArrayList();
            arrayList.add(root);
            int i3 = Integer.MAX_VALUE;
            int i4 = 120;
            while (!arrayList.isEmpty()) {
                int i5 = i4 - 1;
                if (i4 <= 0) {
                    break;
                }
                AccessibilityNodeInfo accessibilityNodeInfo = (AccessibilityNodeInfo) arrayList.remove(arrayList.size() - 1);
                if (accessibilityNodeInfo.isVisibleToUser()) {
                    CharSequence text = accessibilityNodeInfo.getText();
                    if (text != null && text.length() > 0 && text.length() <= 40) {
                        Rect rect = new Rect();
                        accessibilityNodeInfo.getBoundsInScreen(rect);
                        String cls = cls(accessibilityNodeInfo);
                        if ((cls.contains("TextView") || cls.contains("Text") || cls.contains("Title") || cls.contains("ActionBar")) && rect.top >= 0 && rect.top < i2 && rect.top < i3) {
                            i3 = rect.top;
                            str = text.toString().trim();
                        }
                    }
                    for (int i6 = 0; i6 < accessibilityNodeInfo.getChildCount(); i6++) {
                        AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i6);
                        if (child != null) {
                            arrayList.add(child);
                        }
                    }
                }
                accessibilityNodeInfo.recycle();
                i4 = i5;
            }
        } catch (Exception unused) {
        }
        return str;
    }

    private static String cls(AccessibilityNodeInfo accessibilityNodeInfo) {
        CharSequence className = accessibilityNodeInfo.getClassName();
        return className != null ? className.toString() : "";
    }

    private String[] findText(AccessibilityNodeInfo accessibilityNodeInfo, int i, int i2) {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        arrayList.add(accessibilityNodeInfo);
        arrayList2.add(0);
        while (true) {
            if (arrayList.isEmpty()) {
                break;
            }
            int i3 = i2 - 1;
            if (i2 <= 0) {
                break;
            }
            AccessibilityNodeInfo accessibilityNodeInfo2 = (AccessibilityNodeInfo) arrayList.remove(arrayList.size() - 1);
            int intValue = ((Integer) arrayList2.remove(arrayList2.size() - 1)).intValue();
            CharSequence text = accessibilityNodeInfo2.getText();
            if (text != null && text.length() > 0) {
                String trim = text.toString().trim();
                String viewIdResourceName = accessibilityNodeInfo2.getViewIdResourceName();
                if (accessibilityNodeInfo2 != accessibilityNodeInfo) {
                    accessibilityNodeInfo2.recycle();
                }
                String[] strArr = new String[2];
                strArr[0] = trim;
                strArr[1] = viewIdResourceName != null ? viewIdResourceName : "";
                return strArr;
            }
            if (intValue < i) {
                for (int i4 = 0; i4 < accessibilityNodeInfo2.getChildCount() && i4 < 30; i4++) {
                    AccessibilityNodeInfo child = accessibilityNodeInfo2.getChild(i4);
                    if (child != null) {
                        arrayList.add(child);
                        arrayList2.add(Integer.valueOf(intValue + 1));
                    }
                }
            }
            if (accessibilityNodeInfo2 != accessibilityNodeInfo) {
                accessibilityNodeInfo2.recycle();
            }
            i2 = i3;
        }
        return new String[]{"", ""};
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:49:0x0185 A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:52:0x018d A[Catch: Exception -> 0x01f7, TryCatch #0 {Exception -> 0x01f7, blocks: (B:40:0x0102, B:42:0x010e, B:44:0x011f, B:45:0x0164, B:46:0x0137, B:47:0x017e, B:50:0x0187, B:52:0x018d, B:54:0x01a1, B:60:0x01ad, B:76:0x01b8, B:77:0x01c3, B:79:0x01c9, B:82:0x01d7), top: B:39:0x0102 }] */
    /* JADX WARN: Removed duplicated region for block: B:55:0x019e  */
    /* JADX WARN: Removed duplicated region for block: B:69:0x00b4 A[Catch: Exception -> 0x01f5, TryCatch #1 {Exception -> 0x01f5, blocks: (B:6:0x000e, B:7:0x0026, B:9:0x002c, B:11:0x0030, B:13:0x004d, B:15:0x005f, B:17:0x0067, B:19:0x006f, B:21:0x0077, B:25:0x0083, B:28:0x008d, B:30:0x0095, B:34:0x00f1, B:36:0x00f7, B:65:0x009d, B:67:0x00a3, B:69:0x00b4, B:70:0x00d2, B:71:0x00b9), top: B:5:0x000e }] */
    /* JADX WARN: Removed duplicated region for block: B:71:0x00b9 A[Catch: Exception -> 0x01f5, TryCatch #1 {Exception -> 0x01f5, blocks: (B:6:0x000e, B:7:0x0026, B:9:0x002c, B:11:0x0030, B:13:0x004d, B:15:0x005f, B:17:0x0067, B:19:0x006f, B:21:0x0077, B:25:0x0083, B:28:0x008d, B:30:0x0095, B:34:0x00f1, B:36:0x00f7, B:65:0x009d, B:67:0x00a3, B:69:0x00b4, B:70:0x00d2, B:71:0x00b9), top: B:5:0x000e }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.util.List<com.frizovdanya.clawd.PetEngine.Platform> scanPlatforms(int r17, int r18) {
        /*
            Method dump skipped, instructions count: 504
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.frizovdanya.clawd.ClawdAccessibilityService.scanPlatforms(int, int):java.util.List");
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:44:0x00da A[Catch: Exception -> 0x02a5, TryCatch #0 {Exception -> 0x02a5, blocks: (B:16:0x004e, B:17:0x0064, B:19:0x006b, B:21:0x006f, B:23:0x008e, B:26:0x0099, B:28:0x009f, B:30:0x00a5, B:32:0x00b1, B:34:0x00bd, B:42:0x00d4, B:44:0x00da, B:46:0x00ee, B:50:0x00f4, B:54:0x00ff, B:56:0x010f, B:58:0x0118, B:60:0x0121, B:62:0x012a, B:64:0x0133, B:66:0x013b, B:68:0x0143, B:70:0x014b, B:72:0x0153, B:74:0x0159, B:76:0x015d, B:78:0x0163, B:80:0x0167, B:83:0x01a2, B:85:0x016e, B:90:0x01ac, B:93:0x01b6, B:95:0x01bc, B:96:0x01c9, B:97:0x01d2, B:99:0x01d8, B:101:0x0229, B:103:0x0231, B:108:0x023f, B:110:0x025b, B:111:0x0266, B:113:0x026c, B:115:0x0272, B:116:0x027d, B:117:0x028b, B:119:0x0291, B:122:0x029b, B:127:0x02a1, B:105:0x023b), top: B:15:0x004e }] */
    /* JADX WARN: Removed duplicated region for block: B:47:0x00ec  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public com.frizovdanya.clawd.PetEngine.EatTarget pickEatTarget(int r21, int r22, int r23, int r24, int r25) {
        /*
            Method dump skipped, instructions count: 711
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.frizovdanya.clawd.ClawdAccessibilityService.pickEatTarget(int, int, int, int, int):com.frizovdanya.clawd.PetEngine$EatTarget");
    }

    /* JADX WARN: Code restructure failed: missing block: B:46:0x00bd, code lost:
    
        if (r11.length() != 0) goto L51;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean refreshEatTarget(com.frizovdanya.clawd.PetEngine.EatTarget r15) {
        /*
            Method dump skipped, instructions count: 304
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.frizovdanya.clawd.ClawdAccessibilityService.refreshEatTarget(com.frizovdanya.clawd.PetEngine$EatTarget):boolean");
    }
}
