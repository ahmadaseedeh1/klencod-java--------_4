package com.example.adhkar; // تأكد أن هذا السطر يطابق package مشروعك

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    // عناصر الواجهة البرمجية
    private TextView tvStepBadge, tvDhikrTitle, tvDhikrText, tvDhikrNote, tvCounterDisplay;
    private Button btnCounter, btnPrev, btnNext, btnReset;
    private ProgressBar progressBar;

    // نموذج بيانات الذكر
    static class Dhikr {
        String title;
        String text;
        String note;
        int targetCount;

        Dhikr(String title, String text, String note, int targetCount) {
            this.title = title;
            this.text = text;
            this.note = note;
            this.targetCount = targetCount;
        }
    }

    private List<Dhikr> adhkarList;
    private int currentIndex = 0;
    private int currentCount = 0;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. تهيئة خدمة الاهتزاز
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // 2. بناء الواجهة برمجياً
        View rootView = buildUserInterface();
        setContentView(rootView);

        // 3. تجهيز بيانات الأذكار
        initAdhkarList();

        // 4. ربط الأوامر بالأزرار
        btnCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCountClick();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextDhikr();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevDhikr();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAll();
            }
        });

        // 5. تحديث الشاشة
        updateUI();
    }

    // بناء الواجهة كاملة بأكواد Java نقية
    private View buildUserInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F172A"));
        root.setPadding(dp(16), dp(16), dp(16), dp(16));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        // العنوان العلوي
        TextView tvAppTitle = new TextView(this);
        tvAppTitle.setText("أذكار ما بعد الصلاة");
        tvAppTitle.setTextColor(Color.parseColor("#10B981"));
        tvAppTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        tvAppTitle.setTypeface(null, Typeface.BOLD);
        tvAppTitle.setGravity(Gravity.CENTER);
        root.addView(tvAppTitle);

        // شريط التقدم
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
        pbParams.setMargins(0, dp(14), 0, dp(14));
        progressBar.setLayoutParams(pbParams);
        progressBar.setMax(100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#10B981")));
        }
        root.addView(progressBar);

        // حاوية التمرير
        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        scrollView.setLayoutParams(scrollParams);

        // بطاقة الذكر
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#1E293B"));
        cardBg.setCornerRadius(dp(16));
        card.setBackground(cardBg);

        tvStepBadge = new TextView(this);
        tvStepBadge.setTextColor(Color.parseColor("#10B981"));
        tvStepBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvStepBadge.setTypeface(null, Typeface.BOLD);
        card.addView(tvStepBadge);

        tvDhikrTitle = new TextView(this);
        tvDhikrTitle.setTextColor(Color.parseColor("#F59E0B"));
        tvDhikrTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvDhikrTitle.setTypeface(null, Typeface.BOLD);
        tvDhikrTitle.setPadding(0, dp(8), 0, dp(8));
        card.addView(tvDhikrTitle);

        tvDhikrText = new TextView(this);
        tvDhikrText.setTextColor(Color.WHITE);
        tvDhikrText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        tvDhikrText.setLineSpacing(dp(8), 1.0f);
        card.addView(tvDhikrText);

        tvDhikrNote = new TextView(this);
        tvDhikrNote.setTextColor(Color.parseColor("#94A3B8"));
        tvDhikrNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvDhikrNote.setPadding(0, dp(14), 0, 0);
        card.addView(tvDhikrNote);

        scrollView.addView(card);
        root.addView(scrollView);

        // زر التسبيح والعد
        btnCounter = new Button(this);
        LinearLayout.LayoutParams counterParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(60));
        counterParams.setMargins(0, dp(14), 0, 0);
        btnCounter.setLayoutParams(counterParams);
        btnCounter.setText("اضغط للتسبيح");
        btnCounter.setTextColor(Color.WHITE);
        btnCounter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        btnCounter.setTypeface(null, Typeface.BOLD);
        GradientDrawable btnCounterBg = new GradientDrawable();
        btnCounterBg.setColor(Color.parseColor("#10B981"));
        btnCounterBg.setCornerRadius(dp(14));
        btnCounter.setBackground(btnCounterBg);
        root.addView(btnCounter);

        // رقم العداد
        tvCounterDisplay = new TextView(this);
        tvCounterDisplay.setTextColor(Color.WHITE);
        tvCounterDisplay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvCounterDisplay.setTypeface(null, Typeface.BOLD);
        tvCounterDisplay.setGravity(Gravity.CENTER);
        tvCounterDisplay.setPadding(0, dp(6), 0, dp(8));
        root.addView(tvCounterDisplay);

        // حاوية أزرار التنقل
        LinearLayout navLayout = new LinearLayout(this);
        navLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams navParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        navLayout.setLayoutParams(navParams);

        GradientDrawable navBtnBg = new GradientDrawable();
        navBtnBg.setColor(Color.parseColor("#334155"));
        navBtnBg.setCornerRadius(dp(10));

        btnPrev = new Button(this);
        btnPrev.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        btnPrev.setText("السابق");
        btnPrev.setTextColor(Color.parseColor("#CBD5E1"));
        btnPrev.setBackground(navBtnBg);

        Space space = new Space(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(dp(12), ViewGroup.LayoutParams.WRAP_CONTENT));

        btnNext = new Button(this);
        btnNext.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        btnNext.setText("تخطي");
        btnNext.setTextColor(Color.parseColor("#CBD5E1"));
        btnNext.setBackground(navBtnBg.getConstantState().newDrawable());

        navLayout.addView(btnPrev);
        navLayout.addView(space);
        navLayout.addView(btnNext);
        root.addView(navLayout);

        // زر إعادة البدء
        btnReset = new Button(this, null, android.R.attr.borderlessButtonStyle);
        btnReset.setText("إعادة البدء");
        btnReset.setTextColor(Color.parseColor("#EF4444"));
        btnReset.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resetParams.gravity = Gravity.CENTER;
        btnReset.setLayoutParams(resetParams);
        root.addView(btnReset);

        return root;
    }

    private void initAdhkarList() {
        adhkarList = new ArrayList<>();

        adhkarList.add(new Dhikr(
                "الاستغفار والسلام",
                "أَسْتَغْفِرُ اللهَ، أَسْتَغْفِرُ اللهَ، أَسْتَغْفِرُ اللهَ..\nاللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ، تَبَارَكْتَ يَا ذَا الْجَلاَلِ وَالإِكْرَامِ.",
                "يُقال مباشرة بعد التسليم من الصلاة.",
                3
        ));

        adhkarList.add(new Dhikr(
                "التهليل والتوحيد (1)",
                "لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لاَ مَانِعَ لِمَا أَعْطَيْتَ، وَلاَ مُعْطِيَ لِمَا مَنَعْتَ، وَلاَ يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ.",
                "رواه البخاري ومسلم.",
                1
        ));

        adhkarList.add(new Dhikr(
                "التهليل والتوحيد (2)",
                "لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، لاَ حَوْلَ وَلاَ قُوَّةَ إِلاَّ بِاللَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَلاَ نَعْبُدُ إِلاَّ إِيَّاهُ، لَهُ النِّعْمَةُ وَلَهُ الْفَضْلُ وَلَهُ الثَّنَاءُ الْحَسَنُ، لاَ إِلَهَ إِلاَّ اللَّهُ مُخْلِصِينَ لَهُ الدِّينَ وَلَوْ كَرِهَ الْكَافِرُونَ.",
                "رواه مسلم.",
                1
        ));

        adhkarList.add(new Dhikr("التسبيح", "سُبْحَانَ اللهِ", "التسبيح 33 مرة.", 33));
        adhkarList.add(new Dhikr("التحميد", "الْحَمْدُ لِلَّهِ", "التحميد 33 مرة.", 33));
        adhkarList.add(new Dhikr("التكبير", "اللَّهُ أَكْبَرُ", "التكبير 33 مرة.", 33));

        adhkarList.add(new Dhikr(
                "تمام المائة",
                "لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
                "من قالها غُفرت خطاياه وإن كانت مثل زبد البحر.",
                1
        ));

        adhkarList.add(new Dhikr(
                "آية الكرسي",
                "﴿اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ﴾",
                "لم يمنعه من دخول الجنة إلا أن يموت.",
                1
        ));

        adhkarList.add(new Dhikr(
                "المعوذات والدعاء",
                "قراءة سورة الإخلاص، والفلق، والناس.\n\n«اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ».",
                "تقرأ المعوذات مرة بعد كل صلاة (وثلاثاً بعد الفجر والمغرب).",
                1
        ));
    }

    private void handleCountClick() {
        vibrate(35);
        currentCount++;
        Dhikr item = adhkarList.get(currentIndex);

        if (currentCount >= item.targetCount) {
            vibrate(150);
            if (currentIndex < adhkarList.size() - 1) {
                currentIndex++;
                currentCount = 0;
            } else {
                showFinishedDialog();
                return;
            }
        }
        updateUI();
    }

    private void nextDhikr() {
        if (currentIndex < adhkarList.size() - 1) {
            currentIndex++;
            currentCount = 0;
            updateUI();
        } else {
            showFinishedDialog();
        }
    }

    private void prevDhikr() {
        if (currentIndex > 0) {
            currentIndex--;
            currentCount = 0;
            updateUI();
        }
    }

    private void resetAll() {
        currentIndex = 0;
        currentCount = 0;
        updateUI();
        Toast.makeText(this, "تمت إعادة البدء من الأول", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        Dhikr item = adhkarList.get(currentIndex);

        tvStepBadge.setText(String.format("الذكر %d من %d", (currentIndex + 1), adhkarList.size()));
        tvDhikrTitle.setText(item.title);
        tvDhikrText.setText(item.text);
        tvDhikrNote.setText(item.note);
        tvCounterDisplay.setText(String.format("%d / %d", currentCount, item.targetCount));

        btnPrev.setEnabled(currentIndex > 0);

        int progress = (int) (((float) currentIndex / adhkarList.size()) * 100);
        progressBar.setProgress(progress);
    }

    private void showFinishedDialog() {
        progressBar.setProgress(100);
        new AlertDialog.Builder(this)
                .setTitle("تقبّل الله طاعتكم")
                .setMessage("لقد أتممت أذكار ما بعد الصلاة المفروضة كاملة بنجاح.")
                .setPositiveButton("إعادة البدء", (dialog, which) -> resetAll())
                .setCancelable(false)
                .show();
    }

    private void vibrate(long ms) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}