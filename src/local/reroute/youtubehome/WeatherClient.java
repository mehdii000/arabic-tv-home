package local.reroute.youtubehome;

import org.json.*;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.io.*;

/** Fixed-city HTTPS forecast. No device identifier, location permission, or API key. */
final class WeatherClient {
    static final String URL_TEXT="https://api.open-meteo.com/v1/forecast?latitude=35.76&longitude=-5.83&current=temperature_2m,weather_code,is_day&daily=sunrise,sunset&timezone=Africa%2FCasablanca&timeformat=unixtime&forecast_days=2";
    static final class Snapshot {
        final double temperature; final int code; final long stamp,sunrise,sunset;
        Snapshot(double t,int c,long stamp,long rise,long set){temperature=t;code=c;this.stamp=stamp;sunrise=rise;sunset=set;}
        static Snapshot parse(String json,long now) throws JSONException {
            JSONObject root=new JSONObject(json), current=root.getJSONObject("current"),daily=root.getJSONObject("daily");
            double temp=current.getDouble("temperature_2m");
            long stamp=current.getLong("time")*1000L;
            if(Double.isNaN(temp)||Double.isInfinite(temp)||temp< -80||temp>65||!Model.fresh(stamp,now))throw new JSONException("Invalid or stale forecast");
            long rise=0,set=0;
            JSONArray rises=daily.getJSONArray("sunrise"),sets=daily.getJSONArray("sunset");
            for(int i=0;i<rises.length()&&i<sets.length();i++) {
                long candidate=rises.getLong(i)*1000L;
                if(Model.date(candidate).equals(Model.date(now))){rise=candidate;set=sets.getLong(i)*1000L;break;}
            }
            return new Snapshot(temp,current.getInt("weather_code"),stamp,rise,set);
        }
    }
    static String fetch() throws IOException {
        HttpsURLConnection c=(HttpsURLConnection)new URL(URL_TEXT).openConnection();
        c.setConnectTimeout(8000);c.setReadTimeout(8000);c.setInstanceFollowRedirects(false);
        try {
            if(c.getResponseCode()!=200)throw new IOException("Weather unavailable");
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            try(InputStream in=c.getInputStream()) {
                byte[] buffer=new byte[2048];int n;
                while((n=in.read(buffer))!=-1){out.write(buffer,0,n);if(out.size()>65536)throw new IOException("Oversized forecast");}
            }
            return out.toString("UTF-8");
        } finally {c.disconnect();}
    }
}
