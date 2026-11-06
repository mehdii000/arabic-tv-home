package local.reroute.youtubehome;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

/**
 * Owner-only panel reached by MENU three times. "رجوع" is focused first and Back closes it, so an
 * accidental entry needs no decision. Focus moves only between its three buttons.
 */
final class Maintenance extends Dialog {
    interface Listener {boolean openSettings();void refreshWeather();}
    static final int BACK_ID=2001,SETTINGS_ID=2002,WEATHER_ID=2003;
    private final TextView details,status;
    private final TextView back;
    private final LinearLayout panel;

    Maintenance(Context context,final Listener listener){
        super(context,android.R.style.Theme_Material_Dialog_NoActionBar);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        panel=new LinearLayout(context);panel.setOrientation(LinearLayout.VERTICAL);
        panel.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);panel.setPadding(dp(36),dp(30),dp(36),dp(28));
        GradientDrawable bg=new GradientDrawable();bg.setColor(0xf2101d2b);bg.setCornerRadius(dp(26));bg.setStroke(dp(1),0x44f7f6f1);
        panel.setBackground(bg);
        TextView title=text("الصيانة",28,Model.TEXT);title.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));panel.addView(title);
        TextView body=text("هذه الأدوات لمن يعتني بالجهاز.\nاضغطي «رجوع» للعودة إلى الشاشة الرئيسية دون تغيير أي شيء.",18,Model.TEXT_MUTED);
        body.setPadding(0,dp(8),0,dp(12));body.setLineSpacing(0,1.15f);panel.addView(body);
        details=text("",13,Model.TEXT_MUTED);details.setAlpha(0.85f);panel.addView(details);
        LinearLayout row=new LinearLayout(context);row.setOrientation(LinearLayout.HORIZONTAL);row.setPadding(0,dp(22),0,0);
        panel.addView(row,new LinearLayout.LayoutParams(-1,-2));
        back=button("رجوع",BACK_ID,row);
        final TextView settings=button("إعدادات الجهاز",SETTINGS_ID,row);
        TextView weather=button("تحديث الطقس",WEATHER_ID,row);
        // Explicit, closed focus loop between the three buttons; up/down stay put.
        link(back,SETTINGS_ID,BACK_ID);link(settings,WEATHER_ID,BACK_ID);link(weather,WEATHER_ID,SETTINGS_ID);
        status=text("",15,0xfff6c78a);status.setPadding(0,dp(14),0,0);status.setVisibility(View.GONE);panel.addView(status);
        back.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){dismiss();}});
        settings.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            if(listener.openSettings())dismiss();
            else{status.setText("تعذّر فتح إعدادات الجهاز على هذا الجهاز.");status.setVisibility(View.VISIBLE);}
        }});
        weather.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            listener.refreshWeather();status.setText("جارٍ تحديث الطقس…");status.setVisibility(View.VISIBLE);
        }});
        setContentView(panel,new ViewGroup.LayoutParams(dp(700),-2));
        Window w=getWindow();
        if(w!=null){w.setBackgroundDrawable(new ColorDrawable(0));w.setDimAmount(0.55f);}
        setCanceledOnTouchOutside(false);
    }
    @Override public void show(){super.show();back.requestFocus();}
    /** Preview-only access to the panel content. */
    View panel(){return panel;}
    void focusBack(){back.requestFocus();}
    void refreshDetails(String text){details.setText(text);}
    @Override public boolean dispatchKeyEvent(KeyEvent event){
        // MENU inside the panel does nothing; it must never chain into another action.
        if(event.getKeyCode()==KeyEvent.KEYCODE_MENU)return true;
        return super.dispatchKeyEvent(event);
    }

    private int dp(float n){return Math.round(n*getContext().getResources().getDisplayMetrics().density);}
    private TextView text(String value,float sp,int color){
        TextView t=new TextView(getContext());t.setText(value);t.setTextColor(color);t.setTextSize(TypedValue.COMPLEX_UNIT_SP,sp);
        t.setGravity(Gravity.START);t.setTextDirection(View.TEXT_DIRECTION_RTL);return t;
    }
    private TextView button(String label,int id,LinearLayout row){
        final TextView b=text(label,20,Model.TEXT);b.setId(id);b.setGravity(Gravity.CENTER);
        b.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));b.setPadding(dp(26),dp(12),dp(26),dp(12));
        b.setFocusable(true);b.setFocusableInTouchMode(true);b.setClickable(true);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);lp.setMarginEnd(dp(14));row.addView(b,lp);
        style(b,false);
        b.setOnFocusChangeListener(new View.OnFocusChangeListener(){@Override public void onFocusChange(View v,boolean f){style(b,f);}});
        return b;
    }
    private void style(TextView b,boolean focused){
        GradientDrawable d=new GradientDrawable();d.setCornerRadius(dp(18));
        if(focused){d.setColor(HomeScreen.IVORY);d.setStroke(dp(3),HomeScreen.FOCUS);b.setTextColor(HomeScreen.INK);}
        else{d.setColor(0x26f7f6f1);d.setStroke(dp(1),0x44f7f6f1);b.setTextColor(Model.TEXT);}
        b.setBackground(d);
    }
    /** In this RTL row the next button sits to the left. */
    private static void link(View v,int left,int right){v.setNextFocusLeftId(left);v.setNextFocusRightId(right);v.setNextFocusUpId(v.getId());v.setNextFocusDownId(v.getId());}
}
