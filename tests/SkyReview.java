package local.reroute.youtubehome;

import android.content.Context;
import android.graphics.*;
import java.io.FileOutputStream;

/**
 * One-shot rendering/JSON test run with app_process on an Android 10 device or emulator.
 * Never packaged into the launcher. Writes a contact sheet of every phase x weather sky.
 */
public final class SkyReview {
    static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    static final String[] PHASES={"dawn","day","dusk","night"};
    static final String[] KINDS={"clear","cloud","fog","rain","snow","storm","unknown"};
    public static void main(String[] args) throws Exception {
        String out=args.length>0?args[0]:"/data/local/tmp/reroute-preview";
        new java.io.File(out).mkdirs();
        long now=System.currentTimeMillis(),rise=now-3600000,set=now+3600000;
        String valid="{\"current\":{\"time\":"+now/1000+",\"temperature_2m\":21.4,\"weather_code\":61},\"daily\":{\"sunrise\":["+(rise-86400000)/1000+","+rise/1000+"],\"sunset\":["+(set-86400000)/1000+","+set/1000+"]}}";
        WeatherClient.Snapshot parsed=WeatherClient.Snapshot.parse(valid,now);
        require(parsed.code==61&&parsed.temperature==21.4,"parse forecast");
        require(Math.abs(parsed.sunrise-rise)<1000,"select today's solar row rather than yesterday");
        for(String bad:new String[]{"{}","not-json",valid.replace("21.4","999"),valid.replace("\"time\":"+now/1000,"\"time\":"+(now/1000-20000))}) {
            boolean rejected=false;try{WeatherClient.Snapshot.parse(bad,now);}catch(org.json.JSONException expected){rejected=true;}
            require(rejected,"malformed, missing, invalid or stale forecast must be rejected");
        }
        Context context=HomePreview.arabic(HomePreview.systemContext(),1f);
        int cw=320,chh=180;
        Bitmap sheet=Bitmap.createBitmap(cw*4,chh*KINDS.length,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);
        Paint label=new Paint(Paint.ANTI_ALIAS_FLAG);label.setColor(Color.WHITE);label.setTextSize(15);label.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));label.setShadowLayer(3,0,1,0xaa000000);
        float[] progress={0.03f,0.5f,0.97f,0.4f};
        for(int k=0;k<KINDS.length;k++)for(int i=0;i<4;i++){
            int save=canvas.save();canvas.translate(i*cw,k*chh);canvas.clipRect(0,0,cw,chh);
            SkyView view=new SkyView(context);view.layout(0,0,cw,chh);
            view.update(PHASES[i],KINDS[k],KINDS[k].equals("cloud")?0.55f:KINDS[k].equals("clear")||KINDS[k].equals("unknown")?0f:1f,progress[i]);
            view.draw(canvas);
            canvas.drawText(KINDS[k].toUpperCase(java.util.Locale.US)+" / "+PHASES[i].toUpperCase(java.util.Locale.US),10,22,label);
            canvas.restoreToCount(save);
        }
        try(FileOutputStream o=new FileOutputStream(out+"/sky-sheet.png")){require(sheet.compress(Bitmap.CompressFormat.PNG,100,o),"PNG encode");}
        System.out.println("SkyReview: WeatherClient JSON validation passed; sky sheet of "+(4*KINDS.length)+" states written");
    }
}
