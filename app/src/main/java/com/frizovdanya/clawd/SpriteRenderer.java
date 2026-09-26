package com.frizovdanya.clawd;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class SpriteRenderer {
    private int bodyColor;
    private int eyeColor;
    private final Map<String, Bitmap> frames = new LinkedHashMap<String, Bitmap>(16, 0.75f, true) { // from class: com.frizovdanya.clawd.SpriteRenderer.1
        @Override // java.util.LinkedHashMap
        protected boolean removeEldestEntry(Map.Entry<String, Bitmap> entry) {
            return size() > 48;
        }
    };
    private final Map<String, Bitmap> particles = new HashMap();
    private int unit;

    public int getUnit() {
        return this.unit;
    }

    public void configure(int i, int i2, int i3) {
        this.unit = i;
        this.bodyColor = i2;
        this.eyeColor = i3;
        this.frames.clear();
        this.particles.clear();
    }

    public Bitmap frame(String str) {
        Bitmap bitmap = this.frames.get(str);
        if (bitmap != null) {
            return bitmap;
        }
        Bitmap renderFrame = renderFrame(str);
        this.frames.put(str, renderFrame);
        return renderFrame;
    }

    public static String frameKey(String str, int i, String str2, String str3, int i2, boolean z, String str4) {
        StringBuilder append = new StringBuilder().append(str).append('|').append(i).append('|').append(str2).append('|').append(str3).append('|').append(i2).append('|').append(z ? '1' : '0').append('|');
        if (str4 == null) {
            str4 = "";
        }
        return append.append(str4).toString();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private Bitmap renderFrame(String str) {
        int i;
        int i2;
        int i3;
        int i4;
        char c;
        int[] iArr;
        String str2;
        String str3;
        int i5;
        String[] split = str.split("\\|", -1);
        String str4 = split[0];
        int parseInt = Integer.parseInt(split[1]);
        String str5 = split[2];
        String str6 = split[3];
        int i6 = 4;
        int parseInt2 = Integer.parseInt(split[4]);
        boolean equals = "1".equals(split[5]);
        String str7 = split[6].isEmpty() ? null : split[6];
        int i7 = this.unit;
        Bitmap createBitmap = Bitmap.createBitmap(i7 * 18, i7 * 13, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint(1);
        paint.setStyle(Paint.Style.FILL);
        if (parseInt2 > 0) {
            i = 16;
            i3 = 2;
            i2 = 5;
        } else if (parseInt2 < 0) {
            i = 14;
            i3 = 4;
            i2 = 2;
        } else {
            i = 15;
            i2 = 3;
            i3 = 3;
        }
        if ("tuck".equals(str6)) {
            i2++;
            i4 = 11;
        } else {
            i4 = 10;
        }
        int i8 = i4;
        int i9 = i2;
        int i10 = i8;
        str6.hashCode();
        switch (str6.hashCode()) {
            case -1339091217:
                if (str6.equals("dangle")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 3571369:
                if (str6.equals("tuck")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 112895880:
                if (str6.equals("walk1")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 112895881:
                if (str6.equals("walk2")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                iArr = new int[]{3, 3, 3, 3};
                break;
            case 1:
                iArr = new int[]{1, 1, 1, 1};
                break;
            case 2:
                iArr = new int[]{1, 2, 1, 2};
                break;
            case 3:
                iArr = new int[]{2, 1, 2, 1};
                break;
            default:
                iArr = new int[]{2, 2, 2, 2};
                break;
        }
        int[] iArr2 = iArr;
        int i11 = i3 + 3;
        int i12 = i - 4;
        int[] iArr3 = {i3 + 1, i11, i12, i - 2};
        int i13 = 0;
        while (i13 < i6) {
            int i14 = iArr3[i13];
            Paint paint2 = paint;
            int i15 = i10;
            px(canvas, paint2, i14, i15, i14 + 1, i10 + iArr2[i13], this.bodyColor);
            i13++;
            str7 = str7;
            iArr2 = iArr2;
            i6 = i6;
            paint = paint2;
            i10 = i15;
            canvas = canvas;
            createBitmap = createBitmap;
        }
        Paint paint3 = paint;
        int i16 = i10;
        Canvas canvas2 = canvas;
        String str8 = str7;
        Bitmap bitmap = createBitmap;
        int i17 = i6;
        if ("wave1".equals(str5)) {
            str3 = "normal";
            str2 = "up";
        } else if ("wave2".equals(str5)) {
            str2 = "normal";
            str3 = "up";
        } else {
            str2 = str5;
            str3 = str2;
        }
        int i18 = i3;
        int i19 = i;
        arm(canvas2, paint3, -1, i18, i19, i9, str2, this.bodyColor);
        arm(canvas2, paint3, 1, i18, i19, i9, str3, this.bodyColor);
        px(canvas2, paint3, i3, i9, i19, i16, this.bodyColor);
        int i20 = i9 + 2;
        int[] iArr4 = {-1, 1};
        int[] iArr5 = {i11 + parseInt, i12 + parseInt};
        int i21 = 0;
        for (int i22 = 2; i21 < i22; i22 = 2) {
            int i23 = iArr4[i21];
            int i24 = iArr5[i21];
            str4.hashCode();
            switch (str4.hashCode()) {
                case 92961185:
                    if (str4.equals("angry")) {
                        i5 = 0;
                        break;
                    }
                    i5 = -1;
                    break;
                case 93826908:
                    if (str4.equals("blink")) {
                        i5 = 1;
                        break;
                    }
                    i5 = -1;
                    break;
                case 95601300:
                    if (str4.equals("dizzy")) {
                        i5 = 2;
                        break;
                    }
                    i5 = -1;
                    break;
                case 99047136:
                    if (str4.equals("happy")) {
                        i5 = 3;
                        break;
                    }
                    i5 = -1;
                    break;
                case 109522647:
                    if (str4.equals("sleep")) {
                        i5 = i17;
                        break;
                    }
                    i5 = -1;
                    break;
                case 1757705883:
                    if (str4.equals("surprised")) {
                        i5 = 5;
                        break;
                    }
                    i5 = -1;
                    break;
                default:
                    i5 = -1;
                    break;
            }
            switch (i5) {
                case 0:
                    int i25 = i24 + 1;
                    px(canvas2, paint3, i24, i20, i25, i9 + 4, this.eyeColor);
                    int i26 = i23 < 0 ? i24 - 1 : i25;
                    px(canvas2, paint3, i26, i9 + 1, i26 + 1, i20, this.eyeColor);
                    break;
                case 1:
                    px(canvas2, paint3, i24, i9 + 3, i24 + 1, i9 + 4, this.eyeColor);
                    break;
                case 2:
                    int[][] iArr6 = new int[5][];
                    char c2 = 65535;
                    char c3 = 0;
                    iArr6[0] = new int[]{-1, -1};
                    char c4 = 1;
                    iArr6[1] = new int[]{1, -1};
                    iArr6[2] = new int[]{0, 0};
                    iArr6[3] = new int[]{-1, 1};
                    iArr6[i17] = new int[]{1, 1};
                    int i27 = 0;
                    while (i27 < 5) {
                        int[] iArr7 = iArr6[i27];
                        int i28 = iArr7[c3];
                        int i29 = iArr7[c4];
                        px(canvas2, paint3, i24 + i28, i20 + i29, i24 + i28 + 1, i29 + i20 + 1, this.eyeColor);
                        i27++;
                        c3 = c3;
                        c2 = c2;
                        iArr6 = iArr6;
                        c4 = 1;
                    }
                    break;
                case 3:
                    int i30 = i9 + 3;
                    int i31 = i9 + 4;
                    px(canvas2, paint3, i24 - 1, i30, i24, i31, this.eyeColor);
                    int i32 = i24 + 1;
                    px(canvas2, paint3, i24, i20, i32, i30, this.eyeColor);
                    px(canvas2, paint3, i32, i30, i24 + 2, i31, this.eyeColor);
                    break;
                case PetData.SHAKE_HITS /* 4 */:
                    px(canvas2, paint3, i24 - 1, i9 + 3, i24 + 2, i9 + 4, this.eyeColor);
                    break;
                case 5:
                    px(canvas2, paint3, i24, i9 + 1, i24 + 1, i9 + 4, this.eyeColor);
                    break;
                default:
                    px(canvas2, paint3, i24, i20, i24 + 1, i9 + 4, this.eyeColor);
                    break;
            }
            if (equals) {
                if (i23 < 0) {
                    px(canvas2, paint3, i24 - 2, i9 + 4, i24, i9 + 5, mixColor(this.bodyColor, -45191, 0.5f));
                } else {
                    px(canvas2, paint3, i24 + 1, i9 + 4, i24 + 3, i9 + 5, mixColor(this.bodyColor, -45191, 0.5f));
                }
            }
            i21++;
        }
        if (str8 != null) {
            int i33 = (i3 + i) / 2;
            int i34 = i9 + 4;
            if ("open".equals(str8)) {
                px(canvas2, paint3, i33 - 1, i34, i33 + 1, Math.max(i9 + 5, Math.min(i9 + 6, i16)), this.eyeColor);
            } else {
                px(canvas2, paint3, i33 - 1, i34, i33 + 1, i9 + 5, this.eyeColor);
            }
        }
        return bitmap;
    }

    private void arm(Canvas canvas, Paint paint, int i, int i2, int i3, int i4, String str, int i5) {
        if ("up".equals(str)) {
            if (i < 0) {
                int i6 = i2 - 1;
                int i7 = i4 + 3;
                px(canvas, paint, i2 - 2, i4 - 1, i6, i7, i5);
                px(canvas, paint, i6, i4 + 2, i2, i7, i5);
                return;
            }
            int i8 = i3 + 1;
            int i9 = i4 + 3;
            px(canvas, paint, i8, i4 - 1, i3 + 2, i9, i5);
            px(canvas, paint, i3, i4 + 2, i8, i9, i5);
            return;
        }
        if ("tuck".equals(str)) {
            if (i < 0) {
                px(canvas, paint, i2 - 1, i4 + 3, i2, i4 + 5, i5);
                return;
            } else {
                px(canvas, paint, i3, i4 + 3, i3 + 1, i4 + 5, i5);
                return;
            }
        }
        if (i < 0) {
            px(canvas, paint, i2 - 2, i4 + 3, i2, i4 + 5, i5);
        } else {
            px(canvas, paint, i3, i4 + 3, i3 + 2, i4 + 5, i5);
        }
    }

    private void px(Canvas canvas, Paint paint, int i, int i2, int i3, int i4, int i5) {
        paint.setColor(i5);
        int i6 = this.unit;
        canvas.drawRect(i * i6, i2 * i6, i3 * i6, i4 * i6, paint);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:38:0x0059, code lost:
    
        if (r19.equals("spark") == false) goto L14;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public android.graphics.Bitmap particle(java.lang.String r19) {
        /*
            Method dump skipped, instructions count: 266
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.frizovdanya.clawd.SpriteRenderer.particle(java.lang.String):android.graphics.Bitmap");
    }

    private static Integer colorFor(String str, char c, int[] iArr) {
        if (!"poop".equals(str)) {
            if (c == 'X') {
                return Integer.valueOf(iArr[0]);
            }
            return null;
        }
        if (c == 'X') {
            return Integer.valueOf(iArr[0]);
        }
        if (c == 'k') {
            return Integer.valueOf(iArr[3]);
        }
        if (c == 'o') {
            return Integer.valueOf(iArr[1]);
        }
        if (c != 'w') {
            return null;
        }
        return Integer.valueOf(iArr[2]);
    }

    public static int mixColor(int i, int i2, float f) {
        int[] iArr = {16, 8, 0};
        int i3 = -16777216;
        for (int i4 = 0; i4 < 3; i4++) {
            int i5 = iArr[i4];
            i3 |= ((int) (((i >> i5) & 255) + ((((i2 >> i5) & 255) - r4) * f))) << i5;
        }
        return i3;
    }
}
