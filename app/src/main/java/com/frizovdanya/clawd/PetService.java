package com.frizovdanya.clawd;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import com.frizovdanya.clawd.PetEngine;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class PetService extends Service implements PetEngine.Host {
    public static final String ACTION_FROM_ACTIVITY = "com.frizovdanya.clawd.ACTIVITY";
    public static final String CMD_CAPTURE = "capture";
    public static final String CMD_PING = "ping";
    public static final String EXTRA_CAPTURE_RESULT = "capture_result";
    public static final String EXTRA_COMMAND = "cmd";
    private static volatile PetService instance;
    private int captureDensity;
    private int captureH;
    private volatile boolean captureRunning;
    private Thread captureThread;
    private int captureW;
    private Cfg cfg;
    private PetEngine engine;
    private Handler handler;
    private ImageReader imageReader;
    private volatile Bitmap lastFrame;
    private WindowManager.LayoutParams lp;
    private FrameLayout petInput;
    private boolean petInputAttached;
    private WindowManager.LayoutParams petLp;
    private MediaProjection projection;
    private FrameLayout root;
    private BroadcastReceiver screenReceiver;
    private SensorListener sensorListener;
    private SensorManager sensorManager;
    private VirtualDisplay virtualDisplay;
    private WindowManager windowManager;
    private final List<FrameLayout> poopInputs = new ArrayList();
    private final List<WindowManager.LayoutParams> poopLps = new ArrayList();
    private final Rect lastPetRect = new Rect();
    private final List<Rect> lastPoopRects = new ArrayList();
    private final Rect regionRect = new Rect();
    private final Rect poopBuf = new Rect();
    private final ArrayList<Rect> poopRectBuf = new ArrayList<>();

    public static PetService get() {
        return instance;
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public ViewGroup container() {
        return this.root;
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public Context ctx() {
        return this;
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public Handler handler() {
        return this.handler;
    }

    public boolean hasCapture() {
        return (this.projection == null || this.lastFrame == null) ? false : true;
    }

    public boolean isCaptureRunning() {
        return this.projection != null;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        instance = this;
        this.handler = new Handler(Looper.getMainLooper());
        Cfg cfg = new Cfg(this);
        this.cfg = cfg;
        PetData.applyCustom(cfg.s("custom_color", null));
        this.windowManager = (WindowManager) getSystemService("window");
        startAsForeground(false);
        createOverlay();
        this.engine = new PetEngine(this, this.cfg);
        startSensor();
        registerScreenReceiver();
        if (this.cfg.b("show", true)) {
            this.engine.start(true);
        }
        try {
            PowerManager powerManager = (PowerManager) getSystemService("power");
            if (powerManager == null || powerManager.isInteractive()) {
                return;
            }
            this.engine.onPause();
            stopSensor();
        } catch (Exception unused) {
        }
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent == null) {
            return 1;
        }
        String stringExtra = intent.getStringExtra(EXTRA_COMMAND);
        if (CMD_CAPTURE.equals(stringExtra)) {
            Intent intent2 = (Intent) intent.getParcelableExtra(EXTRA_CAPTURE_RESULT);
            if (intent2 == null) {
                return 1;
            }
            startCapture(intent2);
            return 1;
        }
        if ("toggle".equals(stringExtra)) {
            toggleShow();
            return 1;
        }
        if (!CMD_PING.equals(stringExtra)) {
            return 1;
        }
        ping();
        return 1;
    }

    @Override // android.app.Service
    public void onDestroy() {
        FrameLayout frameLayout;
        instance = null;
        PetEngine petEngine = this.engine;
        if (petEngine != null) {
            petEngine.shutdown();
        }
        unregisterScreenReceiver();
        stopSensor();
        stopCapture();
        Iterator<FrameLayout> it = this.poopInputs.iterator();
        while (it.hasNext()) {
            try {
                this.windowManager.removeView(it.next());
            } catch (Exception unused) {
            }
        }
        this.poopInputs.clear();
        this.poopLps.clear();
        if (this.petInputAttached && (frameLayout = this.petInput) != null) {
            try {
                this.windowManager.removeView(frameLayout);
            } catch (Exception unused2) {
            }
            this.petInputAttached = false;
        }
        FrameLayout frameLayout2 = this.root;
        if (frameLayout2 != null) {
            try {
                this.windowManager.removeView(frameLayout2);
            } catch (Exception unused3) {
            }
            this.root = null;
        }
        super.onDestroy();
    }

    private void startAsForeground(boolean z) {
        Notification buildNotification = buildNotification();
        try {
            try {
                if (Build.VERSION.SDK_INT >= 34) {
                    startForeground(1, buildNotification, z ? 1073741856 : 1073741824);
                } else if (Build.VERSION.SDK_INT >= 29) {
                    startForeground(1, buildNotification, 32);
                } else {
                    startForeground(1, buildNotification);
                }
            } catch (Exception unused) {
            }
        } catch (Exception unused2) {
            startForeground(1, buildNotification);
        }
    }

    private Notification buildNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
        NotificationChannel notificationChannel = new NotificationChannel("clawd", getString(R.string.channel_name), 2);
        notificationChannel.setShowBadge(false);
        notificationManager.createNotificationChannel(notificationChannel);
        PendingIntent activity = PendingIntent.getActivity(this, 0, new Intent(this, (Class<?>) MainActivity.class), 201326592);
        PendingIntent service = PendingIntent.getService(this, 1, new Intent(this, (Class<?>) PetService.class).setAction(ACTION_FROM_ACTIVITY).putExtra(EXTRA_COMMAND, "toggle"), 201326592);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notif_small);
        remoteViews.setTextViewText(R.id.notif_title, "clawd");
        int i = R.id.notif_text;
        Cfg cfg = this.cfg;
        remoteViews.setTextViewText(i, (cfg == null || !cfg.b("show", true)) ? "Питомец скрыт" : "Питомец гуляет по экрану");
        remoteViews.setOnClickPendingIntent(R.id.notif_root, activity);
        Notification.Builder builder = new Notification.Builder(this, "clawd");
        builder.setSmallIcon(R.drawable.ic_stat).setCustomContentView(remoteViews).setOngoing(true).setCategory("service").setContentIntent(activity).addAction(new Notification.Action.Builder((Icon) null, "Показать/Скрыть", service).build());
        return builder.build();
    }

    private void refreshNotification() {
        ((NotificationManager) getSystemService(NotificationManager.class)).notify(1, buildNotification());
    }

    private void createOverlay() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2038, 792, -3);
        this.lp = layoutParams;
        layoutParams.gravity = 8388659;
        this.lp.x = 0;
        this.lp.y = 0;
        FrameLayout frameLayout = new FrameLayout(this);
        this.root = frameLayout;
        this.windowManager.addView(frameLayout, this.lp);
        FrameLayout frameLayout2 = new FrameLayout(this);
        this.petInput = frameLayout2;
        frameLayout2.setOnTouchListener(new View.OnTouchListener() { // from class: com.frizovdanya.clawd.PetService$$ExternalSyntheticLambda6
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                boolean lambda$createOverlay$0;
                lambda$createOverlay$0 = PetService.this.lambda$createOverlay$0(view, motionEvent);
                return lambda$createOverlay$0;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$createOverlay$0(View view, MotionEvent motionEvent) {
        PetEngine petEngine = this.engine;
        return petEngine != null && petEngine.dispatchTouch(motionEvent);
    }

    private WindowManager.LayoutParams inputLp(Rect rect) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(Math.max(1, rect.width()), Math.max(1, rect.height()), 2038, 808, -3);
        layoutParams.gravity = 8388659;
        layoutParams.x = rect.left;
        layoutParams.y = rect.top;
        return layoutParams;
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public void syncRegion() {
        PetEngine petEngine;
        if (this.root == null || (petEngine = this.engine) == null) {
            return;
        }
        petEngine.getTouchRects(this.regionRect, this.poopRectBuf);
        boolean z = !this.regionRect.equals(this.lastPetRect);
        boolean z2 = this.poopRectBuf.size() != this.lastPoopRects.size();
        if (!z2) {
            int i = 0;
            while (true) {
                if (i >= this.poopRectBuf.size()) {
                    break;
                }
                if (!this.poopRectBuf.get(i).equals(this.lastPoopRects.get(i))) {
                    z2 = true;
                    break;
                }
                i++;
            }
        }
        if (z || z2) {
            if (!this.regionRect.isEmpty()) {
                try {
                    if (!this.petInputAttached) {
                        WindowManager.LayoutParams inputLp = inputLp(this.regionRect);
                        this.petLp = inputLp;
                        this.windowManager.addView(this.petInput, inputLp);
                        this.petInputAttached = true;
                    } else if (z) {
                        this.petLp.x = this.regionRect.left;
                        this.petLp.y = this.regionRect.top;
                        this.petLp.width = Math.max(1, this.regionRect.width());
                        this.petLp.height = Math.max(1, this.regionRect.height());
                        this.windowManager.updateViewLayout(this.petInput, this.petLp);
                    }
                } catch (Exception e) {
                    Log.e("clawd", "pet input window error", e);
                }
            } else if (this.petInputAttached) {
                try {
                    this.windowManager.removeView(this.petInput);
                } catch (Exception unused) {
                }
                this.petInputAttached = false;
            }
            this.lastPetRect.set(this.regionRect);
            while (this.poopInputs.size() > this.poopRectBuf.size()) {
                List<FrameLayout> list = this.poopInputs;
                FrameLayout remove = list.remove(list.size() - 1);
                List<WindowManager.LayoutParams> list2 = this.poopLps;
                list2.remove(list2.size() - 1);
                try {
                    this.windowManager.removeView(remove);
                } catch (Exception unused2) {
                }
            }
            while (this.poopInputs.size() < this.poopRectBuf.size()) {
                Rect rect = this.poopRectBuf.get(this.poopInputs.size());
                FrameLayout frameLayout = new FrameLayout(this);
                frameLayout.setOnTouchListener(new View.OnTouchListener() { // from class: com.frizovdanya.clawd.PetService$$ExternalSyntheticLambda0
                    @Override // android.view.View.OnTouchListener
                    public final boolean onTouch(View view, MotionEvent motionEvent) {
                        boolean lambda$syncRegion$1;
                        lambda$syncRegion$1 = PetService.this.lambda$syncRegion$1(view, motionEvent);
                        return lambda$syncRegion$1;
                    }
                });
                WindowManager.LayoutParams inputLp2 = inputLp(rect);
                try {
                    this.windowManager.addView(frameLayout, inputLp2);
                    this.poopInputs.add(frameLayout);
                    this.poopLps.add(inputLp2);
                } catch (Exception e2) {
                    Log.e("clawd", "poop input window error", e2);
                }
            }
            if (z2) {
                for (int i2 = 0; i2 < this.poopInputs.size() && i2 < this.poopRectBuf.size(); i2++) {
                    Rect rect2 = this.poopRectBuf.get(i2);
                    WindowManager.LayoutParams layoutParams = this.poopLps.get(i2);
                    layoutParams.x = rect2.left;
                    layoutParams.y = rect2.top;
                    layoutParams.width = Math.max(1, rect2.width());
                    layoutParams.height = Math.max(1, rect2.height());
                    try {
                        this.windowManager.updateViewLayout(this.poopInputs.get(i2), layoutParams);
                    } catch (Exception unused3) {
                    }
                }
            }
            this.lastPoopRects.clear();
            this.lastPoopRects.addAll(this.poopRectBuf);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$syncRegion$1(View view, MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() != 0) {
            return true;
        }
        PetEngine petEngine = this.engine;
        return petEngine != null && petEngine.tapPoop(motionEvent.getRawX(), motionEvent.getRawY());
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public float dp(float f) {
        return Math.max(1.0f, (f * getResources().getDisplayMetrics().density) + 0.5f);
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public int screenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public int screenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    /* JADX WARN: Removed duplicated region for block: B:17:0x0041  */
    @Override // com.frizovdanya.clawd.PetEngine.Host
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public int[] systemInsets() {
        /*
            r4 = this;
            r0 = 0
            android.widget.FrameLayout r1 = r4.root     // Catch: java.lang.Exception -> L3c
            if (r1 == 0) goto L3a
            android.view.WindowInsets r1 = r1.getRootWindowInsets()     // Catch: java.lang.Exception -> L3c
            if (r1 == 0) goto L3a
            android.widget.FrameLayout r1 = r4.root     // Catch: java.lang.Exception -> L3c
            android.view.WindowInsets r1 = r1.getRootWindowInsets()     // Catch: java.lang.Exception -> L3c
            int r2 = android.os.Build.VERSION.SDK_INT     // Catch: java.lang.Exception -> L3c
            r3 = 30
            if (r2 < r3) goto L31
            int r2 = android.view.WindowInsets.Type.systemBars()     // Catch: java.lang.Exception -> L3c
            int r3 = android.view.WindowInsets.Type.statusBars()     // Catch: java.lang.Exception -> L3c
            r2 = r2 | r3
            android.graphics.Insets r2 = r1.getInsets(r2)     // Catch: java.lang.Exception -> L3c
            int r2 = r2.top     // Catch: java.lang.Exception -> L3c
            int r3 = android.view.WindowInsets.Type.systemBars()     // Catch: java.lang.Exception -> L3d
            android.graphics.Insets r1 = r1.getInsets(r3)     // Catch: java.lang.Exception -> L3d
            int r0 = r1.bottom     // Catch: java.lang.Exception -> L3d
            goto L3d
        L31:
            int r2 = r1.getSystemWindowInsetTop()     // Catch: java.lang.Exception -> L3c
            int r0 = r1.getSystemWindowInsetBottom()     // Catch: java.lang.Exception -> L3d
            goto L3d
        L3a:
            r1 = r0
            goto L3f
        L3c:
            r2 = r0
        L3d:
            r1 = r0
            r0 = r2
        L3f:
            if (r0 != 0) goto L4f
            android.content.res.Resources r0 = r4.getResources()
            android.util.DisplayMetrics r0 = r0.getDisplayMetrics()
            float r0 = r0.density
            r2 = 1103101952(0x41c00000, float:24.0)
            float r0 = r0 * r2
            int r0 = (int) r0
        L4f:
            int[] r0 = new int[]{r0, r1}
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.frizovdanya.clawd.PetService.systemInsets():int[]");
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public int displayRotation() {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                return this.root.getDisplay().getRotation();
            }
            return this.windowManager.getDefaultDisplay().getRotation();
        } catch (Exception unused) {
            return 0;
        }
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public List<PetEngine.Platform> scanPlatforms(int i, int i2) {
        ClawdAccessibilityService clawdAccessibilityService = ClawdAccessibilityService.get();
        if (clawdAccessibilityService == null) {
            return new ArrayList();
        }
        return clawdAccessibilityService.scanPlatforms(i, i2);
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public PetEngine.EatTarget pickEatTarget(int i, int i2, int i3) {
        ClawdAccessibilityService clawdAccessibilityService = ClawdAccessibilityService.get();
        if (clawdAccessibilityService == null) {
            return null;
        }
        return clawdAccessibilityService.pickEatTarget(i, i2, i3, screenWidth(), screenHeight());
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public boolean refreshEatTarget(PetEngine.EatTarget eatTarget) {
        ClawdAccessibilityService clawdAccessibilityService = ClawdAccessibilityService.get();
        return clawdAccessibilityService != null && clawdAccessibilityService.refreshEatTarget(eatTarget);
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public Bitmap latestFrame() {
        if (hasCapture()) {
            return this.lastFrame;
        }
        return null;
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public String activePkg() {
        ClawdAccessibilityService clawdAccessibilityService = ClawdAccessibilityService.get();
        if (clawdAccessibilityService != null) {
            return clawdAccessibilityService.activePkg();
        }
        return null;
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public String activeChatTitle() {
        ClawdAccessibilityService clawdAccessibilityService = ClawdAccessibilityService.get();
        if (clawdAccessibilityService != null) {
            return clawdAccessibilityService.activeChatTitle(screenHeight());
        }
        return null;
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public String playingMediaKind() {
        try {
            MediaSessionManager mediaSessionManager = (MediaSessionManager) getSystemService("media_session");
            ClawdNotificationListener clawdNotificationListener = ClawdNotificationListener.get();
            if (mediaSessionManager != null && clawdNotificationListener != null) {
                for (MediaController mediaController : mediaSessionManager.getActiveSessions(new ComponentName(this, (Class<?>) ClawdNotificationListener.class))) {
                    if (mediaController.getPlaybackState() != null && mediaController.getPlaybackState().getState() == 3) {
                        try {
                            return mediaController.getPlaybackInfo().getAudioAttributes().getContentType() == 1 ? "voice" : "music";
                        } catch (Exception unused) {
                            return "music";
                        }
                    }
                }
            }
        } catch (Exception unused2) {
        }
        try {
            AudioManager audioManager = (AudioManager) getSystemService("audio");
            if (audioManager == null) {
                return null;
            }
            if (audioManager.isMusicActive()) {
                return "music";
            }
            return null;
        } catch (Exception unused3) {
            return null;
        }
    }

    @Override // com.frizovdanya.clawd.PetEngine.Host
    public void vibrate(int i) {
        try {
            Vibrator vibrator = (Vibrator) getSystemService("vibrator");
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(i == 0 ? 18L : i == 1 ? 35L : i == 3 ? 14L : 8L, -1));
            }
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startSensor() {
        if (this.sensorListener != null) {
            return;
        }
        try {
            SensorManager sensorManager = (SensorManager) getSystemService("sensor");
            this.sensorManager = sensorManager;
            Sensor defaultSensor = sensorManager.getDefaultSensor(1);
            if (defaultSensor == null) {
                return;
            }
            SensorListener sensorListener = new SensorListener();
            this.sensorListener = sensorListener;
            this.sensorManager.registerListener(sensorListener, defaultSensor, 2);
        } catch (Exception unused) {
        }
    }

    private void registerScreenReceiver() {
        if (this.screenReceiver != null) {
            return;
        }
        this.screenReceiver = new BroadcastReceiver() { // from class: com.frizovdanya.clawd.PetService.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) {
                    return;
                }
                if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                    if (PetService.this.engine != null) {
                        PetService.this.engine.onPause();
                    }
                    PetService.this.stopSensor();
                } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                    PetService.this.startSensor();
                    if (PetService.this.engine != null) {
                        PetService.this.engine.onResume();
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(this.screenReceiver, intentFilter, 4);
        } else {
            registerReceiver(this.screenReceiver, intentFilter);
        }
    }

    private void unregisterScreenReceiver() {
        BroadcastReceiver broadcastReceiver = this.screenReceiver;
        if (broadcastReceiver == null) {
            return;
        }
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception unused) {
        }
        this.screenReceiver = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopSensor() {
        SensorListener sensorListener;
        SensorManager sensorManager = this.sensorManager;
        if (sensorManager != null && (sensorListener = this.sensorListener) != null) {
            try {
                sensorManager.unregisterListener(sensorListener);
            } catch (Exception unused) {
            }
        }
        this.sensorListener = null;
    }

    private class SensorListener implements SensorEventListener {
        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        private SensorListener() {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (PetService.this.engine != null) {
                PetService.this.engine.onAccel(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            }
        }
    }

    private void startCapture(Intent intent) {
        stopCapture();
        try {
            startAsForeground(true);
            MediaProjection mediaProjection = ((MediaProjectionManager) getSystemService("media_projection")).getMediaProjection(-1, intent);
            this.projection = mediaProjection;
            mediaProjection.registerCallback(new MediaProjection.Callback() { // from class: com.frizovdanya.clawd.PetService.2
                @Override // android.media.projection.MediaProjection.Callback
                public void onStop() {
                    PetService.this.stopCapture();
                }
            }, this.handler);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            this.captureW = displayMetrics.widthPixels;
            this.captureH = displayMetrics.heightPixels;
            this.captureDensity = displayMetrics.densityDpi;
            ImageReader newInstance = ImageReader.newInstance(this.captureW, this.captureH, 1, 2);
            this.imageReader = newInstance;
            this.virtualDisplay = this.projection.createVirtualDisplay("clawd_capture", this.captureW, this.captureH, this.captureDensity, 16, newInstance.getSurface(), null, this.handler);
            this.captureRunning = true;
            Thread thread = new Thread(new Runnable() { // from class: com.frizovdanya.clawd.PetService$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    PetService.this.captureLoop();
                }
            }, "clawd-capture");
            this.captureThread = thread;
            thread.start();
        } catch (Exception e) {
            Log.e("clawd", "capture start error", e);
            stopCapture();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void captureLoop() {
        while (this.captureRunning) {
            try {
                Image acquireNextImage = this.imageReader.acquireNextImage();
                if (acquireNextImage == null) {
                    return;
                }
                try {
                    Bitmap imageToBitmap = imageToBitmap(acquireNextImage);
                    if (imageToBitmap != null) {
                        this.lastFrame = imageToBitmap;
                    }
                } catch (Exception unused) {
                } catch (Throwable th) {
                    acquireNextImage.close();
                    throw th;
                }
                acquireNextImage.close();
            } catch (Exception unused2) {
                return;
            }
        }
    }

    private Bitmap imageToBitmap(Image image) {
        Image.Plane plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        int width = image.getWidth();
        int height = image.getHeight();
        int rowStride = plane.getRowStride();
        int pixelStride = plane.getPixelStride();
        Bitmap createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int i = pixelStride * width;
        if (rowStride == i) {
            buffer.rewind();
            createBitmap.copyPixelsFromBuffer(buffer);
            return createBitmap;
        }
        byte[] bArr = new byte[i];
        ByteBuffer allocate = ByteBuffer.allocate(width * height * 4);
        for (int i2 = 0; i2 < height; i2++) {
            buffer.position(i2 * rowStride);
            buffer.get(bArr, 0, Math.min(i, buffer.remaining()));
            allocate.put(bArr, 0, i);
        }
        allocate.rewind();
        createBitmap.copyPixelsFromBuffer(allocate);
        return createBitmap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopCapture() {
        this.captureRunning = false;
        Thread thread = this.captureThread;
        this.captureThread = null;
        if (thread != null) {
            thread.interrupt();
        }
        try {
            ImageReader imageReader = this.imageReader;
            if (imageReader != null) {
                imageReader.close();
            }
        } catch (Exception unused) {
        }
        this.imageReader = null;
        try {
            VirtualDisplay virtualDisplay = this.virtualDisplay;
            if (virtualDisplay != null) {
                virtualDisplay.release();
            }
        } catch (Exception unused2) {
        }
        this.virtualDisplay = null;
        try {
            MediaProjection mediaProjection = this.projection;
            if (mediaProjection != null) {
                mediaProjection.stop();
            }
        } catch (Exception unused3) {
        }
        this.projection = null;
        this.lastFrame = null;
        if (instance == null || Build.VERSION.SDK_INT < 34) {
            return;
        }
        try {
            startAsForeground(false);
        } catch (Exception unused4) {
        }
    }

    public void onIncomingMessage(final String str, final List<String> list) {
        this.handler.post(new Runnable() { // from class: com.frizovdanya.clawd.PetService$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                PetService.this.lambda$onIncomingMessage$2(str, list);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onIncomingMessage$2(String str, List list) {
        PetEngine petEngine = this.engine;
        if (petEngine != null) {
            petEngine.onIncoming(str, list);
        }
    }

    public void onUserSent(final int i) {
        this.handler.post(new Runnable() { // from class: com.frizovdanya.clawd.PetService$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                PetService.this.lambda$onUserSent$3(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onUserSent$3(int i) {
        PetEngine petEngine = this.engine;
        if (petEngine != null) {
            petEngine.onSent(i);
        }
    }

    public void onWindowChanged() {
        this.handler.post(new Runnable() { // from class: com.frizovdanya.clawd.PetService$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                PetService.this.lambda$onWindowChanged$4();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onWindowChanged$4() {
        PetEngine petEngine = this.engine;
        if (petEngine != null) {
            petEngine.invalidatePlatforms();
        }
    }

    public void toggleShow() {
        final boolean z = !this.cfg.b("show", true);
        this.cfg.putBool("show", z);
        if (this.engine != null) {
            this.handler.post(new Runnable() { // from class: com.frizovdanya.clawd.PetService$$ExternalSyntheticLambda7
                @Override // java.lang.Runnable
                public final void run() {
                    PetService.this.lambda$toggleShow$5(z);
                }
            });
        }
        refreshNotification();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$toggleShow$5(boolean z) {
        this.engine.onCfgChange("show", Boolean.valueOf(z));
    }

    public void ping() {
        refreshNotification();
        if (this.engine == null || !this.cfg.b("show", true)) {
            return;
        }
        this.handler.post(new Runnable() { // from class: com.frizovdanya.clawd.PetService$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() {
                PetService.this.lambda$ping$6();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$ping$6() {
        this.engine.start(false);
    }

    public String petState() {
        return (this.engine == null || !this.cfg.b("show", true)) ? "скрыт" : this.engine.stateLabel();
    }

    public void cfgChanged(final String str, final Object obj) {
        refreshNotification();
        this.handler.post(new Runnable() { // from class: com.frizovdanya.clawd.PetService$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                PetService.this.lambda$cfgChanged$7(str, obj);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$cfgChanged$7(String str, Object obj) {
        PetEngine petEngine = this.engine;
        if (petEngine != null) {
            petEngine.onCfgChange(str, obj);
        }
    }

    public boolean captureActive() {
        return isCaptureRunning();
    }
}
