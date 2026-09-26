package com.frizovdanya.clawd;

import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public final class PetData {
    public static final float EARTH_G = 9.81f;
    public static final float EAT_COOLDOWN_MAX = 120.0f;
    public static final float EAT_COOLDOWN_MIN = 40.0f;
    public static final int EAT_SCAN_LIMIT = 900;
    public static final int GRID_H = 13;
    public static final int GRID_W = 18;
    public static final int HEART_COLOR = -41862;
    public static final int MAX_CHAT_VISITS = 200;
    public static final int MAX_EATEN = 5;
    public static final int MAX_FRAME_CACHE = 48;
    public static final int MAX_KEYWORD_TEXT = 400;
    public static final int MAX_PARTICLES = 14;
    public static final int MAX_POOPS = 3;
    public static final int MAX_SCAN_MESSAGES = 10;
    public static final int NOTE_COLOR = -9455361;
    public static final Map<String, String[]> PATTERNS;
    public static final float PEEK_AFTER = 45.0f;
    public static final int PEEK_HIDDEN_UNITS = 3;
    public static final int PEEK_OUT_UNITS = 7;
    public static final Map<String, String[]> PHRASES;
    public static final Map<String, int[]> POOP_COLORS;
    public static final int POOP_EVERY_MAX = 5;
    public static final int POOP_EVERY_MIN = 3;
    public static final int SHAKE_HITS = 4;
    public static final float SHAKE_JERK = 14.0f;
    public static final float SHAKE_WINDOW = 0.9f;
    public static final int SPARK_COLOR = -14003;
    public static final float TILT_DEAD = 0.22f;
    public static final int ZZZ_COLOR = -7363649;
    public static final String[] SIZE_ITEMS = {"Маленький", "Средний", "Большой", "Огромный"};
    public static final float[] SIZE_DP = {3.0f, 4.0f, 5.0f, 7.0f};
    public static final String[] PALETTE_NAMES = {"Claude", "Мятный", "Лавандовый", "Небесный", "Розовый", "Графит", "Свой"};
    public static final int[] PALETTE_BODY = {-2525353, -11550824, -6583570, -11556632, -1145426, -10590608, -2525353};
    public static final int[] PALETTE_EYE = {-15066598, -15588323, -15002063, -15852503, -14020064, -855310, -15066598};
    public static final String[] SLEEP_ITEMS = {"Через 1 минуту", "Через 3 минуты", "Через 10 минут", "Никогда"};
    public static final int[] SLEEP_SECONDS = {60, 180, 600, 0};

    private PetData() {
    }

    static {
        HashMap hashMap = new HashMap();
        PATTERNS = hashMap;
        HashMap hashMap2 = new HashMap();
        POOP_COLORS = hashMap2;
        hashMap.put("heart", new String[]{".XX.XX.", "XXXXXXX", "XXXXXXX", ".XXXXX.", "..XXX..", "...X..."});
        hashMap.put("zzz", new String[]{"XXXXX", "...X.", "..X..", ".X...", "XXXXX"});
        hashMap.put("spark", new String[]{"..X..", "..X..", "XX.XX", "..X..", "..X.."});
        hashMap.put("note", new String[]{"..XXXXX", "..X...X", "..X...X", "..X...X", "XXX.XXX", "XXX.XXX"});
        hashMap.put("poop", new String[]{"....X....", "...XXo...", "..XXXXX..", ".XXXXXXo.", ".XwkXwkX.", "XXXXXXXXo", "XXXXXXXXX"});
        hashMap2.put("poop", new int[]{-7710157, -5079730, -1, -15066598});
        HashMap hashMap3 = new HashMap();
        PHRASES = hashMap3;
        hashMap3.put("hello", new String[]{"Привет!", "Я Clawd!", "О, это ты!"});
        hashMap3.put("idle", new String[]{"Чем помочь?", "Хм…", "Думаю…", "Можно я тут посижу?", "Ты молодец!", "Не забудь попить воды", "Пип-пип!", "Мне нравится этот чат", "*оглядывается*", "Пишу код… шучу", "Интересно, что там в чатах"});
        hashMap3.put("tap", new String[]{"Ой!", "Эй!", "Чего?", "Я тут!", "Хи!"});
        hashMap3.put("annoyed", new String[]{"Хватит тыкать!", "Щекотно же!", "Ну всё, обиделся", "Эй, полегче!"});
        hashMap3.put("pet", new String[]{"Мрр…", "Ещё!", "Приятно ♥", "Хи-хи", "Лучший день!", "♥♥♥"});
        hashMap3.put("drag", new String[]{"Ааа!", "Куда мы?", "Поставь меня!"});
        hashMap3.put("throw", new String[]{"Уиии!", "Я лечу!", "Ваааа!"});
        hashMap3.put("dizzy", new String[]{"Голова кружится…", "@_@", "Где я?"});
        hashMap3.put("wake", new String[]{"А? Что?", "Я не спал!", "*зевает*"});
        hashMap3.put("jump", new String[]{"Хоп!", "Йе!", "Ура!"});
        hashMap3.put("eat", new String[]{"Ням!", "Хрум-хрум", "Вкусная кнопка", "*жуёт*", "Ммм, пиксели", "Это было лишнее", "Добавки!"});
        hashMap3.put("spit", new String[]{"Ладно, отдаю!", "Тьфу!", "Ой, простите", "Забирай…", "Не бей!", "Оно само!"});
        hashMap3.put("mail", new String[]{"Тебе пишут!", "Сообщение!", "Кто-то написал!", "Динь-динь!", "Смотри, новенькое!"});
        hashMap3.put("sent", new String[]{"Отправлено!", "Улетело!", "Хорошо сказано!", "Пиу!", "Жду ответа!"});
        hashMap3.put("essay", new String[]{"Ого, целое эссе", "Это роман?", "Много букв!", "Я не дочитал…"});
        hashMap3.put("love", new String[]{"Ааа, мило ♥", "♥♥♥", "Как трогательно!", "Любовь!"});
        hashMap3.put("laugh", new String[]{"Ахаха!", "Хи-хи-хи", "Смешно!", "Ха-ха!"});
        hashMap3.put("hi", new String[]{"Привет-привет!", "И тебе привет!", "Здрасьте!", "*машет*"});
        hashMap3.put("music", new String[]{"О, музыка!", "Танцуем!", "Это мой трек!", "♪♪♪"});
        hashMap3.put("listen", new String[]{"Слушаю…", "Кто это говорит?", "*внимательно слушает*"});
        hashMap3.put("music_end", new String[]{"Эх, закончилось", "Ещё!", "Хороший трек"});
        hashMap3.put("chat_again", new String[]{"О, опять этот чат", "Снова сюда?", "Опять {name}?", "Мы тут уже были"});
        hashMap3.put("chat_new", new String[]{"О, {name}!", "Это же {name}!", "Что пишет {name}?", "Привет, {name}!"});
        hashMap3.put("chat_self", new String[]{"Твои заметки!", "Избранное, уютно", "Что сохраним?"});
        hashMap3.put("shake", new String[]{"Аааа!", "Землетрясение!", "Хватит трясти!", "Уоооу!"});
        hashMap3.put("slide", new String[]{"Скользко!", "Уиии~", "Меня несёт!"});
        hashMap3.put("hang", new String[]{"Держусь!", "Помогите!", "Высоковато…", "Не отпущу!"});
        hashMap3.put("hang_fall", new String[]{"Ой-ой-ой!", "Не удержался!", "Падаю!"});
        hashMap3.put("peek", new String[]{"Ку-ку!", "Я тебя вижу", "*подглядывает*", "Ты тут?"});
        hashMap3.put("found", new String[]{"Нашёл меня!", "Эх, раскусил", "Я просто смотрел!"});
        hashMap3.put("poop", new String[]{"Упс…", "Это не я!", "Ой…", "Кнопки были лишними", "*краснеет*"});
        hashMap3.put("clean", new String[]{"Спасибо!", "Прости…", "Больше не буду!", "Ты лучший"});
    }

    public static boolean applyCustom(String str) {
        if (str == null) {
            return false;
        }
        String trim = str.trim();
        if (trim.startsWith("#")) {
            trim = trim.substring(1);
        }
        if (trim.startsWith("0x") || trim.startsWith("0X")) {
            trim = trim.substring(2);
        }
        if (trim.length() != 6) {
            return false;
        }
        try {
            int parseLong = (int) (Long.parseLong(trim, 16) | (-16777216));
            PALETTE_BODY[6] = parseLong;
            int max = Math.max(20, (((parseLong >> 16) & 255) * 3) / 8);
            int max2 = Math.max(20, (((parseLong >> 8) & 255) * 3) / 8);
            PALETTE_EYE[6] = Math.max(20, ((parseLong & 255) * 3) / 8) | (max << 16) | (-16777216) | (max2 << 8);
            return true;
        } catch (Exception unused) {
            return false;
        }
    }
}
