package local.reroute.youtubehome;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.*;
import android.view.ContextThemeWrapper;
import android.view.View;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Offscreen review renders of the real HomeScreen view tree, run with app_process on an Android 10
 * device or emulator. These are software-canvas renders of the production views (no elevation
 * shadow), not Activity screenshots. Never packaged into the launcher.
 */
public final class HomePreview {
    static Context systemContext() throws Exception {
        if(android.os.Looper.myLooper()==null)android.os.Looper.prepareMainLooper();
        Class<?> activityThread=Class.forName("android.app.ActivityThread");
        Object thread=activityThread.getMethod("systemMain").invoke(null);
        return (Context)activityThread.getMethod("getSystemContext").invoke(thread);
    }
    static Context arabic(Context base,float fontScale){
        Configuration config=new Configuration(base.getResources().getConfiguration());
        config.setLocale(Locale.forLanguageTag("ar-MA-u-nu-latn"));config.fontScale=fontScale;
        // The framework's system context does not declare supportsRtl; the launcher manifest does.
        // Report it here so views resolve right-to-left exactly as they do inside the real app.
        return new ContextThemeWrapper(base.createConfigurationContext(config),android.R.style.Theme_Material_NoActionBar){
            @Override public android.content.pm.ApplicationInfo getApplicationInfo(){
                android.content.pm.ApplicationInfo info=new android.content.pm.ApplicationInfo(super.getApplicationInfo());
                info.flags|=android.content.pm.ApplicationInfo.FLAG_SUPPORTS_RTL;info.targetSdkVersion=29;return info;
            }
        };
    }
    static long local(int hour,int minute){Calendar c=Calendar.getInstance(Model.TANGIER,Locale.US);c.clear();c.set(2026,Calendar.NOVEMBER,6,hour,minute,0);return c.getTimeInMillis();}
    static WeatherClient.Snapshot weather(long now,double t,int code){long[] s=Model.sunTimes(now);return new WeatherClient.Snapshot(t,code,now-600000,s[0],s[1]);}

    public static void main(String[] args) throws Exception {
        String out=args.length>0?args[0]:"/data/local/tmp/reroute-preview";
        new java.io.File(out).mkdirs();
        Context system=systemContext();
        android.graphics.drawable.Drawable icon=null;
        try{icon=system.getPackageManager().getApplicationIcon("com.google.android.youtube.tv");}catch(Exception missing){System.out.println("YouTube TV icon not installed here; fallback mark used");}
        // Same rule as HomeActivity.youTubeIcon(): only a sharp icon replaces the vector mark.
        if(icon!=null&&icon.getIntrinsicWidth()<Math.round(78*system.getResources().getDisplayMetrics().density)){System.out.println("YouTube icon "+icon.getIntrinsicWidth()+"px is too small; vector mark used");icon=null;}
        Object[][] cases={
            {"01-day-partly-cloudy",local(13,30),weather(local(13,30),21.4,2),0,1f,false},
            {"02-dawn-clear",local(8,5),weather(local(8,5),14.2,0),0,1f,false},
            {"03-day-clear",local(11,15),weather(local(11,15),19.6,0),0,1f,false},
            {"04-dusk-overcast",local(18,10),weather(local(18,10),17.0,3),0,1f,false},
            {"05-night-clear",local(21,40),weather(local(21,40),15.1,0),0,1f,false},
            {"06-day-rain",local(15,0),weather(local(15,0),16.3,63),0,1f,false},
            {"07-night-rain",local(22,30),weather(local(22,30),13.8,61),0,1f,false},
            {"08-night-storm",local(23,10),weather(local(23,10),12.9,95),0,1f,false},
            {"09-day-fog",local(9,30),weather(local(9,30),13.0,45),0,1f,false},
            {"10-dawn-snow",local(8,0),weather(local(8,0),0.6,73),0,1f,false},
            {"11-loading-first-boot",local(19,5),null,0,1f,false},
            {"12-weather-unavailable",local(16,20),null,3,1f,false},
            {"13-clock-not-synced",Model.MIN_VALID_TIME-30L*86400000L,null,0,1f,false},
            {"14-large-text-130-night-rain",local(22,30),weather(local(22,30),13.8,61),0,1.3f,false},
            {"15-large-text-130-longest-date",local(20,45),weather(local(20,45),-12.0,82),0,1.3f,false},
            {"16-maintenance-panel",local(18,10),weather(local(18,10),17.0,3),0,1f,true},
            {"17-maintenance-130",local(18,10),weather(local(18,10),17.0,3),0,1.3f,true},
            {"18-maintenance-130-settings-error",local(18,10),weather(local(18,10),17.0,3),0,1.3f,true},
            {"19-maintenance-130-weather-status",local(18,10),weather(local(18,10),17.0,3),0,1.3f,true},
        };
        // Long strings for width stress: Wednesday 29 September 2027, two-digit day, minus temperature.
        Calendar wide=Calendar.getInstance(Model.TANGIER,Locale.US);wide.clear();wide.set(2027,Calendar.SEPTEMBER,29,20,45,0);
        cases[14][1]=wide.getTimeInMillis();cases[14][2]=weather(wide.getTimeInMillis(),-12.0,82);
        for(Object[] k:cases){
            Context ctx=arabic(system,(Float)k[4]);
            HomeScreen screen=new HomeScreen(ctx);
            if(icon!=null)screen.setYouTubeIcon(icon.getConstantState().newDrawable(ctx.getResources()).mutate());
            screen.bind((Long)k[1],(WeatherClient.Snapshot)k[2],(Integer)k[3]);
            screen.showFocused();
            Bitmap bitmap=render(screen,1280,720);
            if((Boolean)k[5]){
                Maintenance m=new Maintenance(ctx,new Maintenance.Listener(){public boolean openSettings(){return false;}public void refreshWeather(){}});
                m.refreshDetails("الإصدار 3.0.1   ·   الساعة: غير مضبوطة بعد   ·   الطقس: غير متاح (محاولات: 6)");
                m.focusBack();
                View panel=m.panel();
                if(((String)k[0]).contains("settings-error"))panel.findViewById(Maintenance.SETTINGS_ID).performClick();
                if(((String)k[0]).contains("weather-status"))panel.findViewById(Maintenance.WEATHER_ID).performClick();
                Canvas c=new Canvas(bitmap);c.drawColor(0x8c000000);
                int w=Math.round(700*ctx.getResources().getDisplayMetrics().density);
                panel.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
                panel.layout(0,0,panel.getMeasuredWidth(),panel.getMeasuredHeight());
                checkPanel(panel,(String)k[0]);
                c.translate((1280-panel.getMeasuredWidth())/2f,(720-panel.getMeasuredHeight())/2f);panel.draw(c);
            }
            checkInsideSafeArea(screen,(String)k[0]);
            checkTextNotClipped(screen,(String)k[0]);
            try(FileOutputStream o=new FileOutputStream(out+"/"+k[0]+".png")){bitmap.compress(Bitmap.CompressFormat.PNG,100,o);}
            System.out.println("rendered "+k[0]);
        }
        System.out.println("HomePreview: "+cases.length+" offscreen renders written to "+out);
    }
    /** Validate the separately rendered maintenance tree, including its multiline status/error. */
    static void checkPanel(View panel,String name){
        int x=(1280-panel.getWidth())/2,y=(720-panel.getHeight())/2;
        if(x<64||y<36)throw new AssertionError(name+": maintenance outside TV safe area");
        checkPanelText(panel,panel.getWidth(),panel.getHeight(),name);
        List<Rect> boxes=new ArrayList<>();List<String> labels=new ArrayList<>();collect(panel,boxes,labels);
        for(int i=0;i<boxes.size();i++)for(int j=i+1;j<boxes.size();j++)
            if(Rect.intersects(boxes.get(i),boxes.get(j)))throw new AssertionError(name+": maintenance text overlap "+labels.get(i)+" / "+labels.get(j));
    }
    static void checkPanelText(View v,int w,int h,String name){
        if(v.getVisibility()!=View.VISIBLE)return;
        if(v instanceof android.widget.TextView){
            android.widget.TextView t=(android.widget.TextView)v;android.text.Layout l=t.getLayout();
            if(l!=null&&t.getText().length()>0){
                int room=t.getWidth()-t.getTotalPaddingLeft()-t.getTotalPaddingRight();
                for(int i=0;i<l.getLineCount();i++)if(l.getLineWidth(i)>room+1)throw new AssertionError(name+": maintenance text width clipped «"+t.getText()+"»");
                if(l.getHeight()>t.getHeight()-t.getTotalPaddingTop()-t.getTotalPaddingBottom())throw new AssertionError(name+": maintenance text height clipped");
                int[] at=new int[2];offset(v,at);
                if(at[0]<0||at[1]<0||at[0]+t.getWidth()>w||at[1]+t.getHeight()>h)throw new AssertionError(name+": maintenance child outside panel");
            }
        }
        if(v instanceof android.view.ViewGroup){android.view.ViewGroup g=(android.view.ViewGroup)v;for(int i=0;i<g.getChildCount();i++)checkPanelText(g.getChildAt(i),w,h,name);}
    }
    static Bitmap render(View v,int w,int h){
        v.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));
        v.layout(0,0,w,h);
        Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));return b;
    }
    /** Every visible text/action must sit inside a 5% overscan margin and must not overlap another. */
    static void checkInsideSafeArea(HomeScreen screen,String name){
        List<Rect> boxes=new ArrayList<>();List<String> labels=new ArrayList<>();
        collect(screen,boxes,labels);
        Rect safe=new Rect(64,36,1280-64,720-36);
        for(int i=0;i<boxes.size();i++){
            Rect r=boxes.get(i);
            if(!safe.contains(r))throw new AssertionError(name+": outside overscan safe area: "+labels.get(i)+" "+r.toShortString());
            for(int j=i+1;j<boxes.size();j++)if(Rect.intersects(r,boxes.get(j))&&!nested(labels.get(i),labels.get(j)))
                throw new AssertionError(name+": overlap "+labels.get(i)+" / "+labels.get(j));
        }
    }
    /** Every visible single text line must fit inside its own view (catches truncated status lines). */
    static void checkTextNotClipped(View v,String name){
        if(v.getVisibility()!=View.VISIBLE)return;
        if(v instanceof android.widget.TextView){
            android.widget.TextView t=(android.widget.TextView)v;android.text.Layout l=t.getLayout();
            if(l!=null&&t.getText().length()>0){
                float need=0;for(int i=0;i<l.getLineCount();i++)need=Math.max(need,l.getLineWidth(i));
                int room=t.getWidth()-t.getTotalPaddingLeft()-t.getTotalPaddingRight();
                if(need>room+1||l.getLineCount()>1&&t.getMaxLines()==1)throw new AssertionError(name+": text clipped «"+t.getText()+"» needs "+need+" has "+room);
                if(l.getLineCount()>1)throw new AssertionError(name+": unexpected wrap «"+t.getText()+"»");
            }
        }
        if(v instanceof android.view.ViewGroup){android.view.ViewGroup g=(android.view.ViewGroup)v;for(int i=0;i<g.getChildCount();i++)checkTextNotClipped(g.getChildAt(i),name);}
    }
    static boolean nested(String a,String b){return a.startsWith("action")||b.startsWith("action")?a.startsWith("action")&&b.startsWith("action"):false;}
    static void collect(View v,List<Rect> boxes,List<String> labels){
        if(v.getVisibility()!=View.VISIBLE)return;
        if(v instanceof SkyView)return;
        if(v instanceof android.widget.TextView&&((android.widget.TextView)v).getText().length()>0||v instanceof WeatherGlyph||v.getId()==HomeScreen.ACTION_ID){
            int[] at=new int[2];offset(v,at);
            Rect r=new Rect(at[0],at[1],at[0]+v.getWidth(),at[1]+v.getHeight());
            // Text views may be wider than their text; measure the drawn text instead.
            if(v instanceof android.widget.TextView&&v.getId()!=HomeScreen.ACTION_ID){
                android.widget.TextView t=(android.widget.TextView)v;android.text.Layout l=t.getLayout();
                if(l!=null){float left=Float.MAX_VALUE,right=0;for(int i=0;i<l.getLineCount();i++){left=Math.min(left,l.getLineLeft(i));right=Math.max(right,l.getLineRight(i));}
                    r=new Rect(at[0]+t.getTotalPaddingLeft()+(int)left,at[1]+t.getTotalPaddingTop(),at[0]+t.getTotalPaddingLeft()+(int)Math.ceil(right),at[1]+t.getTotalPaddingTop()+l.getHeight());}
            }
            boolean inAction=false;for(View p=v;p!=null&&p.getParent() instanceof View;p=(View)p.getParent())if(p.getId()==HomeScreen.ACTION_ID){inAction=true;break;}
            boxes.add(r);labels.add((inAction?"action:":"")+v.getClass().getSimpleName()+(v instanceof android.widget.TextView?"«"+((android.widget.TextView)v).getText()+"»":""));
        }
        if(v instanceof android.view.ViewGroup){android.view.ViewGroup g=(android.view.ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collect(g.getChildAt(i),boxes,labels);}
    }
    static void offset(View v,int[] at){int x=0,y=0;for(View p=v;p!=null;){x+=p.getLeft();y+=p.getTop();if(!(p.getParent() instanceof View))break;p=(View)p.getParent();}at[0]=x;at[1]=y;}
}
