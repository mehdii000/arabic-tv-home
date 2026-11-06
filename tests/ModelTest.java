package local.reroute.youtubehome;
import java.util.*;
public final class ModelTest {
  static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
  static final String[] PHASES={"dawn","day","dusk","night"};
  static final String[] KINDS={"clear","cloud","fog","rain","snow","storm","unknown"};
  static long utc(int y,int m,int d,int h,int min){Calendar c=Calendar.getInstance(TimeZone.getTimeZone("UTC"),Locale.US);c.clear();c.set(y,m-1,d,h,min,0);return c.getTimeInMillis();}
  static int localMinutes(long t){Calendar c=Model.calendar(t);return c.get(Calendar.HOUR_OF_DAY)*60+c.get(Calendar.MINUTE);}
  public static void main(String[] args) {
    // Digits and calendar (unchanged behaviour)
    check("0123456789 0123456789".equals(Model.latinDigits("٠١٢٣٤٥٦٧٨٩ ۰۱۲۳۴۵۶۷۸۹")), "convert both Arabic digit families");
    long now=1793984400000L;
    String date=Model.date(now);
    check(date.contains("الجمعة") && date.contains("نونبر") && date.contains("2026"), "Arabic Moroccan calendar: "+date);
    check(Model.dayName(now).equals("الجمعة") && Model.dayMonthYear(now).equals("6 نونبر 2026"), "split day/date lines: "+Model.dayMonthYear(now));
    for(String shown:new String[]{Model.clock(now),date,Model.dayMonthYear(now)})
      check(!shown.matches(".*[٠-٩۰-۹].*"), "Latin digits only: "+shown);
    TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"));
    Locale.setDefault(Locale.forLanguageTag("ar-EG"));
    check(date.equals(Model.date(now)),"fixed Tangier timezone independent of device locale/timezone");
    check(!Model.clock(now).matches(".*[٠-٩۰-۹].*")&&Model.clock(now).equals("18:00"),"Latin 24h clock under an Arabic default locale: "+Model.clock(now));

    // Unsynchronized boot clock
    check(!Model.clockValid(utc(2025,11,13,9,0)),"box boot clock from 2025 is not trusted");
    check(Model.clockValid(now),"synchronized clock trusted");
    check(Model.delayToNextMinute(utc(2026,11,6,18,0)+59000)==1250 && Model.delayToNextMinute(utc(2026,11,6,18,0))==60250,"minute-aligned ticks");

    // Weather codes and states
    check("rain".equals(Model.weatherKind(61)),"rain WMO");
    check("snow".equals(Model.weatherKind(85)),"snow WMO");
    check("storm".equals(Model.weatherKind(99)),"storm WMO");
    check("unknown".equals(Model.weatherKind(123)),"unknown must not pretend sunny");
    check(Model.cloudiness(0)==0f&&Model.cloudiness(2)>Model.cloudiness(1)&&Model.cloudiness(3)==1f&&Model.cloudiness(123)==0f,"cloud cover ordering");
    check(Model.weatherLabel(80,true).equals("زخات مطر")&&Model.weatherLabel(53,true).equals("رذاذ"),"showers and drizzle named precisely");
    check(!Model.fresh(now-10800001L,now),"stale weather after 3h");
    check(!Model.fresh(now+600000,now),"future cached weather after clock change invalid");
    check(Model.fresh(now-60000,now),"fresh cached weather");
    check(Model.weatherState(true,0,true)==Model.WEATHER_FRESH,"fresh weather shown");
    check(Model.weatherState(true,0,false)==Model.WEATHER_LOADING,"no weather claim before the clock is trusted");
    check(Model.weatherState(false,0,true)==Model.WEATHER_LOADING,"first boot / first attempt is loading");
    check(Model.weatherState(false,1,true)==Model.WEATHER_UNAVAILABLE,"failed attempt is unavailable, not invented");
    check(Model.weatherState(false,3,true)==Model.WEATHER_UNAVAILABLE,"later retries keep a steady unavailable message");
    check(Model.retryDelay(1)==15000 && Model.retryDelay(2)==30000 && Model.retryDelay(100)==300000,"short startup retry with bounded backoff");

    // Solar phase with forecast times
    long rise=now-4*3600000L, set=now+4*3600000L;
    check("day".equals(Model.phase(now,rise,set)),"day");
    check("dusk".equals(Model.phase(set-600000,rise,set)),"sunset");
    check("night".equals(Model.phase(set+3600000,rise,set)),"night");
    check("dawn".equals(Model.phase(rise-600000,rise,set)),"sunrise");
    check(Math.abs(Model.arcProgress(now,rise,set)-0.5f)<0.001f,"midday arc");
    float nightProgress=Model.arcProgress(set+3600000,rise,set);
    check(nightProgress>0f&&nightProgress<0.2f,"early night arc "+nightProgress);

    check(Model.arcProgress(rise-60000,rise,set)==0f,"dawn sun stays at east horizon before sunrise");
    check(Model.arcProgress(set+60000,rise,set)==1f,"dusk sun stays at west horizon after sunset");
    check(Math.abs(Model.arcProgress(rise+60000,rise,set)-Model.arcProgress(rise-60000,rise,set))<0.01f,"no sun jump at sunrise");
    check(Math.abs(Model.arcProgress(set+60000,rise,set)-Model.arcProgress(set-60000,rise,set))<0.01f,"no sun jump at sunset");

    // Offline sun calculation for Tangier (reference: published Tangier almanac, UTC+1)
    long[] june=Model.sunTimes(utc(2026,6,21,12,0)), dec=Model.sunTimes(utc(2026,12,21,12,0)), nov=Model.sunTimes(now);
    double juneHours=(june[1]-june[0])/3600000.0, decHours=(dec[1]-dec[0])/3600000.0;
    check(juneHours>14.3&&juneHours<14.7,"June day length "+juneHours);
    check(decHours>9.5&&decHours<10.0,"December day length "+decHours);
    check(localMinutes(june[0])>=6*60&&localMinutes(june[0])<=6*60+25,"June sunrise local "+Model.clock(june[0]));
    check(localMinutes(dec[0])>=8*60+15&&localMinutes(dec[0])<=8*60+40,"December sunrise local "+Model.clock(dec[0]));
    check(localMinutes(nov[0])>=7*60+35&&localMinutes(nov[0])<=8*60+5&&localMinutes(nov[1])>=18*60+10&&localMinutes(nov[1])<=18*60+35,"6 Nov sun "+Model.clock(nov[0])+"–"+Model.clock(nov[1]));
    check(Model.sameLocalDay(nov[0],now)&&Model.sameLocalDay(nov[1],now),"computed sun belongs to the local day");
    check(!"day".equals(Model.phase(now+86400000L,rise,set)) || Model.phase(now+86400000L,rise,set).equals(Model.phase(now+86400000L,0,0)),"yesterday forecast times fall back to the offline sun");
    check("night".equals(Model.phase(utc(2026,11,6,23,0),0,0))&&"day".equals(Model.phase(utc(2026,11,6,12,0),0,0)),"offline phase without weather");

    // Sky palettes: distinct by time and weather; text keeps contrast on every state
    for(String kind:KINDS) {
      check(!Arrays.equals(Model.skyPalette("day",kind,1f),Model.skyPalette("night",kind,1f)),kind+" must change with time of day");
      check(!Arrays.equals(Model.skyPalette("dawn",kind,1f),Model.skyPalette("dusk",kind,1f)),kind+" dawn and dusk distinguishable");
      for(String phase:PHASES) for(float cover:new float[]{0f,0.25f,0.55f,1f}) {
        int[] p=Model.skyPalette(phase,kind,cover);
        for(int i=0;i<2;i++) {
          double main=Model.contrast(Model.TEXT,p[i]), muted=Model.contrast(Model.TEXT_MUTED,p[i]);
          check(main>=7.0,String.format(Locale.US,"%s/%s text contrast %.2f on stop %d",phase,kind,main,i));
          check(muted>=4.5,String.format(Locale.US,"%s/%s muted contrast %.2f on stop %d",phase,kind,muted,i));
        }
      }
    }
    for(String phase:PHASES) for(String kind:KINDS) for(String other:KINDS)
      if(!kind.equals(other)) check(!Arrays.equals(Model.skyPalette(phase,kind,1f),Model.skyPalette(phase,other,1f)),phase+" "+kind+" vs "+other+" must differ");
    check(Model.contrast(0xffffffff,0xff000000)>20.9,"contrast formula");

    // Remote
    Model.MenuGate gate=new Model.MenuGate();
    check(!gate.press(1000) && !gate.press(1500),"single and double MENU cannot enter settings");
    check(gate.press(1900),"triple MENU");
    check(!gate.press(2000),"triple consumes sequence");
    check(!gate.press(5000),"old sequence expires");
    gate.reset();check(!gate.press(5100),"navigation resets sequence");
    check(!gate.press(9000)&&!gate.press(9900)&&!gate.press(11100),"three presses spread over more than 2 seconds do not open");
    gate.reset();
    check(!gate.release(1000,1050,500,false)&&!gate.release(1200,1250,500,false),"two deliberate MENU taps");
    check(!gate.release(1300,1900,500,false),"held third MENU cannot open maintenance");
    check(!gate.release(1950,2000,500,false),"held MENU resets the prior taps");
    gate.reset();
    check(!gate.release(1000,1050,500,false)&&!gate.release(1200,1250,500,true)&&!gate.release(1400,1450,500,false),"cancelled MENU resets sequence");
    Model.WeatherHint hint=new Model.WeatherHint();
    check(hint.delay(1000,0,false)==-1,"no hint does not schedule work");
    hint.request();check(hint.delay(1000,0,false)==4000,"early recovery hint schedules instead of disappearing");
    check(hint.delay(6000,0,true)==-1&&hint.pending(),"in-flight request keeps hint pending");
    check(hint.delay(6000,0,false)==0&&hint.take(6000,0,false),"completion consumes pending recovery hint promptly");
    check(!hint.take(6001,0,false),"one hint causes at most one forced refresh");
    hint.request();hint.request();check(hint.take(7000,0,false)&&!hint.pending(),"repeated hints coalesce");
    Model.LaunchGuard guard=new Model.LaunchGuard();
    check(guard.allow(20000)&&!guard.allow(20400)&&guard.allow(22000),"double OK opens YouTube once");
    System.out.println("ModelTest: all date, digit, clock, weather, solar, palette-contrast and remote checks passed");
  }
}
