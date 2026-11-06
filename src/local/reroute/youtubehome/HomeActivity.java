package local.reroute.youtubehome;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.util.Log;
import android.view.*;
import java.util.Locale;

/** Arabic TV Home. Resource locale belongs to this Activity, never the device. */
public final class HomeActivity extends Activity {
    static final String TAG="YouTubeHome";
    private static final String YOUTUBE="com.google.android.youtube.tv";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Model.MenuGate menu=new Model.MenuGate();
    private final Model.WeatherHint weatherHint=new Model.WeatherHint();
    private final Model.LaunchGuard launchGuard=new Model.LaunchGuard();
    private HomeScreen screen;
    private Maintenance maintenance;
    private WeatherClient.Snapshot weather;
    private boolean visible,fetching,receiving;
    private int failures;
    private long lastAttempt=-1800000L,lastSuccess=-1800000L;
    private final Runnable ticker=new Runnable(){@Override public void run(){
        if(!visible)return;render();refreshWeather(false);
        handler.postDelayed(this,Math.min(15000L,Model.delayToNextMinute(System.currentTimeMillis())));
    }};
    /** Clock set by NTP, timezone/date change, minute tick, or a network change (e.g. Wi-Fi late at boot). */
    private final BroadcastReceiver systemChanges=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent intent){
        String action=intent.getAction();
        if(Intent.ACTION_TIME_CHANGED.equals(action)||Intent.ACTION_TIMEZONE_CHANGED.equals(action)||Intent.ACTION_DATE_CHANGED.equals(action)){
            Log.i(TAG,"clock changed: reconsider cached weather");
            weather=null;render();nudgeWeather();
        }else if(CONNECTIVITY.equals(action)){
            if(!isInitialStickyBroadcast()){Log.i(TAG,"connectivity changed: reconsider weather");nudgeWeather();}
        }else render();
    }};
    @SuppressWarnings("deprecation")
    private static final String CONNECTIVITY=android.net.ConnectivityManager.CONNECTIVITY_ACTION;

    @Override protected void attachBaseContext(Context base){
        Configuration config=new Configuration(base.getResources().getConfiguration());
        config.setLocale(Locale.forLanguageTag("ar-MA-u-nu-latn"));
        super.attachBaseContext(base.createConfigurationContext(config));
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        immersive();
        screen=new HomeScreen(this);
        screen.action.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){launchYouTube();}});
        screen.setYouTubeIcon(youTubeIcon());
        setContentView(screen);
        screen.action.requestFocus();
        render();
    }
    private Drawable youTubeIcon(){
        // Use the installed icon only if it is sharp at the action's size; otherwise keep the
        // crisp vector play mark rather than a blurred upscale.
        try{
            Drawable icon=getPackageManager().getApplicationIcon(YOUTUBE);
            int needed=Math.round(78*getResources().getDisplayMetrics().density);
            return icon.getIntrinsicWidth()>=needed?icon:null;
        }catch(PackageManager.NameNotFoundException missing){return null;}
    }
    private void immersive(){
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    private void render(){
        long now=System.currentTimeMillis();
        // A boot may start before NTP has corrected the system clock. Reconsider
        // the untouched cache on later ticks rather than losing it for the session.
        if(weather==null){try{String cached=getPreferences(MODE_PRIVATE).getString("weather_json",null);if(cached!=null)weather=WeatherClient.Snapshot.parse(cached,now);}catch(Exception ignored){}}
        screen.bind(now,weather,failures);
        if(maintenance!=null&&maintenance.isShowing())maintenance.refreshDetails(details());
    }
    /** Retry hints survive a throttle or an in-flight request instead of being discarded. */
    private final Runnable hintedRefresh=new Runnable(){@Override public void run(){
        if(!visible||isDestroyed())return;
        if(weather!=null&&Model.fresh(weather.stamp,System.currentTimeMillis())){weatherHint.clear();return;}
        long elapsed=SystemClock.elapsedRealtime(),delay=weatherHint.delay(elapsed,lastAttempt,fetching);
        if(delay>0){handler.postDelayed(this,delay);return;}
        if(delay==0&&weatherHint.take(elapsed,lastAttempt,fetching)){
            failures=Math.min(failures,1);refreshWeather(true);
        }
    }};
    private void nudgeWeather(){weatherHint.request();queueHint();}
    private void queueHint(){
        if(visible&&weatherHint.pending()){
            handler.removeCallbacks(hintedRefresh);handler.post(hintedRefresh);
        }
    }
    private void refreshWeather(boolean force){
        long elapsed=SystemClock.elapsedRealtime();if(fetching||!visible)return;
        if(!force&&(elapsed-lastAttempt<Model.retryDelay(failures)||(weather!=null&&Model.fresh(weather.stamp,System.currentTimeMillis())&&elapsed-lastSuccess<1800000L)))return;
        lastAttempt=elapsed;fetching=true;
        new Thread(new Runnable(){@Override public void run(){
            try{
                final String json=WeatherClient.fetch();
                handler.post(new Runnable(){@Override public void run(){
                    fetching=false;if(isDestroyed())return;
                    try{
                        // Wall clock may have changed while HTTPS was in flight.
                        weather=WeatherClient.Snapshot.parse(json,System.currentTimeMillis());
                        failures=0;lastSuccess=SystemClock.elapsedRealtime();
                        getPreferences(MODE_PRIVATE).edit().putString("weather_json",json).apply();
                        Log.i(TAG,"weather updated");
                    }catch(org.json.JSONException invalid){failures=Math.min(6,failures+1);Log.w(TAG,"forecast rejected after clock/freshness validation");}
                    if(visible)render();queueHint();
                }});
            }catch(Exception error){
                Log.w(TAG,"weather attempt failed: "+error.getClass().getSimpleName());
                handler.post(new Runnable(){@Override public void run(){fetching=false;failures=Math.min(6,failures+1);if(visible&&!isDestroyed())render();queueHint();}});
            }
        }},"tangier-weather").start();
    }
    @Override protected void onResume(){
        super.onResume();visible=true;menu.reset();immersive();
        if(!receiving){
            IntentFilter filter=new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);filter.addAction(Intent.ACTION_DATE_CHANGED);
            filter.addAction(CONNECTIVITY);
            registerReceiver(systemChanges,filter);receiving=true;
        }
        handler.removeCallbacks(ticker);handler.post(ticker);
        focusAction();queueHint();
    }
    @Override protected void onPause(){
        visible=false;menu.reset();handler.removeCallbacks(ticker);handler.removeCallbacks(hintedRefresh);
        if(receiving){unregisterReceiver(systemChanges);receiving=false;}
        super.onPause();
    }
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);if(maintenance!=null)maintenance.dismiss();super.onDestroy();}
    /** Home pressed while already Home: close anything open and return to the single action. */
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);if(maintenance!=null)maintenance.dismiss();menu.reset();focusAction();}
    @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);if(hasFocus){immersive();focusAction();}}
    private void focusAction(){if(screen!=null&&!screen.action.isFocused())screen.action.requestFocus();}

    private void launchYouTube(){
        if(!launchGuard.allow(SystemClock.elapsedRealtime()))return;
        Intent intent=getPackageManager().getLeanbackLaunchIntentForPackage(YOUTUBE);
        if(intent==null)intent=getPackageManager().getLaunchIntentForPackage(YOUTUBE);
        if(intent==null){Log.w(TAG,"YouTube launch intent missing");message("تعذّر فتح يوتيوب", "يوتيوب غير متاح حالياً. حاولي مرة أخرى لاحقاً.");return;}
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try{startActivity(intent);}catch(RuntimeException error){Log.w(TAG,"YouTube launch failed",error);message("تعذّر فتح يوتيوب", "حاولي مرة أخرى بعد قليل.");}
    }
    private void message(String title,String body){new AlertDialog.Builder(this).setTitle(title).setMessage(body).setPositiveButton("حسناً",null).show();}

    // ---- Hidden maintenance -------------------------------------------------------------
    private void openMaintenance(){
        if(maintenance!=null&&maintenance.isShowing())return;
        Log.i(TAG,"maintenance opened by MENU x3");
        maintenance=new Maintenance(this,new Maintenance.Listener(){
            @Override public boolean openSettings(){return launchSettings();}
            @Override public void refreshWeather(){failures=Math.min(failures,1);HomeActivity.this.refreshWeather(true);}
        });
        maintenance.setOnDismissListener(new DialogInterface.OnDismissListener(){@Override public void onDismiss(DialogInterface d){focusAction();}});
        maintenance.show();
        maintenance.refreshDetails(details());
    }
    private String details(){
        long now=System.currentTimeMillis();
        String version="?";try{version=getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(PackageManager.NameNotFoundException ignored){}
        String clockState=Model.clockValid(now)?"مضبوطة":"غير مضبوطة بعد";
        String weatherState=weather!=null&&Model.fresh(weather.stamp,now)?"محدّث":fetching?"جارٍ التحميل":"غير متاح (محاولات: "+failures+")";
        return "الإصدار "+version+"   ·   الساعة: "+clockState+"   ·   الطقس: "+weatherState;
    }
    /**
     * Opens device Settings in its own task. Tries the generic action first, then the known TV
     * and phone Settings components, and logs the outcome for remote diagnosis.
     */
    private boolean launchSettings(){
        Intent[] candidates={
            new Intent(android.provider.Settings.ACTION_SETTINGS),
            new Intent(Intent.ACTION_MAIN).setClassName("com.android.tv.settings","com.android.tv.settings.MainSettings"),
            new Intent(Intent.ACTION_MAIN).setClassName("com.android.settings","com.android.settings.Settings"),
        };
        PackageManager pm=getPackageManager();
        for(Intent intent:candidates){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName target=intent.resolveActivity(pm);
            if(target==null){Log.i(TAG,"settings candidate unresolved: "+intent);continue;}
            try{startActivity(intent);Log.i(TAG,"settings launched: "+target.flattenToShortString());return true;}
            catch(RuntimeException error){Log.w(TAG,"settings launch failed: "+target.flattenToShortString(),error);}
        }
        return false;
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event){
        int key=event.getKeyCode();
        if(key==KeyEvent.KEYCODE_MENU){
            if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()>0)menu.reset();
            if(event.getAction()==KeyEvent.ACTION_UP&&menu.release(event.getDownTime(),event.getEventTime(),
                    ViewConfiguration.getLongPressTimeout(),event.isCanceled()||event.isLongPress()||event.getRepeatCount()>0))openMaintenance();
            return true;
        }
        if(event.getAction()==KeyEvent.ACTION_DOWN)menu.reset();
        switch(key){
            case KeyEvent.KEYCODE_DPAD_UP:case KeyEvent.KEYCODE_DPAD_DOWN:case KeyEvent.KEYCODE_DPAD_LEFT:case KeyEvent.KEYCODE_DPAD_RIGHT:
                // Only one control exists: arrows keep (or restore) focus on it and go nowhere else.
                if(event.getAction()==KeyEvent.ACTION_DOWN)focusAction();
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:case KeyEvent.KEYCODE_ENTER:case KeyEvent.KEYCODE_NUMPAD_ENTER:case KeyEvent.KEYCODE_BUTTON_A:
                if(screen!=null&&!screen.action.isFocused()){
                    focusAction();
                    if(event.getAction()==KeyEvent.ACTION_UP&&!event.isCanceled())launchYouTube();
                    return true;
                }
                break;
            default:break;
        }
        return super.dispatchKeyEvent(event);
    }
    @Override public void onBackPressed(){focusAction();}
}
