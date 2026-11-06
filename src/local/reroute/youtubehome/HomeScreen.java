package local.reroute.youtubehome;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The whole Home composition, independent of the Activity so it can be rendered offscreen for
 * review. Information band on top (time on the right, Tangier weather on the left), open sky in
 * the middle, and a single YouTube action resting on the horizon.
 */
final class HomeScreen extends FrameLayout {
    static final int INK=0xff10202f, INK_SOFT=0xff415468, IVORY=0xfff7f2e8, FOCUS=0xfff2b544;
    static final int ACTION_ID=1001;
    final SkyView sky;
    final LinearLayout action;
    private final WeatherGlyph glyph;
    private final TextView clock,greeting,dayName,dayDate,temp,conditions,weatherStatus,actionTitle,actionHint,okBadge;
    private final LinearLayout tempRow;
    private final ImageView icon;
    private final float textScale;

    HomeScreen(Context context){
        super(context);
        textScale=Math.max(1f,Math.min(1.3f,getResources().getConfiguration().fontScale));
        setLayoutDirection(LAYOUT_DIRECTION_RTL);
        sky=new SkyView(context);addView(sky,new LayoutParams(-1,-1));

        // Safe area: about 5% overscan on each side of a 1280x720 television.
        FrameLayout safe=new FrameLayout(context);safe.setPadding(dp(56),dp(34),dp(56),dp(30));
        addView(safe,new LayoutParams(-1,-1));

        LinearLayout band=new LinearLayout(context);band.setOrientation(LinearLayout.HORIZONTAL);band.setGravity(Gravity.TOP);
        safe.addView(band,new LayoutParams(-1,-2,Gravity.TOP));

        // Time lockup: clock | divider | greeting, weekday, date.
        LinearLayout time=new LinearLayout(context);time.setOrientation(LinearLayout.HORIZONTAL);time.setGravity(Gravity.CENTER_VERTICAL);
        band.addView(time,new LinearLayout.LayoutParams(-2,-2));
        clock=text("",0,Model.TEXT);clock.setTextSize(TypedValue.COMPLEX_UNIT_PX,dp(120));
        clock.setTypeface(Typeface.create("sans-serif-light",Typeface.NORMAL));clock.setIncludeFontPadding(false);
        clock.setTextDirection(TEXT_DIRECTION_LTR);clock.setSingleLine(true);clock.setLetterSpacing(-0.02f);
        time.addView(clock,new LinearLayout.LayoutParams(-2,-2));
        View rule=new View(context);rule.setBackgroundColor(0x55f7f6f1);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(dp(2),dp(92));rp.setMarginStart(dp(26));rp.setMarginEnd(dp(24));
        time.addView(rule,rp);
        LinearLayout calendar=new LinearLayout(context);calendar.setOrientation(LinearLayout.VERTICAL);
        time.addView(calendar,new LinearLayout.LayoutParams(-2,-2));
        greeting=text("",20,Model.TEXT_MUTED);calendar.addView(greeting,wrap());
        dayName=text("",36,Model.TEXT);dayName.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));calendar.addView(dayName,wrap());
        dayDate=text("",25,Model.TEXT);calendar.addView(dayDate,wrap());

        View space=new View(context);band.addView(space,new LinearLayout.LayoutParams(0,1,1));

        // Weather corner, flush with the left safe edge.
        LinearLayout weather=new LinearLayout(context);weather.setOrientation(LinearLayout.VERTICAL);weather.setGravity(Gravity.LEFT);
        band.addView(weather,new LinearLayout.LayoutParams(-2,-2));
        TextView city=text("طنجة",20,Model.TEXT_MUTED);city.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));city.setGravity(Gravity.LEFT);weather.addView(city,wrap());
        tempRow=new LinearLayout(context);tempRow.setOrientation(LinearLayout.HORIZONTAL);tempRow.setGravity(Gravity.CENTER_VERTICAL);
        weather.addView(tempRow,new LinearLayout.LayoutParams(-2,-2));
        temp=text("",58,Model.TEXT);temp.setTypeface(Typeface.create("sans-serif-light",Typeface.NORMAL));temp.setTextDirection(TEXT_DIRECTION_LTR);temp.setIncludeFontPadding(false);
        tempRow.addView(temp);
        glyph=new WeatherGlyph(context);
        LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(dp(66),dp(66));gp.setMarginStart(dp(12));tempRow.addView(glyph,gp);
        conditions=text("",23,Model.TEXT);conditions.setGravity(Gravity.LEFT);weather.addView(conditions,wrap());
        weatherStatus=text("",13,Model.TEXT_MUTED);weatherStatus.setGravity(Gravity.LEFT);weatherStatus.setPadding(0,dp(6),0,0);weatherStatus.setAlpha(0.9f);weather.addView(weatherStatus,wrap());

        // The one action.
        action=new LinearLayout(context);action.setId(ACTION_ID);action.setOrientation(LinearLayout.HORIZONTAL);action.setGravity(Gravity.CENTER_VERTICAL);
        action.setFocusable(true);action.setFocusableInTouchMode(true);action.setClickable(true);
        action.setNextFocusLeftId(ACTION_ID);action.setNextFocusRightId(ACTION_ID);action.setNextFocusUpId(ACTION_ID);action.setNextFocusDownId(ACTION_ID);
        LayoutParams ap=new LayoutParams(dp(540),-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        safe.addView(action,ap);
        icon=new ImageView(context);icon.setScaleType(ImageView.ScaleType.FIT_CENTER);icon.setImageDrawable(new PlayMark());
        icon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        action.addView(icon,new LinearLayout.LayoutParams(dp(78),dp(78)));
        LinearLayout copy=new LinearLayout(context);copy.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);cp.setMarginStart(dp(22));cp.setMarginEnd(dp(16));action.addView(copy,cp);
        actionTitle=text("يوتيوب",40,INK);actionTitle.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));actionTitle.setShadowLayer(0,0,0,0);copy.addView(actionTitle);
        actionHint=text("اضغطي للمشاهدة",19,INK_SOFT);actionHint.setShadowLayer(0,0,0,0);copy.addView(actionHint);
        okBadge=text("OK",17,INK);okBadge.setTextDirection(TEXT_DIRECTION_LTR);okBadge.setGravity(Gravity.CENTER);
        okBadge.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));okBadge.setShadowLayer(0,0,0,0);okBadge.setIncludeFontPadding(false);
        int badge=Math.max(dp(56),Math.round(okBadge.getTextSize()*2.3f));
        action.addView(okBadge,new LinearLayout.LayoutParams(badge,badge));
        action.setContentDescription("يوتيوب. اضغطي على زر OK للمشاهدة");
        action.setOnFocusChangeListener(new OnFocusChangeListener(){@Override public void onFocusChange(View v,boolean f){styleAction(f);}});
        styleAction(false);
    }

    /** Preview-only: draw the focused state without a window. */
    void showFocused(){styleAction(true);}
    void setYouTubeIcon(Drawable drawable){if(drawable!=null)icon.setImageDrawable(drawable);}

    /** Bind wall-clock time and the current weather knowledge. */
    void bind(long now,WeatherClient.Snapshot weather,int failures){
        boolean clockOk=Model.clockValid(now);
        if(clockOk){
            clock.setText(Model.clock(now));clock.setTextColor(Model.TEXT);
            greeting.setText(Model.greeting(now));dayName.setText(Model.dayName(now));
            dayDate.setText(Model.dayMonthYear(now));dayDate.setVisibility(VISIBLE);
        }else{
            clock.setText("--:--");clock.setTextColor(Model.TEXT_MUTED);
            greeting.setText("أهلاً بك");dayName.setText("جارٍ ضبط الساعة…");dayDate.setVisibility(GONE);
        }
        boolean hasFresh=weather!=null&&Model.fresh(weather.stamp,now);
        int state=Model.weatherState(hasFresh,failures,clockOk);
        long rise=hasFresh?weather.sunrise:0,set=hasFresh?weather.sunset:0;
        String phase=Model.phase(now,rise,set);
        float progress=Model.arcProgress(now,rise,set);
        if(state==Model.WEATHER_FRESH){
            String kind=Model.weatherKind(weather.code);float cover=Model.cloudiness(weather.code);
            sky.update(phase,kind,cover,progress);
            boolean daylight=!phase.equals("night");
            glyph.set(kind,daylight,cover);glyph.setVisibility(kind.equals("unknown")?GONE:VISIBLE);
            temp.setText(String.format(Locale.US,"%d°",Math.round(weather.temperature)));tempRow.setVisibility(VISIBLE);
            conditions.setText(Model.weatherLabel(weather.code,daylight));
            SimpleDateFormat f=new SimpleDateFormat("HH:mm",Locale.US);f.setTimeZone(Model.TANGIER);
            weatherStatus.setText("تحديث "+f.format(new Date(weather.stamp))+"  ·  Open-Meteo");
        }else{
            sky.update(phase,"unknown",0f,progress);
            tempRow.setVisibility(GONE);
            conditions.setText(state==Model.WEATHER_LOADING?"جارٍ تحميل الطقس…":"الطقس غير متاح الآن");
            weatherStatus.setText(state==Model.WEATHER_LOADING?"":"سيظهر عند عودة الإنترنت");
        }
    }

    private void styleAction(boolean focused){
        if(focused){
            GradientDrawable ring=round(0,dp(34));ring.setStroke(dp(4),FOCUS);
            GradientDrawable fill=round(IVORY,dp(28));
            LayerDrawable bg=new LayerDrawable(new Drawable[]{ring,fill});bg.setLayerInset(1,dp(7),dp(7),dp(7),dp(7));
            action.setBackground(bg);action.setElevation(dp(10));
            padAction();
            actionTitle.setTextColor(INK);actionHint.setTextColor(INK_SOFT);
            okBadge.setTextColor(INK);okBadge.setBackground(oval(0,INK,3));
        }else{
            GradientDrawable fill=round(0xcc0e1f30,dp(28));fill.setStroke(dp(2),0x66f7f6f1);
            InsetDrawable bg=new InsetDrawable(fill,dp(7));
            action.setBackground(bg);action.setElevation(0);
            padAction();
            actionTitle.setTextColor(Model.TEXT);actionHint.setTextColor(Model.TEXT_MUTED);
            okBadge.setTextColor(Model.TEXT);okBadge.setBackground(oval(0,Model.TEXT,2));
        }
    }

    /** setBackground() replaces padding with the drawable's own; restore the card's spacing. */
    private void padAction(){action.setPadding(dp(30),dp(18),dp(30),dp(18));}

    /** Explicit wrap width: a default MATCH_PARENT child would not widen its wrap-content column. */
    private static LinearLayout.LayoutParams wrap(){return new LinearLayout.LayoutParams(-2,-2);}

    // ---- helpers -------------------------------------------------------------------------
    int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    /** Text size honours the accessibility font scale up to 130%, which the layout is tested at. */
    private TextView text(String value,float sp,int color){
        TextView t=new TextView(getContext());t.setText(value);t.setTextColor(color);
        if(sp>0)t.setTextSize(TypedValue.COMPLEX_UNIT_PX,sp*getResources().getDisplayMetrics().density*textScale);
        t.setGravity(Gravity.START);t.setTextDirection(TEXT_DIRECTION_RTL);t.setFontFeatureSettings("tnum");
        t.setShadowLayer(dp(8),0,dp(1),0x66000000);
        return t;
    }
    private GradientDrawable round(int fill,float radius){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(radius);return d;}
    private GradientDrawable oval(int fill,int stroke,int width){GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);d.setColor(fill);d.setStroke(dp(width),stroke);return d;}

    /** Fallback mark if the installed YouTube icon cannot be loaded. */
    static final class PlayMark extends Drawable {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c){
            Rect b=getBounds();float w=b.width(),h=w*0.7f,top=b.top+(b.height()-h)/2f;
            p.setColor(0xffe0261b);c.drawRoundRect(b.left,top,b.right,top+h,h*0.28f,h*0.28f,p);
            Path tri=new Path();float cx=b.exactCenterX()+w*0.03f,cy=top+h/2f,r=h*0.22f;
            tri.moveTo(cx-r*0.8f,cy-r);tri.lineTo(cx+r*1.05f,cy);tri.lineTo(cx-r*0.8f,cy+r);tri.close();
            p.setColor(0xffffffff);c.drawPath(tri,p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
}
