package local.reroute.youtubehome;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Small static weather symbol drawn natively, so the condition reads without words. */
final class WeatherGlyph extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private String kind="unknown";private boolean day=true;private float cloud;
    WeatherGlyph(Context context){super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    void set(String kind,boolean day,float cloudiness){
        if(!kind.equals(this.kind)||day!=this.day||cloudiness!=cloud){this.kind=kind;this.day=day;cloud=cloudiness;invalidate();}
    }
    @Override protected void onDraw(Canvas c){
        if(kind.equals("unknown"))return;
        float s=Math.min(getWidth(),getHeight())/100f;
        int save=c.save();c.translate((getWidth()-100*s)/2f,(getHeight()-100*s)/2f);c.scale(s,s);
        p.setShadowLayer(6,0,1,0x55000000);
        boolean clear=kind.equals("clear"), partly=kind.equals("cloud")&&cloud<0.9f;
        if(clear)body(c,50,50,1f);
        else if(partly)body(c,64,36,0.72f);
        if(!clear){
            float cy=kind.equals("cloud")?(partly?60:54):44;
            cloud(c,46,cy,kind.equals("storm")?0xffc3cad6:0xfff1f4f8);
        }
        p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
        if(kind.equals("rain")){
            p.setColor(0xffa8d5f2);p.setStrokeWidth(5);
            for(int i=0;i<3;i++){float x=30+i*18;c.drawLine(x,72,x-5,88,p);}
        }else if(kind.equals("snow")){
            p.setStyle(Paint.Style.FILL);p.setColor(0xffffffff);
            for(int i=0;i<3;i++){c.drawCircle(30+i*18,80+(i%2)*6,4.5f,p);}
        }else if(kind.equals("storm")){
            p.setStyle(Paint.Style.FILL);p.setColor(0xfff6c457);
            Path bolt=new Path();bolt.moveTo(52,62);bolt.lineTo(38,84);bolt.lineTo(49,84);bolt.lineTo(43,98);bolt.lineTo(62,74);bolt.lineTo(51,74);bolt.lineTo(58,62);bolt.close();c.drawPath(bolt,p);
        }else if(kind.equals("fog")){
            p.setColor(0xffe4e9ef);p.setStrokeWidth(6);
            c.drawLine(18,74,82,74,p);c.drawLine(26,88,74,88,p);
        }
        p.setStyle(Paint.Style.FILL);
        c.restoreToCount(save);
    }
    private void body(Canvas c,float x,float y,float k){
        p.setStyle(Paint.Style.FILL);
        if(day){
            p.setColor(0xfff7c96a);c.drawCircle(x,y,20*k,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5*k);p.setStrokeCap(Paint.Cap.ROUND);
            for(int i=0;i<8;i++){double a=Math.PI/4*i;float r1=29*k,r2=39*k;c.drawLine(x+(float)Math.cos(a)*r1,y+(float)Math.sin(a)*r1,x+(float)Math.cos(a)*r2,y+(float)Math.sin(a)*r2,p);}
            p.setStyle(Paint.Style.FILL);
        }else{
            Path moon=new Path();moon.addCircle(x,y,26*k,Path.Direction.CW);
            Path bite=new Path();bite.addCircle(x+13*k,y-9*k,22*k,Path.Direction.CW);moon.op(bite,Path.Op.DIFFERENCE);
            p.setColor(0xffe8eef4);c.drawPath(moon,p);
        }
    }
    private void cloud(Canvas c,float x,float y,int color){
        p.setStyle(Paint.Style.FILL);p.setColor(color);
        c.drawRoundRect(x-34,y,x+36,y+24,12,12,p);
        c.drawCircle(x-12,y+2,19,p);c.drawCircle(x+13,y+4,15,p);
    }
}
