package local.reroute.youtubehome;

import java.text.SimpleDateFormat;
import java.util.*;

/** Calendar, weather, sky and remote rules, independent of Android/UI state. */
public final class Model {
    public static final TimeZone TANGIER = TimeZone.getTimeZone("Africa/Casablanca");
    public static final double LATITUDE = 35.76, LONGITUDE = -5.83;
    /** 2026-11-01T00:00Z. A box without a synchronized clock boots earlier than this. */
    public static final long MIN_VALID_TIME = 1793491200000L;
    private static final String[] DAYS={"الأحد","الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت"};
    private static final String[] MONTHS={"يناير","فبراير","مارس","أبريل","ماي","يونيو","يوليوز","غشت","شتنبر","أكتوبر","نونبر","دجنبر"};
    private Model() {}

    // ---- Calendar -------------------------------------------------------------------------
    public static String latinDigits(String input) {
        StringBuilder out=new StringBuilder();
        for(char c:input.toCharArray()) {
            if(c>='٠' && c<='٩') c=(char)('0'+c-'٠');
            if(c>='۰' && c<='۹') c=(char)('0'+c-'۰');
            out.append(c);
        }
        return out.toString();
    }
    static Calendar calendar(long now) {Calendar c=Calendar.getInstance(TANGIER,Locale.US);c.setTimeInMillis(now);return c;}
    /** False while the box still runs on an unsynchronized boot clock. */
    public static boolean clockValid(long now) {return now>=MIN_VALID_TIME;}
    public static String clock(long now) {
        SimpleDateFormat f=new SimpleDateFormat("HH:mm",Locale.US);f.setTimeZone(TANGIER);return f.format(new Date(now));
    }
    /** Full single-line date, also used to match a forecast's solar row to the local day. */
    public static String date(long now) {return dayName(now)+"، "+dayMonthYear(now);}
    public static String dayName(long now) {return DAYS[calendar(now).get(Calendar.DAY_OF_WEEK)-1];}
    public static String dayMonthYear(long now) {
        Calendar c=calendar(now);
        return c.get(Calendar.DAY_OF_MONTH)+" "+MONTHS[c.get(Calendar.MONTH)]+" "+c.get(Calendar.YEAR);
    }
    public static String greeting(long now) {int h=calendar(now).get(Calendar.HOUR_OF_DAY);return h>=5 && h<12?"صباح الخير":"مساء الخير";}
    public static String period(long now) {int h=calendar(now).get(Calendar.HOUR_OF_DAY);return h<5||h>=21?"ليلاً":h<12?"صباحاً":h<17?"بعد الظهر":"مساءً";}
    /** Milliseconds until the next local minute begins, plus a small margin. */
    public static long delayToNextMinute(long now) {return 60000L-((now%60000L+60000L)%60000L)+250L;}

    // ---- Weather --------------------------------------------------------------------------
    public static boolean fresh(long stamp,long now) {return stamp>0 && now>=stamp-60000 && now-stamp<=10800000L;}
    public static String weatherKind(int code) {
        if(code==0)return "clear";
        if(code>=1&&code<=3)return "cloud";
        if(code==45||code==48)return "fog";
        if(code>=51&&code<=67||code>=80&&code<=82)return "rain";
        if(code>=71&&code<=77||code==85||code==86)return "snow";
        if(code==95||code==96||code==99)return "storm";
        return "unknown";
    }
    /** Approximate cloud cover implied by a WMO code, 0 (clear) to 1 (overcast). */
    public static float cloudiness(int code) {
        if(code==0)return 0f;
        if(code==1)return 0.25f;
        if(code==2)return 0.55f;
        return weatherKind(code).equals("unknown")?0f:1f;
    }
    public static String weatherLabel(int code,boolean day) {
        if(code==0)return day?"سماء صافية":"ليلة صافية";
        if(code==1)return "غالباً صافٍ";
        if(code==2)return "غائم جزئياً";
        if(code==3)return "غائم";
        if(code>=51&&code<=57)return "رذاذ";
        if(code==80||code==81||code==82)return "زخات مطر";
        String k=weatherKind(code);
        if(k.equals("fog"))return "ضباب";
        if(k.equals("rain"))return "أمطار";
        if(k.equals("snow"))return "ثلوج";
        if(k.equals("storm"))return "عواصف رعدية";
        return "الطقس غير متاح";
    }
    public static final int WEATHER_FRESH=0, WEATHER_LOADING=1, WEATHER_UNAVAILABLE=2;
    /**
     * What the weather corner may claim. Never presents old or unverified data as current. Once an
     * attempt has failed the message stays "unavailable" during later retries, so it does not flicker.
     */
    public static int weatherState(boolean hasFresh,int failures,boolean clockValid) {
        if(hasFresh&&clockValid)return WEATHER_FRESH;
        if(!clockValid||failures==0)return WEATHER_LOADING;
        return WEATHER_UNAVAILABLE;
    }
    public static long retryDelay(int failures){return Math.min(300000L,15000L << Math.max(0,Math.min(5,failures-1)));}

    // ---- Sun ------------------------------------------------------------------------------
    /** Sunrise and sunset (epoch ms) for Tangier's local civil day containing now; offline. */
    public static long[] sunTimes(long now) {
        Calendar c=calendar(now);
        Calendar utcNoon=Calendar.getInstance(TimeZone.getTimeZone("UTC"),Locale.US);
        utcNoon.clear();utcNoon.set(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH),12,0,0);
        double jd=utcNoon.getTimeInMillis()/86400000.0+2440587.5;
        double n=Math.round(jd-2451545.0+0.0008);
        double jStar=n-LONGITUDE/360.0;
        double m=Math.toRadians((357.5291+0.98560028*jStar)%360.0);
        double center=1.9148*Math.sin(m)+0.0200*Math.sin(2*m)+0.0003*Math.sin(3*m);
        double lambda=Math.toRadians((Math.toDegrees(m)+center+180.0+102.9372)%360.0);
        double transit=2451545.0+jStar+0.0053*Math.sin(m)-0.0069*Math.sin(2*lambda);
        double declination=Math.asin(Math.sin(lambda)*Math.sin(Math.toRadians(23.4397)));
        double phi=Math.toRadians(LATITUDE);
        double cosOmega=(Math.sin(Math.toRadians(-0.833))-Math.sin(phi)*Math.sin(declination))/(Math.cos(phi)*Math.cos(declination));
        double omega=Math.toDegrees(Math.acos(Math.max(-1,Math.min(1,cosOmega))))/360.0;
        return new long[]{toEpoch(transit-omega),toEpoch(transit+omega)};
    }
    private static long toEpoch(double julian){return Math.round((julian-2440587.5)*86400000.0);}
    static boolean sameLocalDay(long a,long b){Calendar x=calendar(a),y=calendar(b);return x.get(Calendar.YEAR)==y.get(Calendar.YEAR)&&x.get(Calendar.DAY_OF_YEAR)==y.get(Calendar.DAY_OF_YEAR);}
    /** Forecast solar times when they match today, otherwise the offline calculation. */
    public static long[] solar(long now,long sunrise,long sunset) {
        if(sunrise>0&&sunset>sunrise&&sameLocalDay(now,sunrise))return new long[]{sunrise,sunset};
        return sunTimes(now);
    }
    public static String phase(long now,long sunrise,long sunset) {
        long[] s=solar(now,sunrise,sunset);
        if(now>=s[0]-1800000L && now<s[0]+2700000L)return "dawn";
        if(now>=s[1]-2700000L && now<s[1]+1800000L)return "dusk";
        return now>=s[0] && now<s[1]?"day":"night";
    }
    /** Progress through the current daylight (or darkness) arc, 0 at its start, 1 at its end. */
    public static float arcProgress(long now,long sunrise,long sunset) {
        long[] s=solar(now,sunrise,sunset);
        long rise=s[0],set=s[1],day=set-rise,night=86400000L-day;
        if(!phase(now,rise,set).equals("night"))return Math.max(0f,Math.min(1f,(float)(now-rise)/day));
        long since=now>=set?now-set:now-(set-86400000L);
        return Math.max(0f,Math.min(1f,(float)since/night));
    }

    // ---- Sky colours ----------------------------------------------------------------------
    public static final int TEXT=0xfff7f6f1, TEXT_MUTED=0xffd3dde5;
    private static int[] base(String phase) {
        if(phase.equals("night"))return new int[]{0xff060f1c,0xff0c1c31,0xff1d3550};
        if(phase.equals("dawn"))return new int[]{0xff19223f,0xff3d3a5c,0xffc07a62};
        if(phase.equals("dusk"))return new int[]{0xff151a37,0xff452f53,0xffbd5f4a};
        return new int[]{0xff0b3760,0xff1b5682,0xff6c9fbb};
    }
    private static int tone(String phase) {
        if(phase.equals("night"))return 0xff151b24;
        if(phase.equals("dawn"))return 0xff383a4a;
        if(phase.equals("dusk"))return 0xff383141;
        return 0xff3a495c;
    }
    /** Zenith, upper-text band and horizon colours for a phase and weather state. */
    public static int[] skyPalette(String phase,String kind,float cloudiness) {
        int[] b=base(phase);int g=tone(phase);int[] out=new int[3];
        for(int i=0;i<3;i++){
            int c=b[i];
            if(kind.equals("cloud"))c=mix(c,g,0.2f+0.4f*cloudiness);
            else if(kind.equals("fog"))c=mix(c,i==2?lighten(g,0.35f):lighten(g,0.04f),0.72f);
            else if(kind.equals("rain"))c=scale(mix(c,g,0.62f),0.84f);
            else if(kind.equals("storm"))c=scale(mix(c,mix(g,0xff2a2142,0.5f),0.68f),0.7f);
            else if(kind.equals("snow"))c=mix(c,mix(g,0xff7d93ab,i==2?0.6f:0.2f),i==2?0.6f:0.45f);
            else if(kind.equals("unknown"))c=mix(c,g,0.3f);
            out[i]=c;
        }
        return out;
    }
    public static int mix(int a,int b,float t){
        return 0xff000000|(Math.round(ch(a,16)+(ch(b,16)-ch(a,16))*t)<<16)|(Math.round(ch(a,8)+(ch(b,8)-ch(a,8))*t)<<8)|Math.round(ch(a,0)+(ch(b,0)-ch(a,0))*t);
    }
    public static int scale(int a,float f){return mix(0xff000000,a,f);}
    public static int lighten(int a,float t){return mix(a,0xffffffff,t);}
    private static int ch(int c,int shift){return (c>>shift)&0xff;}
    public static double luminance(int c){return 0.2126*lin(ch(c,16))+0.7152*lin(ch(c,8))+0.0722*lin(ch(c,0));}
    private static double lin(int v){double s=v/255.0;return s<=0.03928?s/12.92:Math.pow((s+0.055)/1.055,2.4);}
    public static double contrast(int a,int b){double x=luminance(a),y=luminance(b);return (Math.max(x,y)+0.05)/(Math.min(x,y)+0.05);}

    // ---- Remote ---------------------------------------------------------------------------
    /** Three distinct MENU releases within two seconds; any other key resets the sequence. */
    public static final class MenuGate {
        private long first; private int count;
        public boolean press(long elapsed) {
            if(count==0||elapsed-first>2000||elapsed<first){first=elapsed;count=0;}
            if(++count==3){reset();return true;}return false;
        }
        public boolean release(long down,long up,int holdTimeout,boolean invalid) {
            if(invalid||up<down||up-down>=holdTimeout){reset();return false;}
            return press(up);
        }
        public void reset(){count=0;first=0;}
    }
    /** A recovery hint survives throttling and an in-flight request; repeated hints coalesce. */
    public static final class WeatherHint {
        private boolean pending;
        public void request(){pending=true;}
        public boolean pending(){return pending;}
        public void clear(){pending=false;}
        public long delay(long elapsed,long lastAttempt,boolean fetching){
            if(!pending||fetching)return -1;
            return Math.max(0L,5000L-(elapsed-lastAttempt));
        }
        public boolean take(long elapsed,long lastAttempt,boolean fetching){
            if(delay(elapsed,lastAttempt,fetching)!=0)return false;
            pending=false;return true;
        }
    }
    /** Ignores an accidental second OK while YouTube is still starting. */
    public static final class LaunchGuard {
        private long last=-10000L;
        public boolean allow(long elapsed){if(elapsed-last<1500&&elapsed>=last)return false;last=elapsed;return true;}
    }
}
