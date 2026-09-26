package com.frizovdanya.clawd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.function.Supplier;

/* loaded from: classes.dex */
public class MainActivity extends Activity {
    private static final int REQ_CAPTURE = 101;
    private static final int REQ_NOTIF = 102;
    private Cfg cfg;
    private LinearLayout list;
    private final Runnable refresher = new Runnable() { // from class: com.frizovdanya.clawd.MainActivity.1
        @Override // java.lang.Runnable
        public void run() {
            MainActivity.this.refresh();
            MainActivity.this.findViewById(android.R.id.content).postDelayed(this, 1500L);
        }
    };
    private TextView statEat;
    private TextView statPets;
    private TextView statPoop;
    private TextView statState;
    private TextView statusA11y;
    private TextView statusCapture;
    private TextView statusNotif;
    private TextView statusOverlay;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.cfg = new Cfg(this);
        buildUi();
        ensureService();
        if (Build.VERSION.SDK_INT < 33 || checkSelfPermission("android.permission.POST_NOTIFICATIONS") == 0) {
            return;
        }
        requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, REQ_NOTIF);
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        refresh();
        ensureService();
        findViewById(android.R.id.content).removeCallbacks(this.refresher);
        findViewById(android.R.id.content).post(this.refresher);
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        findViewById(android.R.id.content).removeCallbacks(this.refresher);
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(-526862);
        LinearLayout linearLayout = new LinearLayout(this);
        this.list = linearLayout;
        linearLayout.setOrientation(1);
        int dp = dp(16);
        this.list.setPadding(dp, dp, dp, dp);
        scrollView.addView(this.list);
        setContentView(scrollView);
        LinearLayout card = card();
        TextView textView = new TextView(this);
        textView.setText("clawd");
        textView.setTextSize(34.0f);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextColor(Color.parseColor("#E8590C"));
        card.addView(textView);
        TextView textView2 = new TextView(this);
        textView2.setText("пиксельный питомец · v1.5");
        textView2.setTextSize(13.0f);
        textView2.setTextColor(-8947849);
        card.addView(textView2);
        TextView textView3 = new TextView(this);
        textView3.setText("Сделал FRIZOVDANYA · Telegram @frizovdanya");
        textView3.setTextSize(13.0f);
        textView3.setTextColor(-6710887);
        textView3.setPadding(0, dp(6), 0, 0);
        card.addView(textView3);
        addCard(card);
        section("Доступы");
        this.statusOverlay = statusRow("Оверлей поверх всех окон", new Supplier() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda20
            @Override // java.util.function.Supplier
            public final Object get() {
                Boolean lambda$buildUi$0;
                lambda$buildUi$0 = MainActivity.this.lambda$buildUi$0();
                return lambda$buildUi$0;
            }
        }, new Runnable() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildUi$1();
            }
        });
        this.statusA11y = statusRow("Служба доступности (чаты и еда)", new Supplier() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda5
            @Override // java.util.function.Supplier
            public final Object get() {
                Boolean lambda$buildUi$2;
                lambda$buildUi$2 = MainActivity.this.lambda$buildUi$2();
                return lambda$buildUi$2;
            }
        }, new Runnable() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildUi$3();
            }
        });
        this.statusNotif = statusRow("Доступ к уведомлениям (входящие)", new Supplier() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda7
            @Override // java.util.function.Supplier
            public final Object get() {
                Boolean lambda$buildUi$4;
                lambda$buildUi$4 = MainActivity.this.lambda$buildUi$4();
                return lambda$buildUi$4;
            }
        }, new Runnable() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildUi$5();
            }
        });
        this.statusCapture = statusRow("Захват экрана (снимок еды)", new Supplier() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda9
            @Override // java.util.function.Supplier
            public final Object get() {
                Boolean valueOf;
                valueOf = Boolean.valueOf(PetService.get() != null && PetService.get().captureActive());
                return valueOf;
            }
        }, new Runnable() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda10
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.askCapture();
            }
        });
        section("Питомец");
        switchRow("Показывать", "show", true);
        selectorRow("Размер", "size", 3, PetData.SIZE_ITEMS, new int[]{0, 1, 2, 3});
        selectorRow("Цвет", "color", 0, PetData.PALETTE_NAMES, new int[]{0, 1, 2, 3, 4, 5, 6});
        selectorRow("Прозрачность", "alpha", 100, new String[]{"100%", "85%", "70%", "50%"}, new int[]{100, 85, 70, 50});
        actionRow("Свой цвет (HEX…)", new Runnable() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda12
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.showColorDialog();
            }
        });
        section("Поведение");
        switchRow("Гравитация (стоит на пузырях и поле ввода)", "gravity", false);
        switchRow("Блуждает по краям экрана", "wander", true);
        switchRow("Реплики в облачке", "phrases", true);
        selectorRow("Засыпает", "sleep", 1, PetData.SLEEP_ITEMS, new int[]{0, 1, 2, 3});
        selectorRow("Активность", "act", 1, new String[]{"Спокойно", "Нормально", "Часто"}, new int[]{0, 1, 2});
        switchRow("Вибро-отклик", "haptics", true);
        switchRow("Ест элементы интерфейса", "eat", true);
        switchRow("Реагирует на сообщения", "tg_events", true);
        switchRow("Танцует под музыку", "dance", true);
        switchRow("Какает после еды", "poop", true);
        switchRow("Трястись от встряски", "shake", true);
        section("Действия");
        actionRow("Вернуть на место", new Runnable() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda13
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildUi$7();
            }
        });
        actionRow("Собрать какашки", new Runnable() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda21
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildUi$8();
            }
        });
        actionRow("Перезапустить питомца", new Runnable() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildUi$9();
            }
        });
        actionRow("Усыпить / Разбудить", new Runnable() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$buildUi$10();
            }
        });
        actionRow("Свои фразы…", new Runnable() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.showPhrasesDialog();
            }
        });
        section("Статистика");
        LinearLayout card2 = card();
        this.statPets = statLine(card2, "Питомцев:");
        this.statEat = statLine(card2, "Съедено:");
        this.statPoop = statLine(card2, "Убрано какашек:");
        this.statState = statLine(card2, "Сейчас:");
        addCard(card2);
        section("О приложении");
        LinearLayout card3 = card();
        TextView textView4 = new TextView(this);
        textView4.setText("clawd — порт плагина \"clawd\" с exteragram\n\nсоздатель плагина: t.me/itskotovski\nсоздатель порта: t.me/frizovdanya");
        textView4.setTextSize(14.0f);
        textView4.setTextColor(-11184811);
        textView4.setAutoLinkMask(1);
        textView4.setLinkTextColor(Color.parseColor("#E8590C"));
        textView4.setLineSpacing(0.0f, 1.25f);
        card3.addView(textView4);
        addCard(card3);
        TextView textView5 = new TextView(this);
        textView5.setText("clawd · FRIZOVDANYA · @frizovdanya");
        textView5.setTextSize(12.0f);
        textView5.setTextColor(-5592406);
        textView5.setGravity(17);
        textView5.setPadding(0, dp(18), 0, dp(24));
        this.list.addView(textView5);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$buildUi$0() {
        return Boolean.valueOf(SettingsUi.overlayGranted(this));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$1() {
        SettingsUi.askOverlay(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$buildUi$2() {
        return Boolean.valueOf(SettingsUi.isAccessibilityEnabled(this));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$3() {
        SettingsUi.openAccessibility(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$buildUi$4() {
        return Boolean.valueOf(SettingsUi.isNotificationAccessEnabled(this));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$5() {
        SettingsUi.openNotificationAccess(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$7() {
        PetService petService = PetService.get();
        if (petService != null) {
            petService.cfgChanged("recenter", true);
            toast("clawd вернулся на место");
        } else {
            toast("Сервис не запущен");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$8() {
        PetService petService = PetService.get();
        if (petService != null) {
            petService.cfgChanged("give_back", true);
            toast("Убрано");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$9() {
        stopService(new Intent(this, (Class<?>) PetService.class));
        finish();
        startActivity(new Intent(this, (Class<?>) MainActivity.class));
        overridePendingTransition(0, 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildUi$10() {
        PetService petService = PetService.get();
        if (petService != null) {
            petService.cfgChanged("toggle_sleep", true);
        } else {
            toast("Сервис не запущен");
        }
    }

    private LinearLayout card() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(-1);
        linearLayout.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.bottomMargin = dp(8);
        linearLayout.setLayoutParams(layoutParams);
        return linearLayout;
    }

    private void addCard(LinearLayout linearLayout) {
        this.list.addView(linearLayout);
    }

    private void section(String str) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(13.0f);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextColor(-1550068);
        textView.setPadding(dp(4), dp(14), 0, dp(4));
        this.list.addView(textView);
    }

    private TextView statLine(LinearLayout linearLayout, String str) {
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(14.0f);
        textView.setTextColor(-12303292);
        TextView textView2 = new TextView(this);
        textView2.setTextSize(14.0f);
        textView2.setTypeface(Typeface.DEFAULT_BOLD);
        textView2.setTextColor(-14540254);
        linearLayout2.addView(textView);
        linearLayout2.addView(textView2);
        linearLayout.addView(linearLayout2);
        return textView2;
    }

    private TextView statusRow(String str, Supplier<Boolean> supplier, final Runnable runnable) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(14.0f);
        textView.setTextColor(-12303292);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textView2 = new TextView(this);
        textView2.setTextSize(13.0f);
        textView2.setTypeface(Typeface.DEFAULT_BOLD);
        textView2.setPadding(dp(8), 0, 0, 0);
        linearLayout.addView(textView);
        linearLayout.addView(textView2);
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda15
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                runnable.run();
            }
        });
        LinearLayout card = card();
        card.addView(linearLayout);
        addCard(card);
        return textView2;
    }

    private void switchRow(String str, final String str2, boolean z) {
        LinearLayout card = card();
        Switch r1 = new Switch(this);
        r1.setText(str);
        r1.setTextSize(14.0f);
        r1.setTextColor(-12303292);
        r1.setChecked(this.cfg.b(str2, z));
        r1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda11
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z2) {
                MainActivity.this.lambda$switchRow$12(str2, compoundButton, z2);
            }
        });
        card.addView(r1);
        addCard(card);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$switchRow$12(String str, CompoundButton compoundButton, boolean z) {
        this.cfg.putBool(str, z);
        PetService petService = PetService.get();
        if (petService != null) {
            petService.cfgChanged(str, Boolean.valueOf(z));
        }
        if ("show".equals(str)) {
            ensureService();
        }
    }

    private void selectorRow(final String str, final String str2, final int i, final String[] strArr, final int[] iArr) {
        LinearLayout card = card();
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(14.0f);
        textView.setTextColor(-12303292);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
        final TextView textView2 = new TextView(this);
        textView2.setTextSize(14.0f);
        textView2.setTypeface(Typeface.DEFAULT_BOLD);
        textView2.setTextColor(-1550068);
        linearLayout.addView(textView);
        linearLayout.addView(textView2);
        this.cfg.i(str2, i);
        final Runnable[] runnableArr = {new Runnable() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda17
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$selectorRow$13(iArr, str2, i, textView2, strArr);
            }
        }};
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda18
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$selectorRow$15(iArr, str2, i, str, strArr, runnableArr, view);
            }
        });
        runnableArr[0].run();
        card.addView(linearLayout);
        addCard(card);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$selectorRow$13(int[] iArr, String str, int i, TextView textView, String[] strArr) {
        int i2 = 0;
        for (int i3 = 0; i3 < iArr.length; i3++) {
            if (iArr[i3] == this.cfg.i(str, i)) {
                i2 = i3;
            }
        }
        textView.setText(strArr[i2]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$selectorRow$15(final int[] iArr, final String str, int i, String str2, String[] strArr, final Runnable[] runnableArr, View view) {
        int i2 = 0;
        for (int i3 = 0; i3 < iArr.length; i3++) {
            if (iArr[i3] == this.cfg.i(str, i)) {
                i2 = i3;
            }
        }
        new AlertDialog.Builder(this).setTitle(str2).setSingleChoiceItems(strArr, i2, new DialogInterface.OnClickListener() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda19
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i4) {
                MainActivity.this.lambda$selectorRow$14(str, iArr, runnableArr, dialogInterface, i4);
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$selectorRow$14(String str, int[] iArr, Runnable[] runnableArr, DialogInterface dialogInterface, int i) {
        this.cfg.putInt(str, iArr[i]);
        PetService petService = PetService.get();
        if (petService != null) {
            petService.cfgChanged(str, Integer.valueOf(iArr[i]));
        }
        runnableArr[0].run();
        dialogInterface.dismiss();
    }

    private void actionRow(String str, final Runnable runnable) {
        LinearLayout card = card();
        Button button = new Button(this);
        button.setText(str);
        button.setTextSize(14.0f);
        button.setTextColor(-1);
        button.setBackgroundColor(Color.parseColor("#E8590C"));
        button.setAllCaps(false);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda14
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                runnable.run();
            }
        });
        card.addView(button);
        addCard(card);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refresh() {
        int parseColor;
        int parseColor2;
        int parseColor3;
        this.statusOverlay.setText(SettingsUi.overlayGranted(this) ? "✓ выдан" : "→ выдать");
        this.statusOverlay.setTextColor(SettingsUi.overlayGranted(this) ? Color.parseColor("#2E9E5B") : Color.parseColor("#E8590C"));
        boolean isAccessibilityEnabled = SettingsUi.isAccessibilityEnabled(this);
        this.statusA11y.setText(isAccessibilityEnabled ? "✓ включена" : "→ включить");
        TextView textView = this.statusA11y;
        if (isAccessibilityEnabled) {
            parseColor = Color.parseColor("#2E9E5B");
        } else {
            parseColor = Color.parseColor("#E8590C");
        }
        textView.setTextColor(parseColor);
        boolean isNotificationAccessEnabled = SettingsUi.isNotificationAccessEnabled(this);
        this.statusNotif.setText(isNotificationAccessEnabled ? "✓ выдан" : "→ выдать");
        TextView textView2 = this.statusNotif;
        if (isNotificationAccessEnabled) {
            parseColor2 = Color.parseColor("#2E9E5B");
        } else {
            parseColor2 = Color.parseColor("#E8590C");
        }
        textView2.setTextColor(parseColor2);
        boolean z = PetService.get() != null && PetService.get().captureActive();
        this.statusCapture.setText(z ? "✓ активен" : "→ включить");
        TextView textView3 = this.statusCapture;
        if (z) {
            parseColor3 = Color.parseColor("#2E9E5B");
        } else {
            parseColor3 = Color.parseColor("#E8590C");
        }
        textView3.setTextColor(parseColor3);
        this.statPets.setText(String.valueOf(this.cfg.i("pets_count", 0)));
        this.statEat.setText(String.valueOf(this.cfg.i("eaten_count", 0)));
        this.statPoop.setText(String.valueOf(this.cfg.i("poops_cleaned", 0)));
        PetService petService = PetService.get();
        this.statState.setText(petService != null ? petService.petState() : "—");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showColorDialog() {
        final EditText editText = new EditText(this);
        editText.setHint("#E8590C");
        String s = this.cfg.s("custom_color", "#E8590C");
        editText.setText(s);
        editText.setSelection(s.length());
        new AlertDialog.Builder(this).setTitle("Свой цвет тела").setMessage("HEX-код #RRGGBB. Цвет применяется сразу, палитра «Свой» запоминается.").setView(editText).setPositiveButton("Сохранить", new DialogInterface.OnClickListener() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$showColorDialog$17(editText, dialogInterface, i);
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showColorDialog$17(EditText editText, DialogInterface dialogInterface, int i) {
        String trim = editText.getText().toString().trim();
        if (!PetData.applyCustom(trim)) {
            toast("Неверный формат — нужно #RRGGBB");
            return;
        }
        this.cfg.putStr("custom_color", trim);
        this.cfg.putInt("color", 6);
        PetService petService = PetService.get();
        if (petService != null) {
            petService.cfgChanged("custom_color", trim);
        }
        toast("Свой цвет применён");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showPhrasesDialog() {
        final EditText editText = new EditText(this);
        editText.setMinLines(4);
        editText.setGravity(48);
        editText.setInputType(131073);
        String s = this.cfg.s("my_phrases", "");
        editText.setText(s);
        if (!s.isEmpty()) {
            editText.setSelection(s.length());
        }
        new AlertDialog.Builder(this).setTitle("Свои фразы").setMessage("По одной фразе в строке. Они будут случайно появляться в облачке среди обычных реплик.").setView(editText).setPositiveButton("Сохранить", new DialogInterface.OnClickListener() { // from class: com.frizovdanya.clawd.MainActivity$$ExternalSyntheticLambda16
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$showPhrasesDialog$18(editText, dialogInterface, i);
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPhrasesDialog$18(EditText editText, DialogInterface dialogInterface, int i) {
        this.cfg.putStr("my_phrases", editText.getText().toString());
        toast("Сохранено");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void askCapture() {
        try {
            startActivityForResult(((MediaProjectionManager) getSystemService("media_projection")).createScreenCaptureIntent(), REQ_CAPTURE);
        } catch (Exception unused) {
            toast("Захват экрана недоступен");
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == REQ_CAPTURE && i2 == -1 && intent != null) {
            ensureService();
            startForegroundService(new Intent(this, (Class<?>) PetService.class).setAction(PetService.ACTION_FROM_ACTIVITY).putExtra(PetService.EXTRA_COMMAND, PetService.CMD_CAPTURE).putExtra(PetService.EXTRA_CAPTURE_RESULT, intent));
            toast("Захват экрана включён");
        }
    }

    private void ensureService() {
        if (SettingsUi.overlayGranted(this) && this.cfg.b("show", true)) {
            try {
                startForegroundService(new Intent(this, (Class<?>) PetService.class));
            } catch (Exception e) {
                toast("Не удалось запустить сервис: " + e.getMessage());
            }
        }
    }

    private void toast(String str) {
        Toast.makeText(this, str, 0).show();
    }

    private int dp(int i) {
        return Math.max(1, (int) ((i * getResources().getDisplayMetrics().density) + 0.5f));
    }

    static int iconColor(Context context) {
        return new Cfg(context).i("color", 0);
    }
}
