package com.frizovdanya.clawd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

/* loaded from: classes.dex */
public class PetEngine {
    private static final String[] KW_HOOD = {"love", "laugh", "hi"};
    private static final Pattern[] KW_PAT = {Pattern.compile("❤|♥|💕|💖|😘|😍|\u1f970|\\bлюблю\\b|\\bобнима\\w*|\\bцелую\\b|\\blove\\b"), Pattern.compile("(?:ха){2,}|(?:хе){2,}|(?:хи){2,}|\\bлол\\b|\\blol\\b|\\bржу\\w*|😂|🤣"), Pattern.compile("\\b(?:привет\\w*|здравствуй\\w*|хай|салют|hello|hi|hey)\\b", 2)};
    private float[] accelPrev;
    private double attachCheck;
    private double blinkUntil;
    private Rect boundsCache;
    private int boundsH;
    private double boundsTime;
    private int boundsW;
    private TextView bubble;
    private boolean bubbleFadeIn;
    private int bubbleGen;
    private double bubbleUntil;
    private boolean bubbleVisible;
    private final Cfg cfg;
    private double chewUntil;
    private ViewGroup container;
    private String expr;
    private double exprUntil;
    private String frameKey;
    private boolean greetPending;
    private double hangStart;
    private double hangUntil;
    private boolean hopActive;
    private float hopOff;
    private float hopVy;
    private final Host host;
    private int insetBottom;
    private int insetTop;
    private double lastInteraction;
    private String lastPhrase;
    private double lastRegionSync;
    private double lastTap;
    private double lastTickT;
    private int look;
    private String media;
    private double mediaCheck;
    private double mouthOpenUntil;
    private double nextAction;
    private double nextBlink;
    private double nextChatComment;
    private double nextEat;
    private double nextHeart;
    private double nextMsgReact;
    private double nextNote;
    private double nextPeek;
    private double nextSentReact;
    private double nextSlideSay;
    private double nextZ;
    private boolean nightMode;
    private double peekEnd;
    private boolean peekMoving;
    private int peekPending;
    private double peekPhaseUntil;
    private int peekSide;
    private ImageView pet;
    private Platform platform;
    private boolean platformMoving;
    private double platformScan;
    private float[] platformSpan;
    private double poopDue;
    private int poopNeed;
    private int rotation;
    private boolean shakeDizzy;
    private float shakePending;
    private float slideV;
    private double squashUntil;
    private float tilt;
    private Touch touch;
    private float vx;
    private float vy;
    private boolean walkIn;
    private float walkTarget;
    private double waveUntil;
    private float x;
    private float y;
    private final Random rnd = new Random();
    private final SpriteRenderer sprites = new SpriteRenderer();
    private boolean cShow = true;
    private int cSize = 1;
    private int cColor = 0;
    private boolean cGravity = false;
    private boolean cWander = true;
    private boolean cPhrases = true;
    private int cSleep = 1;
    private boolean cHaptics = true;
    private boolean cEat = true;
    private boolean cTgEvents = true;
    private boolean cDance = true;
    private boolean cPoop = true;
    private boolean cShake = true;
    private int cAlpha = 100;
    private int cAct = 1;
    private boolean running = false;
    private boolean paused = false;
    private boolean unloaded = false;
    private final List<ImageView> particles = new ArrayList();
    private final List<ImageView> poops = new ArrayList();
    private final List<ImageView> flying = new ArrayList();
    private final List<EatenItem> eaten = new ArrayList();
    private final List<EatenItem> spitting = new ArrayList();
    private int unit = 1;
    private int w = 1;
    private int h = 1;
    private String state = "idle";
    private boolean posReady = false;
    private final List<Double> recentTaps = new ArrayList();
    private String chatDialog = "";
    private final Map<String, Integer> chatVisits = new HashMap();
    private final List<Double> shakeHits = new ArrayList();
    private List<Platform> platforms = new ArrayList();
    private String peekPhase = "hide";
    private final Runnable tickR = new Runnable() { // from class: com.frizovdanya.clawd.PetEngine$$ExternalSyntheticLambda5
        @Override // java.lang.Runnable
        public final void run() {
            PetEngine.this.lambda$new$1();
        }
    };

    public interface Host {
        String activeChatTitle();

        String activePkg();

        ViewGroup container();

        Context ctx();

        int displayRotation();

        float dp(float f);

        Handler handler();

        Bitmap latestFrame();

        EatTarget pickEatTarget(int i, int i2, int i3);

        String playingMediaKind();

        boolean refreshEatTarget(EatTarget eatTarget);

        List<Platform> scanPlatforms(int i, int i2);

        int screenHeight();

        int screenWidth();

        void syncRegion();

        int[] systemInsets();

        void vibrate(int i);
    }

    private float actFactor() {
        int i = this.cAct;
        if (i == 0) {
            return 1.7f;
        }
        return i == 2 ? 0.65f : 1.0f;
    }

    private static float clamp(float f, float f2, float f3) {
        if (f3 >= f2 && f >= f2) {
            return f > f3 ? f3 : f;
        }
        return f2;
    }

    private static int clampInt(int i, int i2, int i3) {
        if (i3 >= i2 && i >= i2) {
            return i > i3 ? i3 : i;
        }
        return i2;
    }

    private float mouthX() {
        return this.x + (this.w / 2.0f);
    }

    private float mouthY() {
        return this.y + this.hopOff + (this.unit * 8.0f);
    }

    private float peekX(int i, int i2) {
        return this.peekSide > 0 ? i - (i2 * this.unit) : (i2 * this.unit) - this.w;
    }

    private int screenW() {
        return this.boundsW;
    }

    public void invalidatePlatforms() {
        this.platformScan = 0.0d;
    }

    public boolean isShown() {
        return this.cShow;
    }

    public static class Platform {
        public String key;
        public final String kind;
        public Rect rect;
        public final String text;

        public Platform(String str, String str2, String str3, Rect rect) {
            this.kind = str;
            this.key = str2;
            this.text = str3;
            this.rect = rect;
        }
    }

    public static class EatTarget {
        public final String className;
        public final String pkg;
        public Rect rect;
        public final String text;
        public final String viewId;

        public EatTarget(String str, String str2, String str3, String str4, Rect rect) {
            this.pkg = str;
            this.viewId = str2;
            this.className = str3;
            this.text = str4;
            this.rect = new Rect(rect);
        }
    }

    private void resetState() {
        double now = now();
        this.state = "idle";
        this.x = 0.0f;
        this.y = 0.0f;
        this.vx = 0.0f;
        this.vy = 0.0f;
        this.posReady = false;
        this.lastTickT = now;
        this.lastInteraction = now;
        this.nextAction = 4.0d + now;
        this.nextBlink = 2.0d + now;
        this.blinkUntil = 0.0d;
        this.look = 0;
        this.expr = null;
        this.exprUntil = 0.0d;
        this.squashUntil = 0.0d;
        this.waveUntil = 0.0d;
        this.hopActive = false;
        this.hopOff = 0.0f;
        this.hopVy = 0.0f;
        this.walkTarget = 0.0f;
        this.touch = null;
        this.lastTap = 0.0d;
        this.recentTaps.clear();
        this.nextHeart = 0.0d;
        this.nextZ = 0.0d;
        this.bubbleVisible = false;
        this.bubbleFadeIn = false;
        this.bubbleUntil = 0.0d;
        this.bubbleGen = 0;
        this.lastPhrase = null;
        this.boundsCache = null;
        this.boundsTime = 0.0d;
        this.attachCheck = 0.0d;
        this.greetPending = false;
        this.nextEat = 25.0d + now;
        this.mouthOpenUntil = 0.0d;
        this.chewUntil = 0.0d;
        this.media = null;
        this.mediaCheck = 0.0d;
        this.nextNote = 0.0d;
        this.nextMsgReact = 0.0d;
        this.nextSentReact = 0.0d;
        this.chatDialog = "";
        this.chatVisits.clear();
        this.nextChatComment = 8.0d + now;
        this.rotation = 0;
        this.accelPrev = null;
        this.shakeHits.clear();
        this.shakePending = 0.0f;
        this.shakeDizzy = false;
        this.tilt = 0.0f;
        this.slideV = 0.0f;
        this.nextSlideSay = 0.0d;
        this.platform = null;
        this.platforms = new ArrayList();
        this.platformScan = 0.0d;
        this.platformMoving = false;
        this.walkIn = false;
        this.hangStart = 0.0d;
        this.hangUntil = 0.0d;
        this.peekSide = 0;
        this.peekPending = 0;
        this.peekPhase = "hide";
        this.peekPhaseUntil = 0.0d;
        this.peekEnd = 0.0d;
        this.peekMoving = false;
        this.poopDue = 0.0d;
        this.poopNeed = this.rnd.nextInt(3) + 3;
        this.platformSpan = null;
        this.nextPeek = now + 45.0d;
        this.lastRegionSync = 0.0d;
    }

    private static class Touch {
        float anchor;
        int dir;
        final List<double[]> hist;
        float ox;
        float oy;
        float t0;
        float vx;
        float x0;
        float y0;

        private Touch() {
            this.hist = new ArrayList();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    static class EatenItem {
        Bitmap bmp;
        View copy;
        int hh;
        View patch;
        boolean spitting;
        EatTarget target;
        int w;

        private EatenItem() {
        }
    }

    private static double now() {
        return SystemClock.uptimeMillis() / 1000.0d;
    }

    public PetEngine(Host host, Cfg cfg) {
        this.nightMode = false;
        this.host = host;
        this.cfg = cfg;
        reloadCfg();
        resetState();
        this.nightMode = (host.ctx().getResources().getConfiguration().uiMode & 48) == 32;
    }

    private void reloadCfg() {
        this.cShow = this.cfg.b("show", true);
        this.cSize = this.cfg.i("size", 3);
        this.cColor = this.cfg.i("color", 0);
        this.cGravity = this.cfg.b("gravity", false);
        this.cWander = this.cfg.b("wander", true);
        this.cPhrases = this.cfg.b("phrases", true);
        this.cSleep = this.cfg.i("sleep", 1);
        this.cHaptics = this.cfg.b("haptics", true);
        this.cEat = this.cfg.b("eat", true);
        this.cTgEvents = this.cfg.b("tg_events", true);
        this.cDance = this.cfg.b("dance", true);
        this.cPoop = this.cfg.b("poop", true);
        this.cShake = this.cfg.b("shake", true);
        this.cAlpha = clampInt(this.cfg.i("alpha", 100), 40, 100);
        this.cAct = clampInt(this.cfg.i("act", 1), 0, 2);
    }

    public void onCfgChange(String str, Object obj) {
        str.hashCode();
        switch (str) {
            case "recenter":
                recenter();
                break;
            case "act":
                this.cAct = clampInt(((Integer) obj).intValue(), 0, 2);
                break;
            case "eat":
                boolean booleanValue = ((Boolean) obj).booleanValue();
                this.cEat = booleanValue;
                if (!booleanValue) {
                    giveBack();
                    break;
                }
                break;
            case "poop":
                boolean booleanValue2 = ((Boolean) obj).booleanValue();
                this.cPoop = booleanValue2;
                if (!booleanValue2) {
                    removePoops();
                    break;
                }
                break;
            case "show":
                boolean booleanValue3 = ((Boolean) obj).booleanValue();
                this.cShow = booleanValue3;
                if (booleanValue3) {
                    start(true);
                    break;
                } else {
                    applyShow(false);
                    break;
                }
            case "size":
                this.cSize = ((Integer) obj).intValue();
                rebuildSprites();
                break;
            case "alpha":
                int clampInt = clampInt(((Integer) obj).intValue(), 40, 100);
                this.cAlpha = clampInt;
                ImageView imageView = this.pet;
                if (imageView != null) {
                    imageView.setAlpha(clampInt / 100.0f);
                    break;
                }
                break;
            case "color":
                this.cColor = ((Integer) obj).intValue();
                rebuildSprites();
                break;
            case "sleep":
                this.cSleep = ((Integer) obj).intValue();
                if ("sleep".equals(this.state)) {
                    this.lastInteraction = now();
                    break;
                }
                break;
            case "gravity":
                boolean booleanValue4 = ((Boolean) obj).booleanValue();
                this.cGravity = booleanValue4;
                if (!booleanValue4) {
                    this.platform = null;
                    this.slideV = 0.0f;
                    break;
                }
                break;
            case "custom_color":
                if (obj != null) {
                    PetData.applyCustom(String.valueOf(obj));
                }
                this.cColor = 6;
                rebuildSprites();
                break;
            case "give_back":
                removePoops();
                break;
            case "toggle_sleep":
                toggleSleep();
                break;
            default:
                reloadCfg();
                break;
        }
    }

    private void applyShow(boolean z) {
        if (z) {
            start(true);
            return;
        }
        this.running = false;
        this.host.handler().removeCallbacks(this.tickR);
        detachViews();
    }

    public void start(final boolean z) {
        if (this.unloaded || this.paused || !this.cShow) {
            return;
        }
        if (z) {
            this.greetPending = true;
        }
        ensureAttached();
        if (this.pet == null) {
            this.host.handler().postDelayed(new Runnable() { // from class: com.frizovdanya.clawd.PetEngine$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    PetEngine.this.lambda$start$0(z);
                }
            }, 700L);
            return;
        }
        this.running = true;
        this.lastTickT = now();
        schedule(16L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$start$0(boolean z) {
        if (this.running || this.cShow) {
            start(z);
        }
    }

    public void shutdown() {
        this.unloaded = true;
        this.running = false;
        this.host.handler().removeCallbacks(this.tickR);
        detachViews();
    }

    public void onPause() {
        this.paused = true;
        this.host.handler().removeCallbacks(this.tickR);
    }

    public void onResume() {
        this.paused = false;
        if (this.cShow) {
            start(false);
        }
    }

    private void ensureAttached() {
        ViewGroup container = this.host.container();
        if (container == null) {
            return;
        }
        ImageView imageView = this.pet;
        if (imageView != null && this.container == container && imageView.getParent() == container) {
            return;
        }
        detachViews();
        this.container = container;
        buildViews();
    }

    private void buildViews() {
        updateMetrics();
        this.sprites.configure(this.unit, PetData.PALETTE_BODY[cColorClamp()], PetData.PALETTE_EYE[cColorClamp()]);
        Context ctx = this.host.ctx();
        ImageView imageView = new ImageView(ctx);
        this.pet = imageView;
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.pet.setAlpha(this.cAlpha / 100.0f);
        this.pet.setClickable(true);
        this.pet.setOnTouchListener(new View.OnTouchListener() { // from class: com.frizovdanya.clawd.PetEngine$$ExternalSyntheticLambda4
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                boolean onTouch;
                onTouch = PetEngine.this.onTouch(view, motionEvent);
                return onTouch;
            }
        });
        this.container.addView(this.pet, new FrameLayout.LayoutParams(this.w, this.h));
        TextView textView = new TextView(ctx);
        this.bubble = textView;
        textView.setTextSize(1, 13.0f);
        this.bubble.setMaxWidth((int) this.host.dp(220.0f));
        int dp = (int) this.host.dp(10.0f);
        int dp2 = (int) this.host.dp(6.0f);
        this.bubble.setPadding(dp, dp2, dp, dp2);
        this.bubble.setTypeface(Typeface.DEFAULT_BOLD);
        this.bubble.setVisibility(8);
        this.container.addView(this.bubble, new FrameLayout.LayoutParams(-2, -2));
        this.bubbleVisible = false;
        this.frameKey = null;
        this.posReady = false;
    }

    private void detachViews() {
        restoreAll();
        removePoops();
        Iterator it = new ArrayList(this.particles).iterator();
        while (it.hasNext()) {
            lambda$spawnParticle$2((ImageView) it.next());
        }
        this.particles.clear();
        lambda$cleanPoop$6(this.bubble);
        lambda$cleanPoop$6(this.pet);
        this.pet = null;
        this.bubble = null;
        this.container = null;
        this.touch = null;
        this.platform = null;
        this.platforms = new ArrayList();
        if ("press".equals(this.state) || "drag".equals(this.state) || "pet".equals(this.state) || "hang".equals(this.state) || "peek".equals(this.state)) {
            this.state = "idle";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: removeView, reason: merged with bridge method [inline-methods] */
    public void lambda$cleanPoop$6(View view) {
        if (view == null) {
            return;
        }
        try {
            view.animate().cancel();
        } catch (Exception unused) {
        }
        try {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(view);
            }
        } catch (Exception unused2) {
        }
    }

    private void updateMetrics() {
        int max = Math.max(2, Math.round(this.host.dp(PetData.SIZE_DP[clampInt(this.cSize, 0, PetData.SIZE_DP.length - 1)])));
        this.unit = max;
        this.w = max * 18;
        this.h = max * 13;
    }

    private void rebuildSprites() {
        int i = this.w;
        int i2 = this.h;
        updateMetrics();
        this.sprites.configure(this.unit, PetData.PALETTE_BODY[cColorClamp()], PetData.PALETTE_EYE[cColorClamp()]);
        this.frameKey = null;
        ImageView imageView = this.pet;
        if (imageView == null) {
            return;
        }
        this.x += (i - this.w) / 2.0f;
        this.y += i2 - this.h;
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = this.w;
        layoutParams.height = this.h;
        this.pet.setLayoutParams(layoutParams);
        hop(this.host.dp(300.0f));
        schedule(16L);
    }

    private int cColorClamp() {
        return clampInt(this.cColor, 0, PetData.PALETTE_NAMES.length - 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1() {
        if (!this.running || this.paused) {
            return;
        }
        try {
            step();
        } catch (Exception e) {
            Log.e("clawd", "tick error", e);
        }
        if (!this.running || this.paused) {
            return;
        }
        schedule(nextDelay());
    }

    private void schedule(long j) {
        this.host.handler().removeCallbacks(this.tickR);
        this.host.handler().postDelayed(this.tickR, j);
    }

    private long nextDelay() {
        if (this.pet == null) {
            return 1000L;
        }
        double now = now();
        if ("press".equals(this.state) || "drag".equals(this.state) || "pet".equals(this.state) || "fly".equals(this.state) || "walk".equals(this.state) || this.hopActive || this.bubbleFadeIn || this.walkIn || this.platformMoving || this.slideV != 0.0f || this.shakePending != 0.0f || now < this.squashUntil) {
            return 16L;
        }
        if ("dizzy".equals(this.expr) && now < this.exprUntil) {
            return 16L;
        }
        if ("hang".equals(this.state) || "peek".equals(this.state)) {
            return 33L;
        }
        if ("sleep".equals(this.state)) {
            return 250L;
        }
        return (this.media == null || !"idle".equals(this.state)) ? 80L : 33L;
    }

    private float dpv(float f) {
        return this.host.dp(f);
    }

    private void bounds(double d) {
        if (this.boundsCache == null || d - this.boundsTime >= 0.4d || this.boundsW != this.host.screenWidth()) {
            int screenWidth = this.host.screenWidth();
            int screenHeight = this.host.screenHeight();
            int[] systemInsets = this.host.systemInsets();
            this.insetTop = systemInsets[0];
            this.insetBottom = systemInsets[1];
            this.rotation = this.host.displayRotation();
            this.boundsCache = new Rect(0, 0, Math.max(0, screenWidth - this.w), Math.max(this.insetTop, (screenHeight - this.insetBottom) - this.h));
            this.boundsW = screenWidth;
            this.boundsH = screenHeight;
            this.boundsTime = d;
        }
    }

    private float minX() {
        if (this.boundsCache != null) {
            return r0.left;
        }
        return 0.0f;
    }

    private float maxX() {
        if (this.boundsCache != null) {
            return r0.right;
        }
        return 0.0f;
    }

    private float minY() {
        if (this.boundsCache != null) {
            return r0.top;
        }
        return 0.0f;
    }

    private float maxY() {
        if (this.boundsCache != null) {
            return r0.bottom;
        }
        return 0.0f;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:78:0x0115, code lost:
    
        if (r0.equals("pet") == false) goto L44;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void step() {
        /*
            Method dump skipped, instructions count: 522
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.frizovdanya.clawd.PetEngine.step():void");
    }

    private void stepGround(double d, double d2, boolean z, float f, float f2, float f3, float f4) {
        int i;
        int i2;
        String str;
        float[] fArr;
        float f5 = f;
        float f6 = f2;
        this.walkIn = false;
        if (!"walk".equals(this.state)) {
            float f7 = this.x;
            if (f7 < f5 - 0.5f || f7 > f6 + 0.5f) {
                float dpv = dpv(60.0f) * ((float) d);
                float f8 = this.x;
                if (f8 < f5) {
                    this.x = Math.min(f5, f8 + dpv);
                    this.look = 1;
                } else {
                    this.x = Math.max(f6, f8 - dpv);
                    this.look = -1;
                }
                this.walkIn = true;
            } else {
                this.x = clamp(f7, f5, f6);
            }
        }
        this.platformMoving = false;
        if (z) {
            if (this.platform == null) {
                float f9 = this.y;
                if (f9 < f4 - 0.5f && !this.hopActive) {
                    fall(0.0f);
                    return;
                }
                this.y = Math.min(f9, f4);
            } else if (!followPlatform(f3, f4)) {
                fall(dpv(42.0f) * ("walk".equals(this.state) ? this.look : 0));
                return;
            }
            i = 1;
            i2 = -1;
            str = "walk";
            if (stepSlide(d, d2, f, f2)) {
                return;
            }
        } else {
            i = 1;
            i2 = -1;
            str = "walk";
            this.y = clamp(this.y, f3, f4);
            this.slideV = 0.0f;
        }
        if (str.equals(this.state)) {
            float f10 = this.walkTarget;
            float f11 = this.x;
            int i3 = f10 > f11 ? i : i2;
            this.look = i3;
            float dpv2 = f11 + (i3 * dpv(42.0f) * ((float) d));
            this.x = dpv2;
            int i4 = ((i3 <= 0 || dpv2 < this.walkTarget) && (i3 >= 0 || dpv2 > this.walkTarget)) ? 0 : i;
            if ((i3 >= 0 || dpv2 > f5) && (i3 <= 0 || dpv2 < f6)) {
                i = 0;
            }
            if (i4 == 0 && i == 0) {
                return;
            }
            this.x = clamp(dpv2, f5, f6);
            this.state = "idle";
            this.nextAction = (rnd(2.5d, 6.0d) * actFactor()) + d2;
            int i5 = this.peekPending;
            this.peekPending = 0;
            float dpv3 = dpv(4.0f);
            if (i5 != 0 && ((i5 < 0 && this.x <= f5 + dpv3) || (i5 > 0 && this.x >= f6 - dpv3))) {
                startPeek(d2, i5);
                return;
            } else {
                savePosition();
                return;
            }
        }
        if ("sleep".equals(this.state)) {
            if (d2 >= this.nextZ) {
                this.nextZ = d2 + 1.6d;
                spawnParticle("zzz", -1.0f, -1.0f);
                return;
            }
            return;
        }
        if (d2 >= this.nextBlink) {
            this.blinkUntil = 0.13d + d2;
            this.nextBlink = d2 + (this.rnd.nextFloat() < 0.15f ? 0.35d : rnd(2.0d, 5.5d));
        }
        int i6 = PetData.SLEEP_SECONDS[clampInt(this.cSleep, 0, PetData.SLEEP_SECONDS.length - 1)];
        if (i6 > 0 && d2 - this.lastInteraction > i6 && !this.hopActive && this.media == null && !this.walkIn) {
            this.state = "sleep";
            this.look = 0;
            this.nextZ = 0.6d + d2;
            hideBubble();
            return;
        }
        double d3 = this.poopDue;
        if (d3 > 0.0d && d2 >= d3 && !this.hopActive && !this.walkIn) {
            doPoop(d2, f5, f6);
            return;
        }
        String str2 = this.media;
        if (str2 != null) {
            if (d2 >= this.nextNote) {
                this.nextNote = ("music".equals(str2) ? 0.8d : 1.5d) + d2;
                spawnParticle("note", -1.0f, -1.0f);
                return;
            }
            return;
        }
        if (d2 < this.nextAction || this.hopActive || this.walkIn) {
            return;
        }
        this.nextAction = (rnd(3.5d, 9.0d) * actFactor()) + d2;
        if (this.cEat && d2 >= this.nextEat && this.eaten.size() < 5 && this.rnd.nextFloat() < 0.3f) {
            if (tryEat(d2, (int) f3)) {
                return;
            } else {
                this.nextEat = 10.0d + d2;
            }
        }
        float nextFloat = this.rnd.nextFloat();
        if (!this.cWander) {
            if (nextFloat < 0.5f) {
                this.look = this.rnd.nextInt(3) - 1;
                return;
            }
            return;
        }
        if (this.platform == null && d2 >= this.nextPeek && d2 - this.lastInteraction > 45.0d && nextFloat < 0.3f) {
            float f12 = this.x;
            int i7 = f12 - f5 < f6 - f12 ? i2 : i;
            if (i7 >= 0) {
                f5 = f6;
            }
            this.walkTarget = f5;
            this.peekPending = i7;
            this.state = str;
            return;
        }
        if (nextFloat < 0.38f) {
            float rnd = rnd(dpv(40.0f), dpv(160.0f)) * (this.rnd.nextBoolean() ? i : i2);
            if (this.platform != null && (fArr = this.platformSpan) != null) {
                f5 = Math.max(f5, fArr[0] - (this.unit * 4));
                f6 = Math.min(f6, this.platformSpan[i] - (this.unit * 14));
            }
            float clamp = clamp(this.x + rnd, f5, f6);
            if (Math.abs(clamp - this.x) > dpv(12.0f)) {
                this.walkTarget = clamp;
                this.state = str;
                return;
            }
            return;
        }
        if (nextFloat < 0.58f) {
            this.look = this.rnd.nextInt(3) - 1;
            return;
        }
        if (nextFloat < 0.72f) {
            say("idle", false, null);
        } else if (nextFloat < 0.84f) {
            hop(dpv(360.0f));
        } else {
            this.waveUntil = 1.6d + d2;
        }
    }

    private void fall(float f) {
        this.state = "fly";
        this.platform = null;
        this.platforms = new ArrayList();
        this.platformScan = 0.0d;
        this.vx = f + this.slideV;
        this.vy = 0.0f;
        this.slideV = 0.0f;
    }

    private float tiltAccel() {
        float f = this.cShake ? this.tilt : 0.0f;
        if (Math.abs(f) <= 0.22f) {
            return 0.0f;
        }
        return (f - ((f < 0.0f ? -1.0f : 1.0f) * 0.22f)) * dpv(2200.0f);
    }

    private boolean stepSlide(double d, double d2, float f, float f2) {
        if (!this.hopActive) {
            this.slideV += tiltAccel() * ((float) d);
        }
        float exp = this.slideV * ((float) Math.exp((-2.5d) * d));
        this.slideV = exp;
        if (Math.abs(exp) < dpv(6.0f)) {
            this.slideV = 0.0f;
            return false;
        }
        float f3 = this.x;
        float f4 = this.slideV;
        float f5 = f3 + (((float) d) * f4);
        this.x = f5;
        if (f5 < f || f5 > f2) {
            this.x = clamp(f5, f, f2);
            if (Math.abs(this.slideV) > dpv(150.0f)) {
                this.squashUntil = d2 + 0.12d;
                haptic(3);
            }
            this.slideV = 0.0f;
            return false;
        }
        if (Math.abs(f4) < dpv(60.0f)) {
            return false;
        }
        if ("sleep".equals(this.state)) {
            setExpr("surprised", 0.8d);
        }
        this.state = "idle";
        this.peekPending = 0;
        this.look = this.slideV > 0.0f ? 1 : -1;
        if (d2 >= this.nextSlideSay) {
            this.nextSlideSay = d2 + 8.0d;
            if (this.rnd.nextFloat() < 0.5f) {
                say("slide", false, null);
            }
        }
        return true;
    }

    private boolean feetOn(float f, float f2) {
        float f3 = this.x;
        int i = this.unit;
        return Math.min(f3 + ((float) (i * 14)), f2) - Math.max(((float) (i * 4)) + f3, f) >= ((float) (this.unit * 2));
    }

    private List<Platform> scanPlatformsCached(int i, int i2, double d) {
        if (d >= this.platformScan) {
            this.platformScan = d + 0.12d;
            List<Platform> scanPlatforms = this.host.scanPlatforms(i, i2);
            ArrayList arrayList = new ArrayList();
            int i3 = i - (this.unit * 2);
            int i4 = this.h;
            float f = i3 + i4;
            float f2 = i2 + i4;
            for (Platform platform : scanPlatforms) {
                if (platform.rect.right - platform.rect.left >= this.w * 0.6f && platform.rect.top >= f && platform.rect.top <= f2) {
                    arrayList.add(platform);
                }
            }
            this.platforms = arrayList;
        }
        return this.platforms;
    }

    private boolean followPlatform(float f, float f2) {
        if (this.platform == null) {
            return false;
        }
        Platform platform = null;
        float f3 = Float.MAX_VALUE;
        for (Platform platform2 : scanPlatformsCached((int) f, (int) f2, now())) {
            if (platform2.key.equals(this.platform.key)) {
                float abs = Math.abs(platform2.rect.top - this.platform.rect.top) + Math.abs(platform2.rect.left - this.platform.rect.left);
                if (abs < 400.0f && abs < f3) {
                    platform = platform2;
                    f3 = abs;
                }
            }
        }
        if (platform != null && feetOn(platform.rect.left, platform.rect.right)) {
            float f4 = platform.rect.top - this.h;
            if (f4 >= f - (this.unit * 2) && f4 <= f2) {
                this.platformMoving = Math.abs(f4 - this.y) > 0.5f;
                this.platformSpan = new float[]{platform.rect.left, platform.rect.right};
                this.y = f4;
                this.platform = platform;
                return true;
            }
        }
        return false;
    }

    private Float platformLanding(double d, float f, float f2, float f3) {
        List<Platform> scanPlatformsCached = scanPlatformsCached((int) f2, (int) f3, d);
        int i = this.h;
        float f4 = f + i;
        float f5 = this.y + i;
        Platform platform = null;
        for (Platform platform2 : scanPlatformsCached) {
            float f6 = platform2.rect.top;
            if (f4 <= 1.0f + f6 && f5 >= f6 && feetOn(platform2.rect.left, platform2.rect.right) && (platform == null || f6 < platform.rect.top)) {
                platform = platform2;
            }
        }
        if (platform == null) {
            return null;
        }
        float f7 = this.vy;
        this.y = platform.rect.top - this.h;
        if (f7 > dpv(380.0f)) {
            this.vy = (-f7) * 0.42f;
            this.vx *= 0.75f;
            this.squashUntil = d + 0.1d;
        } else {
            this.platformSpan = new float[]{platform.rect.left, platform.rect.right};
            land(d, platform);
        }
        return Float.valueOf(f7);
    }

    /* JADX WARN: Removed duplicated region for block: B:16:0x00b3  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x013e  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x0151  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00bd  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void stepFly(double r16, double r18, boolean r20, float r21, float r22, float r23, float r24) {
        /*
            Method dump skipped, instructions count: 388
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.frizovdanya.clawd.PetEngine.stepFly(double, double, boolean, float, float, float, float):void");
    }

    private void land(double d, Platform platform) {
        this.state = "idle";
        this.vx = 0.0f;
        this.vy = 0.0f;
        this.platform = platform;
        this.platforms = new ArrayList();
        this.squashUntil = 0.14d + d;
        this.nextAction = d + rnd(2.0d, 5.0d);
        if (this.shakeDizzy) {
            this.shakeDizzy = false;
            setExpr("dizzy", 3.0d);
            say("dizzy", false, null);
            if (this.boundsCache != null) {
                float clamp = clamp(this.x + (rnd(dpv(30.0f), dpv(70.0f)) * (this.rnd.nextBoolean() ? 1 : -1)), minX(), maxX());
                this.walkTarget = clamp;
                if (Math.abs(clamp - this.x) > dpv(12.0f)) {
                    this.state = "walk";
                }
            }
        }
        savePosition();
    }

    public void onAccel(float f, float f2, float f3) {
        float f4 = f;
        float f5 = f2;
        if (!this.running || this.paused || this.pet == null || !this.cShake) {
            return;
        }
        double now = now();
        float[] fArr = this.accelPrev;
        this.accelPrev = new float[]{f4, f5, f3};
        if (fArr != null && !"press".equals(this.state) && !"drag".equals(this.state) && !"pet".equals(this.state)) {
            float f6 = fArr[0];
            float f7 = (f4 - f6) * (f4 - f6);
            float f8 = fArr[1];
            float f9 = fArr[2];
            float sqrt = (float) Math.sqrt(f7 + ((f5 - f8) * (f5 - f8)) + ((f3 - f9) * (f3 - f9)));
            if (sqrt > 14.0f) {
                ArrayList arrayList = new ArrayList();
                Iterator<Double> it = this.shakeHits.iterator();
                while (it.hasNext()) {
                    double doubleValue = it.next().doubleValue();
                    if (now - doubleValue < 0.8999999761581421d) {
                        arrayList.add(Double.valueOf(doubleValue));
                    }
                }
                arrayList.add(Double.valueOf(now));
                if (arrayList.size() >= 4) {
                    arrayList.clear();
                    this.shakePending = Math.max(this.shakePending, sqrt);
                    schedule(0L);
                }
                this.shakeHits.clear();
                this.shakeHits.addAll(arrayList);
            }
        }
        int i = this.rotation;
        if (i != 1) {
            if (i == 2) {
                f5 = -f5;
            } else if (i == 3) {
                f5 = -f5;
                f4 = -f4;
            } else {
                f4 = -f4;
            }
            float f10 = f5;
            f5 = f4;
            f4 = f10;
        }
        this.tilt = (this.tilt * 0.7f) + ((f4 > 2.0f ? clamp(f5 / 9.81f, -1.0f, 1.0f) : 0.0f) * 0.3f);
    }

    private void applyShake(double d, boolean z) {
        float min = Math.min(this.shakePending, 40.0f);
        this.shakePending = 0.0f;
        boolean equals = "sleep".equals(this.state);
        if ("peek".equals(this.state)) {
            this.peekSide = 0;
        }
        this.peekPending = 0;
        this.platform = null;
        this.platforms = new ArrayList();
        this.platformScan = 0.0d;
        this.hopActive = false;
        this.hopOff = 0.0f;
        this.hopVy = 0.0f;
        this.slideV = 0.0f;
        this.state = "fly";
        float dpv = dpv((min * 55.0f) + 900.0f);
        double nextDouble = this.rnd.nextDouble() * 3.141592653589793d * 2.0d;
        double d2 = dpv;
        this.vx = (float) (Math.cos(nextDouble) * d2);
        this.vy = ((float) (Math.sin(nextDouble) * d2)) - (z ? dpv(700.0f) : 0.0f);
        this.shakeDizzy = true;
        this.lastInteraction = d;
        setExpr("surprised", 1.0d);
        haptic(1);
        if (equals) {
            say("wake", false, null);
        } else if (this.rnd.nextFloat() < 0.6f) {
            say("shake", false, null);
        }
    }

    private void startHang(double d, float f) {
        this.state = "hang";
        this.vx = 0.0f;
        this.vy = 0.0f;
        this.platform = null;
        this.y = f - (this.unit * 2);
        this.hangStart = d;
        this.hangUntil = d + rnd(2.0d, 3.5d);
        haptic(3);
        if (this.rnd.nextFloat() < 0.7f) {
            say("hang", false, null);
        }
    }

    private void stepHang(double d, boolean z, float f, float f2, float f3) {
        this.x = clamp(this.x, f, f2);
        this.y = f3 - (this.unit * 2);
        if (d < this.hangUntil) {
            return;
        }
        this.state = "fly";
        this.vx = 0.0f;
        this.vy = dpv(z ? 150.0f : 700.0f);
        setExpr("surprised", 0.8d);
        haptic(3);
        if (this.rnd.nextFloat() < 0.6f) {
            say("hang_fall", false, null);
        }
    }

    private void startPeek(double d, int i) {
        this.state = "peek";
        this.peekSide = i;
        this.peekPhase = "hide";
        this.peekPhaseUntil = rnd(3.0d, 6.0d) + d;
        this.peekEnd = d + rnd(30.0d, 60.0d);
        this.look = i;
    }

    private void stepPeek(double d, double d2, int i) {
        float peekX = peekX(i, "out".equals(this.peekPhase) ? 7 : 3);
        float dpv = dpv(50.0f) * ((float) d);
        boolean z = Math.abs(peekX - this.x) > 0.5f;
        this.peekMoving = z;
        if (z) {
            float f = this.x;
            if (peekX > f) {
                this.x = Math.min(peekX, f + dpv);
            } else {
                this.x = Math.max(peekX, f - dpv);
            }
        }
        if ("hide".equals(this.peekPhase) && this.peekMoving) {
            this.look = this.peekSide;
        } else {
            this.look = -this.peekSide;
        }
        if (d2 >= this.nextBlink) {
            this.blinkUntil = 0.13d + d2;
            this.nextBlink = rnd(2.0d, 5.5d) + d2;
        }
        if (d2 < this.peekPhaseUntil || this.peekMoving) {
            return;
        }
        if (d2 >= this.peekEnd) {
            endPeek(d2);
            return;
        }
        if ("hide".equals(this.peekPhase)) {
            this.peekPhase = "out";
            this.peekPhaseUntil = d2 + rnd(1.5d, 2.8d);
            if (this.rnd.nextFloat() < 0.5f) {
                say("peek", false, null);
                return;
            }
            return;
        }
        this.peekPhase = "hide";
        this.peekPhaseUntil = d2 + rnd(3.0d, 7.0d);
    }

    private void endPeek(double d) {
        int i = this.peekSide;
        this.peekSide = 0;
        this.peekMoving = false;
        this.nextPeek = rnd(60.0d, 120.0d) + d;
        this.state = "idle";
        if (this.boundsCache == null || i == 0) {
            return;
        }
        this.walkTarget = clamp(i < 0 ? minX() + dpv(30.0f) : maxX() - dpv(30.0f), minX(), maxX());
        this.state = "walk";
        this.nextAction = d + rnd(3.0d, 6.0d);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:110:0x01ca  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x01ba  */
    /* JADX WARN: Removed duplicated region for block: B:44:0x01df  */
    /* JADX WARN: Removed duplicated region for block: B:49:0x0200  */
    /* JADX WARN: Removed duplicated region for block: B:58:0x022a  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x024f A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:71:0x0258  */
    /* JADX WARN: Removed duplicated region for block: B:84:0x0283 A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:88:0x0290  */
    /* JADX WARN: Type inference failed for: r14v17 */
    /* JADX WARN: Type inference failed for: r14v18 */
    /* JADX WARN: Type inference failed for: r14v5 */
    /* JADX WARN: Type inference failed for: r14v8 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private java.lang.String compose(double r37) {
        /*
            Method dump skipped, instructions count: 692
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.frizovdanya.clawd.PetEngine.compose(double):java.lang.String");
    }

    private void render(double d) {
        float clamp;
        Touch touch;
        double sin;
        double sin2;
        String compose = compose(d);
        if (!compose.equals(this.frameKey)) {
            this.frameKey = compose;
            this.pet.setImageBitmap(this.sprites.frame(compose));
        }
        this.pet.setTranslationX(this.x);
        this.pet.setTranslationY(this.y + this.hopOff);
        this.host.syncRegion();
        if ("fly".equals(this.state)) {
            clamp = clamp(this.vx / dpv(70.0f), -25.0f, 25.0f);
        } else {
            if ("hang".equals(this.state)) {
                sin2 = Math.sin((d - this.hangStart) * 3.0d);
            } else {
                float f = this.slideV;
                if (f != 0.0f) {
                    clamp = clamp((-f) / dpv(30.0f), -12.0f, 12.0f);
                } else {
                    if ("dizzy".equals(this.expr) && d < this.exprUntil) {
                        sin = Math.sin(d * 10.0d) * 7.0d;
                    } else if (this.media != null && "idle".equals(this.state) && !this.hopActive) {
                        if ("music".equals(this.media)) {
                            sin2 = Math.sin(d * 3.141592653589793d * 2.0d);
                        } else {
                            sin = Math.sin(d * 2.5d) * 3.0d;
                        }
                    } else {
                        clamp = (!"drag".equals(this.state) || (touch = this.touch) == null || touch.hist.isEmpty() || d - this.touch.hist.get(this.touch.hist.size() + (-1))[0] >= 0.08d) ? 0.0f : clamp(this.touch.vx / dpv(90.0f), -18.0f, 18.0f);
                    }
                    clamp = (float) sin;
                }
            }
            sin = sin2 * 6.0d;
            clamp = (float) sin;
        }
        this.pet.setRotation(clamp);
    }

    public void getTouchRects(Rect rect, List<Rect> list) {
        if (rect != null) {
            ImageView imageView = this.pet;
            if (imageView == null) {
                rect.setEmpty();
            } else {
                int[] iArr = new int[2];
                imageView.getLocationOnScreen(iArr);
                int width = this.pet.getWidth() > 0 ? this.pet.getWidth() : this.unit * 18;
                int height = this.pet.getHeight() > 0 ? this.pet.getHeight() : this.unit * 13;
                int i = iArr[0];
                int i2 = iArr[1];
                rect.set(i, (this.unit * 2) + i2, width + i, i2 + height);
            }
        }
        if (list != null) {
            list.clear();
            int[] iArr2 = new int[2];
            for (ImageView imageView2 : this.poops) {
                imageView2.getLocationOnScreen(iArr2);
                int width2 = imageView2.getWidth() > 0 ? imageView2.getWidth() : (int) dpv(24.0f);
                int height2 = imageView2.getHeight() > 0 ? imageView2.getHeight() : (int) dpv(24.0f);
                int i3 = iArr2[0];
                int i4 = iArr2[1];
                list.add(new Rect(i3, i4, width2 + i3, height2 + i4));
            }
        }
    }

    public boolean dispatchTouch(MotionEvent motionEvent) {
        try {
            return onTouchEvent(motionEvent);
        } catch (Exception e) {
            Log.e("clawd", "touch error", e);
            return false;
        }
    }

    public boolean tapPoop(float f, float f2) {
        try {
            for (ImageView imageView : this.poops) {
                int round = Math.round(imageView.getTranslationX());
                int round2 = Math.round(imageView.getTranslationY());
                int width = imageView.getWidth() > 0 ? imageView.getWidth() : (int) dpv(24.0f);
                int height = imageView.getHeight() > 0 ? imageView.getHeight() : (int) dpv(24.0f);
                if (f >= round - dpv(4.0f) && f <= round + width + dpv(4.0f) && f2 >= round2 - dpv(4.0f) && f2 <= round2 + height + dpv(4.0f)) {
                    lambda$doPoop$5(imageView);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e("clawd", "poop tap error", e);
            return false;
        }
    }

    private void restorePosition() {
        float f;
        float f2;
        try {
            f = this.cfg.i("pos_x", 800) / 1000.0f;
            f2 = this.cfg.i("pos_y", 720) / 1000.0f;
        } catch (Exception unused) {
            f = 0.8f;
            f2 = 0.72f;
        }
        this.x = minX() + ((maxX() - minX()) * clamp(f, 0.0f, 1.0f));
        this.y = minY() + ((maxY() - minY()) * clamp(f2, 0.0f, 1.0f));
    }

    private void savePosition() {
        if (this.boundsCache == null) {
            return;
        }
        float max = Math.max(1.0f, maxX() - minX());
        float max2 = Math.max(1.0f, maxY() - minY());
        this.cfg.putInt("pos_x", (int) (clamp((this.x - minX()) / max, 0.0f, 1.0f) * 1000.0f));
        this.cfg.putInt("pos_y", (int) (clamp((this.y - minY()) / max2, 0.0f, 1.0f) * 1000.0f));
    }

    public void recenter() {
        if (!this.cShow) {
            this.cShow = true;
            this.cfg.putBool("show", true);
        }
        start(false);
        ViewGroup viewGroup = this.container;
        if (viewGroup == null || viewGroup.getWidth() <= 0) {
            return;
        }
        double now = now();
        bounds(now);
        this.x = (minX() + maxX()) / 2.0f;
        this.y = (minY() + maxY()) / 2.0f;
        this.posReady = true;
        this.state = "idle";
        this.vy = 0.0f;
        this.vx = 0.0f;
        this.slideV = 0.0f;
        this.platform = null;
        this.peekSide = 0;
        this.peekPending = 0;
        this.lastInteraction = now;
        hop(dpv(500.0f));
        for (int i = 0; i < 3; i++) {
            spawnParticle("spark", -1.0f, -1.0f);
        }
        say("hello", false, null);
        savePosition();
        schedule(0L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onTouch(View view, MotionEvent motionEvent) {
        try {
            return onTouchEvent(motionEvent);
        } catch (Exception e) {
            Log.e("clawd", "touch error", e);
            return false;
        }
    }

    private boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        String str;
        int actionMasked = motionEvent.getActionMasked();
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        double now = now();
        if (actionMasked == 0) {
            boolean equals = "sleep".equals(this.state);
            boolean equals2 = "peek".equals(this.state);
            this.lastInteraction = now;
            Touch touch = new Touch();
            this.touch = touch;
            touch.x0 = rawX;
            this.touch.y0 = rawY;
            this.touch.t0 = (float) now;
            this.touch.ox = rawX - this.x;
            this.touch.oy = rawY - this.y;
            this.touch.hist.add(new double[]{now, rawX, rawY});
            this.touch.anchor = rawX;
            this.hopActive = false;
            this.hopOff = 0.0f;
            this.hopVy = 0.0f;
            this.vy = 0.0f;
            this.vx = 0.0f;
            this.slideV = 0.0f;
            this.peekSide = 0;
            this.peekPending = 0;
            this.state = "press";
            if (equals) {
                setExpr("surprised", 0.9d);
                say("wake", false, null);
            } else if (equals2) {
                this.nextPeek = now + rnd(60.0d, 120.0d);
                setExpr("surprised", 0.7d);
                say("found", false, null);
            }
            schedule(0L);
            return true;
        }
        Touch touch2 = this.touch;
        if (touch2 == null) {
            return false;
        }
        if (actionMasked != 2) {
            if (actionMasked != 1 && actionMasked != 3) {
                return true;
            }
            this.touch = null;
            this.lastInteraction = now;
            String str2 = this.state;
            if ("drag".equals(str2)) {
                releaseDrag(now, touch2, actionMasked == 3);
            } else if ("pet".equals(str2)) {
                endPet(now);
            } else if ("press".equals(str2)) {
                this.state = "idle";
                z = true;
                if (actionMasked == 1) {
                    tap(now);
                }
                schedule(0L);
                this.host.syncRegion();
                return z;
            }
            z = true;
            schedule(0L);
            this.host.syncRegion();
            return z;
        }
        double d = rawX;
        touch2.hist.add(new double[]{now, d, rawY});
        while (touch2.hist.size() > 2 && now - touch2.hist.get(0)[0] > 0.1d) {
            touch2.hist.remove(0);
        }
        if (!"press".equals(this.state) || Math.hypot(rawX - touch2.x0, rawY - touch2.y0) <= dpv(8.0f)) {
            str = "drag";
        } else {
            str = "drag";
            this.state = str;
            this.platform = null;
            haptic(3);
            if (this.rnd.nextFloat() < 0.25f) {
                say(str, false, null);
            }
        }
        if (str.equals(this.state)) {
            float width = this.container != null ? r1.getWidth() : 0.0f;
            float height = this.container != null ? r7.getHeight() : 0.0f;
            this.x = clamp(rawX - touch2.ox, (-r8) * 0.3f, width - (this.w * 0.7f));
            this.y = clamp(rawY - touch2.oy, 0.0f, height - (this.h * 0.5f));
            double[] dArr = touch2.hist.get(0);
            double d2 = dArr[0];
            if (now - d2 > 0.005d) {
                touch2.vx = (float) ((d - dArr[1]) / (now - d2));
            }
            this.host.syncRegion();
            return true;
        }
        if (!"pet".equals(this.state)) {
            return true;
        }
        float f = rawX - touch2.anchor;
        if (Math.abs(f) <= dpv(10.0f)) {
            return true;
        }
        int i = f > 0.0f ? 1 : -1;
        if (touch2.dir != 0 && i != touch2.dir) {
            stroke();
        }
        touch2.dir = i;
        touch2.anchor = rawX;
        return true;
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x0075  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x00d1  */
    /* JADX WARN: Removed duplicated region for block: B:32:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void releaseDrag(double r19, com.frizovdanya.clawd.PetEngine.Touch r21, boolean r22) {
        /*
            Method dump skipped, instructions count: 216
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.frizovdanya.clawd.PetEngine.releaseDrag(double, com.frizovdanya.clawd.PetEngine$Touch, boolean):void");
    }

    private void tap(double d) {
        if (!this.eaten.isEmpty()) {
            this.lastTap = 0.0d;
            this.recentTaps.clear();
            spitAll(d);
            return;
        }
        if (d - this.lastTap < 0.32d) {
            this.lastTap = 0.0d;
            hop(dpv(720.0f));
            setExpr("happy", 1.3d);
            for (int i = 0; i < 3; i++) {
                spawnParticle("spark", -1.0f, -1.0f);
            }
            haptic(0);
            if (this.rnd.nextFloat() < 0.4f) {
                say("jump", false, null);
                return;
            }
            return;
        }
        this.lastTap = d;
        this.recentTaps.add(Double.valueOf(d));
        ArrayList arrayList = new ArrayList();
        Iterator<Double> it = this.recentTaps.iterator();
        while (it.hasNext()) {
            double doubleValue = it.next().doubleValue();
            if (d - doubleValue < 2.5d) {
                arrayList.add(Double.valueOf(doubleValue));
            }
        }
        this.recentTaps.clear();
        this.recentTaps.addAll(arrayList);
        if (this.recentTaps.size() >= 6) {
            this.recentTaps.clear();
            setExpr("angry", 2.5d);
            say("annoyed", true, null);
            haptic(1);
            return;
        }
        this.squashUntil = d + 0.12d;
        setExpr("surprised", 0.45d);
        hop(dpv(330.0f));
        haptic(3);
        if (this.rnd.nextFloat() < 0.35f) {
            say("tap", false, null);
        }
    }

    private void startPet(double d) {
        this.state = "pet";
        this.nextHeart = d + 0.1d;
        haptic(0);
    }

    private void stroke() {
        spawnParticle("heart", -1.0f, -1.0f);
        haptic(4);
    }

    private void endPet(double d) {
        this.state = "idle";
        setExpr("happy", 2.2d);
        this.nextAction = d + rnd(3.0d, 6.0d);
        Cfg cfg = this.cfg;
        cfg.putInt("pets_count", cfg.i("pets_count", 0) + 1);
        if (this.rnd.nextFloat() < 0.6f) {
            say("pet", false, null);
        }
    }

    private boolean canReact() {
        return this.running && !this.paused && this.pet != null && this.posReady && this.cTgEvents && ("idle".equals(this.state) || "walk".equals(this.state) || "sleep".equals(this.state) || "peek".equals(this.state));
    }

    private void wakeForEvent(double d) {
        this.lastInteraction = d;
        if ("peek".equals(this.state)) {
            endPeek(d);
        } else if ("sleep".equals(this.state)) {
            this.state = "idle";
            setExpr("surprised", 0.9d);
        }
    }

    public void onIncoming(String str, List<String> list) {
        if (canReact()) {
            reactIncoming(matchKeyword(list), str != null && str.equals(this.host.activePkg()) && sameChatTitle(str, list));
        }
    }

    private boolean sameChatTitle(String str, List<String> list) {
        String activeChatTitle = this.host.activeChatTitle();
        if (activeChatTitle == null || activeChatTitle.isEmpty() || list.isEmpty()) {
            return false;
        }
        return list.get(0).trim().equalsIgnoreCase(activeChatTitle);
    }

    private String matchKeyword(List<String> list) {
        for (int i = 0; i < KW_HOOD.length; i++) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                if (KW_PAT[i].matcher(it.next()).find()) {
                    return KW_HOOD[i];
                }
            }
        }
        return null;
    }

    private void reactIncoming(String str, boolean z) {
        double now = now();
        if (!(str == null && z) && now >= this.nextMsgReact) {
            this.nextMsgReact = (str != null ? 3.0d : 6.0d) + now;
            wakeForEvent(now);
            if ("love".equals(str)) {
                setExpr("happy", 2.2d);
                for (int i = 0; i < 3; i++) {
                    spawnParticle("heart", -1.0f, -1.0f);
                }
                say("love", false, null);
            } else if ("laugh".equals(str)) {
                setExpr("happy", 1.6d);
                hop(dpv(420.0f));
                spawnParticle("spark", -1.0f, -1.0f);
                say("laugh", false, null);
            } else if ("hi".equals(str)) {
                this.waveUntil = 1.8d + now;
                say("hi", false, null);
            } else {
                this.look = this.rnd.nextBoolean() ? -1 : 1;
                setExpr("surprised", 0.7d);
                hop(dpv(300.0f));
                say("mail", false, null);
            }
            this.nextAction = Math.max(this.nextAction, now + 2.5d);
            schedule(0L);
        }
    }

    public void onSent(int i) {
        if (canReact()) {
            double now = now();
            if (now < this.nextSentReact) {
                return;
            }
            this.nextSentReact = 1.5d + now;
            wakeForEvent(now);
            setExpr("happy", 1.2d);
            hop(dpv(520.0f));
            for (int i2 = 0; i2 < 2; i2++) {
                spawnParticle("spark", -1.0f, -1.0f);
            }
            if (i > 300) {
                say("essay", false, null);
            } else if (this.rnd.nextFloat() < 0.3f) {
                say("sent", false, null);
            }
            this.nextAction = Math.max(this.nextAction, now + 2.5d);
            schedule(0L);
        }
    }

    private void pollMedia(double d) {
        if (d < this.mediaCheck) {
            return;
        }
        this.mediaCheck = 0.5d + d;
        String playingMediaKind = this.cDance ? this.host.playingMediaKind() : null;
        if (ObjectsEquals(playingMediaKind, this.media)) {
            return;
        }
        String str = this.media;
        this.media = playingMediaKind;
        if (playingMediaKind == null) {
            this.lastInteraction = d;
            if ("music".equals(str) && "idle".equals(this.state) && this.rnd.nextFloat() < 0.5f) {
                say("music_end", false, null);
            }
            this.nextAction = d + rnd(2.0d, 4.0d);
            return;
        }
        this.lastInteraction = d;
        this.nextNote = 0.3d + d;
        if ("peek".equals(this.state)) {
            endPeek(d);
        } else if ("sleep".equals(this.state)) {
            this.state = "idle";
            setExpr("surprised", 0.8d);
        }
        if (str == null) {
            if ("idle".equals(this.state) || "walk".equals(this.state)) {
                say("music".equals(playingMediaKind) ? "music" : "listen", false, null);
            }
        }
    }

    private void pollChat(double d) {
        String str;
        String activePkg = this.host.activePkg();
        String activeChatTitle = this.host.activeChatTitle();
        if (activePkg != null && !activePkg.isEmpty()) {
            str = activePkg + "|" + (activeChatTitle != null ? activeChatTitle : "");
        }
        if (str.equals(this.chatDialog)) {
            return;
        }
        this.chatDialog = str;
        if (str.isEmpty() || !this.cTgEvents) {
            return;
        }
        Integer remove = this.chatVisits.remove(str);
        int intValue = (remove == null ? 0 : remove.intValue()) + 1;
        this.chatVisits.put(str, Integer.valueOf(intValue));
        if (this.chatVisits.size() > 200) {
            String next = this.chatVisits.keySet().iterator().next();
            if (!next.equals(str)) {
                this.chatVisits.remove(next);
            }
        }
        if (d >= this.nextChatComment) {
            if (("idle".equals(this.state) || "walk".equals(this.state)) && this.rnd.nextFloat() <= 0.4f) {
                this.nextChatComment = d + 25.0d;
                if (activeChatTitle != null && activeChatTitle.length() > 20) {
                    activeChatTitle = activeChatTitle.substring(0, 19).trim() + "…";
                }
                if (activeChatTitle == null || activeChatTitle.isEmpty()) {
                    return;
                }
                if ("Избранное".equalsIgnoreCase(activeChatTitle) || "Saved Messages".equalsIgnoreCase(activeChatTitle)) {
                    say("chat_self", false, null);
                } else if (intValue > 1) {
                    say("chat_again", false, activeChatTitle);
                } else {
                    say("chat_new", false, activeChatTitle);
                }
            }
        }
    }

    private void setExpr(String str, double d) {
        this.expr = str;
        this.exprUntil = now() + d;
    }

    private void hop(float f) {
        if ("idle".equals(this.state) || "walk".equals(this.state)) {
            this.hopActive = true;
            this.hopVy = -f;
            if (this.hopOff > 0.0f) {
                this.hopOff = 0.0f;
            }
        }
    }

    private void haptic(int i) {
        ImageView imageView;
        if (!this.cHaptics || (imageView = this.pet) == null) {
            return;
        }
        try {
            if (imageView.performHapticFeedback(i)) {
                return;
            }
            this.host.vibrate(i);
        } catch (Exception unused) {
            this.host.vibrate(i);
        }
    }

    private void spawnParticle(String str, float f, float f2) {
        float rnd;
        float rnd2;
        if (this.container == null || this.pet == null || this.particles.size() >= 14) {
            return;
        }
        try {
            Bitmap particle = this.sprites.particle(str);
            if (particle == null) {
                return;
            }
            int width = particle.getWidth();
            int height = particle.getHeight();
            final ImageView imageView = new ImageView(this.host.ctx());
            imageView.setImageBitmap(particle);
            this.container.addView(imageView, new FrameLayout.LayoutParams(width, height));
            this.particles.add(imageView);
            float f3 = height;
            float f4 = ((this.y + this.hopOff) + (this.unit * 2)) - f3;
            if (f >= 0.0f) {
                rnd = (f - (width / 2.0f)) + rnd(-dpv(8.0f), dpv(8.0f));
                f4 = f2 - f3;
                rnd2 = rnd(-dpv(16.0f), dpv(16.0f));
            } else if ("zzz".equals(str)) {
                rnd = this.x + (this.w * 0.72f);
                f4 += this.unit * 2;
                rnd2 = dpv(18.0f);
            } else {
                float f5 = this.x;
                int i = this.w;
                rnd = ((f5 + (i / 2.0f)) - (width / 2.0f)) + rnd((-i) * 0.25f, i * 0.25f);
                rnd2 = rnd(-dpv(16.0f), dpv(16.0f));
            }
            imageView.setTranslationX(rnd);
            imageView.setTranslationY(f4);
            imageView.setScaleX(0.5f);
            imageView.setScaleY(0.5f);
            imageView.setRotation(rnd(-15.0f, 15.0f));
            imageView.animate().translationXBy(rnd2).translationYBy(-dpv(rnd(40.0f, 58.0f))).scaleX(1.0f).scaleY(1.0f).alpha(0.0f).setDuration("zzz".equals(str) ? 1600L : 1100L).withEndAction(new Runnable() { // from class: com.frizovdanya.clawd.PetEngine$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    PetEngine.this.lambda$spawnParticle$2(imageView);
                }
            }).start();
        } catch (Exception e) {
            Log.e("clawd", "particle error", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: removeParticle, reason: merged with bridge method [inline-methods] */
    public void lambda$spawnParticle$2(ImageView imageView) {
        this.particles.remove(imageView);
        lambda$cleanPoop$6(imageView);
    }

    private void say(String str, boolean z, String str2) {
        if (this.bubble != null) {
            if (z || this.cPhrases) {
                String[] strArr = PetData.PHRASES.get(str);
                if (strArr == null) {
                    strArr = new String[]{str};
                }
                if ("idle".equals(str) || "hello".equals(str)) {
                    try {
                        String s = this.cfg.s("my_phrases", "");
                        if (s != null && !s.isEmpty()) {
                            ArrayList arrayList = new ArrayList(Arrays.asList(strArr));
                            for (String str3 : s.split("\n")) {
                                String trim = str3.trim();
                                if (!trim.isEmpty()) {
                                    arrayList.add(trim);
                                }
                            }
                            strArr = (String[]) arrayList.toArray(new String[0]);
                        }
                    } catch (Exception unused) {
                    }
                }
                if (str2 == null) {
                    ArrayList arrayList2 = new ArrayList();
                    for (String str4 : strArr) {
                        if (!str4.contains("{name}")) {
                            arrayList2.add(str4);
                        }
                    }
                    if (!arrayList2.isEmpty()) {
                        strArr = (String[]) arrayList2.toArray(new String[0]);
                    }
                }
                ArrayList arrayList3 = new ArrayList();
                for (String str5 : strArr) {
                    if (!str5.equals(this.lastPhrase)) {
                        arrayList3.add(str5);
                    }
                }
                if (arrayList3.isEmpty()) {
                    arrayList3.addAll(Arrays.asList(strArr));
                }
                String str6 = (String) arrayList3.get(this.rnd.nextInt(arrayList3.size()));
                if (str6.contains("{name}")) {
                    if (str2 == null) {
                        str2 = "";
                    }
                    str6 = str6.replace("{name}", str2);
                }
                this.lastPhrase = str6;
                double now = now();
                try {
                    boolean z2 = this.nightMode;
                    int i = -1;
                    int i2 = z2 ? -13882322 : -1;
                    int i3 = z2 ? -12961220 : -2236963;
                    if (!z2) {
                        i = -15658735;
                    }
                    GradientDrawable gradientDrawable = new GradientDrawable();
                    gradientDrawable.setCornerRadius(dpv(12.0f));
                    gradientDrawable.setColor(i2);
                    gradientDrawable.setStroke(Math.max(1, (int) dpv(1.0f)), i3);
                    this.bubble.setBackground(gradientDrawable);
                    this.bubble.setTextColor(i);
                    this.bubble.setElevation(dpv(2.0f));
                    this.bubble.animate().cancel();
                    this.bubble.setText(str6);
                    this.bubble.setAlpha(0.0f);
                    this.bubble.setVisibility(0);
                    this.bubbleGen++;
                    this.bubbleVisible = true;
                    this.bubbleFadeIn = true;
                    this.bubbleUntil = now + 2.0d + (str6.length() * 0.05d);
                } catch (Exception e) {
                    Log.e("clawd", "bubble error", e);
                }
            }
        }
    }

    private void hideBubble() {
        final TextView textView = this.bubble;
        if (textView == null || !this.bubbleVisible) {
            return;
        }
        this.bubbleVisible = false;
        this.bubbleFadeIn = false;
        final int i = this.bubbleGen + 1;
        this.bubbleGen = i;
        textView.animate().alpha(0.0f).setDuration(180L).withEndAction(new Runnable() { // from class: com.frizovdanya.clawd.PetEngine$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() {
                PetEngine.this.lambda$hideBubble$3(i, textView);
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$hideBubble$3(int i, TextView textView) {
        if (i != this.bubbleGen || textView == null) {
            return;
        }
        textView.setVisibility(8);
    }

    private void updateBubble(double d, int i, float f) {
        TextView textView = this.bubble;
        if (textView == null || !this.bubbleVisible) {
            return;
        }
        if (d >= this.bubbleUntil) {
            hideBubble();
            return;
        }
        int width = textView.getWidth();
        int height = this.bubble.getHeight();
        if (width == 0) {
            return;
        }
        float dpv = dpv(4.0f);
        float clamp = clamp((this.x + (this.w / 2.0f)) - (width / 2.0f), dpv, (i - width) - dpv);
        float f2 = this.y;
        float f3 = this.hopOff;
        float f4 = (((f2 + f3) + this.unit) - height) - dpv;
        if (f4 < f) {
            f4 = f2 + f3 + this.h + dpv;
        }
        this.bubble.setTranslationX(clamp);
        this.bubble.setTranslationY(f4);
        if (this.bubbleFadeIn) {
            this.bubbleFadeIn = false;
            this.bubble.animate().alpha(1.0f).setDuration(150L).start();
        }
    }

    private boolean tryEat(double d, int i) {
        try {
            Bitmap latestFrame = this.host.latestFrame();
            if (latestFrame == null) {
                return false;
            }
            EatTarget pickEatTarget = this.host.pickEatTarget(i, (int) (this.x + (this.w / 2)), (int) (this.y + (this.h / 2)));
            if (pickEatTarget == null) {
                return false;
            }
            Rect rect = pickEatTarget.rect;
            if (rect.left >= 0 && rect.top >= i && rect.right <= screenW() && rect.bottom <= this.boundsH) {
                try {
                    Bitmap createBitmap = Bitmap.createBitmap(latestFrame, rect.left, rect.top, rect.width(), rect.height());
                    final EatenItem eatenItem = new EatenItem();
                    eatenItem.target = pickEatTarget;
                    eatenItem.bmp = createBitmap;
                    eatenItem.w = rect.width();
                    eatenItem.hh = rect.height();
                    eatenItem.patch = makePatch(latestFrame, rect);
                    this.container.addView(eatenItem.patch, Math.max(0, this.container.indexOfChild(this.pet)), new FrameLayout.LayoutParams(rect.width(), rect.height()));
                    eatenItem.patch.setTranslationX(rect.left);
                    eatenItem.patch.setTranslationY(rect.top);
                    this.eaten.add(eatenItem);
                    final ImageView makeCopy = makeCopy(createBitmap, rect.width(), rect.height());
                    eatenItem.copy = makeCopy;
                    makeCopy.setTranslationX(rect.left);
                    makeCopy.setTranslationY(rect.top);
                    float mouthX = mouthX();
                    float mouthY = mouthY();
                    this.look = ((float) rect.left) + (((float) rect.width()) / 2.0f) > mouthX ? 1 : -1;
                    this.mouthOpenUntil = 0.7d + d;
                    this.chewUntil = 0.0d;
                    this.nextAction = 3.0d + d;
                    this.nextEat = d + rnd(40.0f, 120.0f);
                    makeCopy.animate().translationX(mouthX - (rect.width() / 2.0f)).translationY(mouthY - (rect.height() / 2.0f)).scaleX(0.05f).scaleY(0.05f).rotation((this.rnd.nextBoolean() ? 1 : -1) * rnd(120.0f, 300.0f)).setInterpolator(new AccelerateInterpolator(1.6f)).setDuration(560L).withEndAction(new Runnable() { // from class: com.frizovdanya.clawd.PetEngine$$ExternalSyntheticLambda6
                        @Override // java.lang.Runnable
                        public final void run() {
                            PetEngine.this.lambda$tryEat$4(makeCopy, eatenItem);
                        }
                    }).start();
                    return true;
                } catch (Exception unused) {
                }
            }
            return false;
        } catch (Exception e) {
            Log.e("clawd", "eat error", e);
            return false;
        }
    }

    private View makePatch(Bitmap bitmap, Rect rect) {
        View view = new View(this.host.ctx());
        int ringMedian = ringMedian(bitmap, rect, Math.max(2, Math.round(dpv(3.0f))));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(dpv(6.0f));
        gradientDrawable.setColor(ringMedian);
        view.setBackground(gradientDrawable);
        return view;
    }

    private int ringMedian(Bitmap bitmap, Rect rect, int i) {
        int i2;
        int max = Math.max(0, rect.left - i);
        int min = Math.min(bitmap.getWidth() - 1, rect.right + i);
        int min2 = Math.min(bitmap.getHeight() - 1, rect.bottom + i);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        int[] iArr = new int[1];
        for (int max2 = Math.max(0, rect.top - i); max2 <= min2; max2++) {
            int i3 = max;
            while (i3 <= min) {
                if (max2 < rect.top || max2 >= rect.bottom || i3 < rect.left || i3 >= rect.right) {
                    i2 = i3;
                    try {
                        bitmap.getPixels(iArr, 0, 1, i3, max2, 1, 1);
                        int i4 = iArr[0];
                        arrayList.add(Integer.valueOf((i4 >> 16) & 255));
                        arrayList2.add(Integer.valueOf((i4 >> 8) & 255));
                        arrayList3.add(Integer.valueOf(i4 & 255));
                    } catch (Exception unused) {
                    }
                } else {
                    i2 = i3;
                }
                i3 = i2 + 1;
            }
        }
        if (arrayList.isEmpty()) {
            return -7829368;
        }
        return (median(arrayList) << 16) | (-16777216) | (median(arrayList2) << 8) | median(arrayList3);
    }

    private static int median(List<Integer> list) {
        Collections.sort(list);
        return list.get(list.size() / 2).intValue();
    }

    private ImageView makeCopy(Bitmap bitmap, int i, int i2) {
        ImageView imageView = new ImageView(this.host.ctx());
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.container.addView(imageView, Math.max(0, this.container.indexOfChild(this.pet)), new FrameLayout.LayoutParams(i, i2));
        this.flying.add(imageView);
        return imageView;
    }

    private void removeFlying(ImageView imageView) {
        this.flying.remove(imageView);
        lambda$cleanPoop$6(imageView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: onSwallowed, reason: merged with bridge method [inline-methods] */
    public void lambda$tryEat$4(ImageView imageView, EatenItem eatenItem) {
        removeFlying(imageView);
        eatenItem.copy = null;
        if (this.pet == null) {
            return;
        }
        double now = now();
        this.mouthOpenUntil = 0.0d;
        this.chewUntil = 1.4d + now;
        this.squashUntil = 0.12d + now;
        setExpr("happy", 1.6d);
        haptic(3);
        Cfg cfg = this.cfg;
        int i = 0;
        cfg.putInt("eaten_count", cfg.i("eaten_count", 0) + 1);
        if (this.rnd.nextFloat() < 0.7f) {
            say("eat", false, null);
        }
        if (this.cPoop) {
            int i2 = this.cfg.i("poop_meter", 0) + 1;
            if (i2 >= this.poopNeed) {
                this.poopNeed = this.rnd.nextInt(3) + 3;
                this.poopDue = now + rnd(5.0d, 10.0d);
            } else {
                i = i2;
            }
            this.cfg.putInt("poop_meter", i);
        }
    }

    private void doPoop(double d, float f, float f2) {
        int i;
        this.poopDue = 0.0d;
        if (!this.cPoop || this.container == null || this.pet == null || this.poops.size() >= 3) {
            return;
        }
        int i2 = this.look;
        if (i2 != 0) {
            i = -i2;
        } else {
            i = this.rnd.nextBoolean() ? -1 : 1;
        }
        try {
            Bitmap particle = this.sprites.particle("poop");
            if (particle == null) {
                return;
            }
            int width = particle.getWidth();
            int height = particle.getHeight();
            float dpv = dpv(6.0f);
            ImageView imageView = new ImageView(this.host.ctx());
            imageView.setImageBitmap(particle);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            int i3 = (int) dpv;
            imageView.setPadding(i3, i3, i3, i3);
            imageView.setOnClickListener(new View.OnClickListener() { // from class: com.frizovdanya.clawd.PetEngine$$ExternalSyntheticLambda8
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    PetEngine.this.lambda$doPoop$5(view);
                }
            });
            float f3 = width;
            float f4 = dpv * 2.0f;
            float f5 = height;
            this.container.addView(imageView, Math.max(0, this.container.indexOfChild(this.pet)), new FrameLayout.LayoutParams((int) (f3 + f4), (int) (f5 + f4)));
            float f6 = this.y + (this.unit * 12);
            imageView.setTranslationX(clamp((((this.x + (this.w / 2.0f)) + ((r10 * i) * 0.42f)) - (f3 / 2.0f)) - dpv, 0.0f, (this.container.getWidth() - width) - f4));
            imageView.setTranslationY((f6 - f5) - dpv);
            imageView.setScaleX(0.1f);
            imageView.setScaleY(0.1f);
            imageView.animate().scaleX(1.0f).scaleY(1.0f).setInterpolator(new OvershootInterpolator(2.0f)).setDuration(320L).start();
            this.poops.add(imageView);
            this.host.syncRegion();
            this.squashUntil = d + 0.2d;
            setExpr("happy", 1.8d);
            haptic(3);
            say("poop", false, null);
            this.look = -i;
            float clamp = clamp(this.x - (i * rnd(dpv(40.0f), dpv(80.0f))), f, f2);
            if (Math.abs(clamp - this.x) > dpv(12.0f)) {
                this.walkTarget = clamp;
                this.state = "walk";
            }
            this.nextAction = d + rnd(3.0d, 6.0d);
        } catch (Exception e) {
            Log.e("clawd", "poop error", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: cleanPoop, reason: merged with bridge method [inline-methods] */
    public void lambda$doPoop$5(View view) {
        final ImageView imageView;
        Iterator<ImageView> it = this.poops.iterator();
        while (true) {
            if (it.hasNext()) {
                imageView = it.next();
                if (imageView == view) {
                    break;
                }
            } else {
                imageView = null;
                break;
            }
        }
        if (imageView == null) {
            return;
        }
        this.poops.remove(imageView);
        imageView.setClickable(false);
        float translationX = imageView.getTranslationX() + (imageView.getWidth() / 2.0f);
        float translationY = imageView.getTranslationY() + (imageView.getHeight() / 2.0f);
        for (int i = 0; i < 3; i++) {
            spawnParticle("spark", translationX, translationY);
        }
        imageView.animate().scaleX(0.0f).scaleY(0.0f).alpha(0.0f).rotation(90.0f).setDuration(260L).withEndAction(new Runnable() { // from class: com.frizovdanya.clawd.PetEngine$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                PetEngine.this.lambda$cleanPoop$6(imageView);
            }
        }).start();
        Cfg cfg = this.cfg;
        cfg.putInt("poops_cleaned", cfg.i("poops_cleaned", 0) + 1);
        haptic(3);
        if (this.pet != null && ("idle".equals(this.state) || "walk".equals(this.state))) {
            setExpr("happy", 1.5d);
            if (this.rnd.nextFloat() < 0.7f) {
                say("clean", false, null);
            }
            schedule(0L);
        }
        this.host.syncRegion();
    }

    private void removePoops() {
        Iterator it = new ArrayList(this.poops).iterator();
        while (it.hasNext()) {
            lambda$cleanPoop$6((ImageView) it.next());
        }
        this.poops.clear();
        Host host = this.host;
        if (host != null) {
            host.syncRegion();
        }
    }

    private void pruneEaten() {
        ArrayList arrayList = new ArrayList();
        String activePkg = this.host.activePkg();
        for (EatenItem eatenItem : this.eaten) {
            if (eatenItem.spitting) {
                arrayList.add(eatenItem);
            } else {
                try {
                    boolean refreshEatTarget = this.host.refreshEatTarget(eatenItem.target);
                    if ((!refreshEatTarget || activePkg == null || activePkg.equals(eatenItem.target.pkg)) && refreshEatTarget) {
                        eatenItem.patch.setTranslationX(eatenItem.target.rect.left);
                        eatenItem.patch.setTranslationY(eatenItem.target.rect.top);
                        arrayList.add(eatenItem);
                    }
                } catch (Exception unused) {
                }
                lambda$cleanPoop$6(eatenItem.patch);
            }
        }
        this.eaten.clear();
        this.eaten.addAll(arrayList);
    }

    private void restoreItem(EatenItem eatenItem) {
        lambda$cleanPoop$6(eatenItem.patch);
        eatenItem.patch = null;
    }

    private void restoreAll() {
        ArrayList<EatenItem> arrayList = new ArrayList(this.eaten);
        arrayList.addAll(this.spitting);
        this.eaten.clear();
        this.spitting.clear();
        Iterator it = new ArrayList(this.flying).iterator();
        while (it.hasNext()) {
            lambda$cleanPoop$6((ImageView) it.next());
        }
        this.flying.clear();
        for (EatenItem eatenItem : arrayList) {
            if (eatenItem.patch != null) {
                lambda$cleanPoop$6(eatenItem.patch);
            }
        }
    }

    public void giveBack() {
        if (!this.eaten.isEmpty() && this.pet != null && this.container != null) {
            spitAll(now());
        } else {
            restoreAll();
        }
    }

    private void spitAll(double d) {
        ArrayList arrayList = new ArrayList(this.eaten);
        this.eaten.clear();
        if (arrayList.isEmpty()) {
            return;
        }
        this.chewUntil = 0.0d;
        this.mouthOpenUntil = 0.35d + d + (arrayList.size() * 0.08d);
        setExpr("surprised", 0.9d);
        this.squashUntil = 0.12d + d;
        hop(dpv(300.0f));
        haptic(1);
        say("spit", false, null);
        this.nextEat = Math.max(this.nextEat, d + rnd(40.0f, 120.0f));
        float mouthX = mouthX();
        float mouthY = mouthY();
        for (int i = 0; i < arrayList.size(); i++) {
            spitOne((EatenItem) arrayList.get(i), mouthX, mouthY, i * 80);
        }
        schedule(0L);
    }

    private void spitOne(final EatenItem eatenItem, float f, float f2, long j) {
        try {
            if (this.container == null) {
                restoreItem(eatenItem);
                return;
            }
            Rect rect = eatenItem.target.rect;
            boolean z = false;
            int i = 1;
            boolean z2 = rect.right > 0 && rect.bottom > 0 && rect.left < screenW() && rect.top < this.boundsH;
            if (this.host.activePkg() != null && this.host.activePkg().equals(eatenItem.target.pkg)) {
                z = true;
            }
            if (z2 && z) {
                eatenItem.spitting = true;
                this.spitting.add(eatenItem);
                final ImageView makeCopy = makeCopy(eatenItem.bmp, eatenItem.w, eatenItem.hh);
                eatenItem.copy = makeCopy;
                makeCopy.setTranslationX(f - (eatenItem.w / 2.0f));
                makeCopy.setTranslationY(f2 - (eatenItem.hh / 2.0f));
                makeCopy.setScaleX(0.01f);
                makeCopy.setScaleY(0.01f);
                if (!this.rnd.nextBoolean()) {
                    i = -1;
                }
                makeCopy.setRotation(i * rnd(120.0f, 300.0f));
                makeCopy.animate().translationX(rect.left).translationY(rect.top).scaleX(1.0f).scaleY(1.0f).rotation(0.0f).setInterpolator(new OvershootInterpolator(1.2f)).setStartDelay(j).setDuration(460L).withEndAction(new Runnable() { // from class: com.frizovdanya.clawd.PetEngine$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        PetEngine.this.lambda$spitOne$7(makeCopy, eatenItem);
                    }
                }).start();
                return;
            }
            restoreItem(eatenItem);
        } catch (Exception e) {
            Log.e("clawd", "spit error", e);
            this.spitting.remove(eatenItem);
            restoreItem(eatenItem);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$spitOne$7(ImageView imageView, EatenItem eatenItem) {
        removeFlying(imageView);
        this.spitting.remove(eatenItem);
        restoreItem(eatenItem);
    }

    private static boolean ObjectsEquals(String str, String str2) {
        if (str == null) {
            return str2 == null;
        }
        return str.equals(str2);
    }

    private float rnd(float f, float f2) {
        return f + (this.rnd.nextFloat() * (f2 - f));
    }

    private double rnd(double d, double d2) {
        return d + (this.rnd.nextDouble() * (d2 - d));
    }

    public void toggleSleep() {
        if (!this.running || this.pet == null) {
            return;
        }
        double now = now();
        if ("sleep".equals(this.state)) {
            this.state = "idle";
            setExpr("surprised", 0.9d);
            say("wake", false, null);
        } else {
            if (!"idle".equals(this.state) && !"walk".equals(this.state)) {
                return;
            }
            this.state = "sleep";
            this.look = 0;
            this.nextZ = 0.6d + now;
            hideBubble();
        }
        this.lastInteraction = now;
        schedule(0L);
    }

    public String stateLabel() {
        if (!this.cShow || this.pet == null) {
            return "скрыт";
        }
        String str = this.state;
        str.hashCode();
        switch (str) {
            case "fly":
                return "летит";
            case "pet":
                return "гладят";
            case "drag":
                return "перетаскивают";
            case "hang":
                return "держится за край";
            case "peek":
                return "прячется";
            case "walk":
                return "идёт";
            case "press":
                return "нажат";
            case "sleep":
                return "спит";
            default:
                return this.media != null ? "танцует" : "сидит";
        }
    }
}
