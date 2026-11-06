package local.reroute.youtubehome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.View;

/**
 * A quiet view across the Strait: sky, sea and the sun or moon at their real position for
 * Tangier, tinted by weather. Rendered once into a bitmap whenever the state changes; there is
 * no render loop. A state change cross-fades using the system animator scale (instant if off).
 */
final class SkyView extends View {
    static final float W=1280f,H=720f,HORIZON=500f;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
    private String phase="night",kind="unknown";
    private float cloud,progress;
    private Bitmap current,previous;
    private float fade=1f;
    private ValueAnimator animator;
    private String key="";

    SkyView(Context context){super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}

    /** progress is the position along today's daylight or night arc (0..1). */
    void update(String phase,String kind,float cloudiness,float progress){
        this.phase=phase;this.kind=kind;this.cloud=cloudiness;this.progress=Math.round(progress*48f)/48f;
        // Cross-fade only while actually on screen; offscreen or before attach, switch instantly.
        rebuild(isAttachedToWindow()&&getWindowVisibility()==VISIBLE);
    }
    @Override protected void onSizeChanged(int w,int h,int ow,int oh){super.onSizeChanged(w,h,ow,oh);key="";rebuild(false);}
    private void rebuild(boolean animate){
        if(getWidth()<=0||getHeight()<=0)return;
        String next=phase+"|"+kind+"|"+cloud+"|"+progress+"|"+getWidth()+"x"+getHeight();
        if(next.equals(key))return;
        key=next;
        Bitmap bitmap=Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(bitmap);c.scale(getWidth()/W,getHeight()/H);paint(c);
        if(animator!=null)animator.cancel();
        if(previous!=null&&previous!=current)previous.recycle();
        previous=animate?current:null;
        if(!animate&&current!=null)current.recycle();
        current=bitmap;fade=previous==null?1f:0f;
        if(previous!=null){
            animator=ValueAnimator.ofFloat(0f,1f);animator.setDuration(1600);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){@Override public void onAnimationUpdate(ValueAnimator a){
                fade=(Float)a.getAnimatedValue();
                if(fade>=1f&&previous!=null){previous.recycle();previous=null;}
                invalidate();
            }});
            animator.start();
        }
        invalidate();
    }
    @Override protected void onDetachedFromWindow(){if(animator!=null)animator.cancel();super.onDetachedFromWindow();}
    @Override protected void onDraw(Canvas c){
        if(current==null){rebuild(false);if(current==null)return;}
        p.setAlpha(255);
        if(previous!=null&&fade<1f){c.drawBitmap(previous,0,0,p);p.setAlpha(Math.round(fade*255));}
        c.drawBitmap(current,0,0,p);p.setAlpha(255);
    }

    // ---------------------------------------------------------------------------------------
    private void paint(Canvas c){
        boolean night=phase.equals("night"),low=phase.equals("dawn")||phase.equals("dusk"),day=phase.equals("day");
        boolean wet=kind.equals("rain")||kind.equals("storm"),fog=kind.equals("fog"),snow=kind.equals("snow"),unknown=kind.equals("unknown");
        float cover=wet||fog||snow?1f:kind.equals("clear")?0f:cloud;
        int[] s=Model.skyPalette(phase,kind,cloud);
        int zenith=s[0],mid=s[1],horizon=s[2];

        // Sky
        p.setShader(new LinearGradient(0,0,0,HORIZON,new int[]{zenith,mid,horizon},new float[]{0f,0.62f,1f},Shader.TileMode.CLAMP));
        c.drawRect(0,0,W,HORIZON+1,p);p.setShader(null);

        // Stars on clear-ish nights only.
        if(night&&!unknown&&!wet&&!fog&&!snow&&cover<0.7f){
            int seed=7;int count=Math.round(80*(1f-cover));
            for(int i=0;i<count;i++){
                seed=seed*1103515245+12345;float x=((seed>>>8)%1280);
                seed=seed*1103515245+12345;float y=((seed>>>8)%420);
                seed=seed*1103515245+12345;int a=60+((seed>>>8)%110);
                p.setColor(Color.argb(Math.round(a*(1f-y/520f)),232,238,248));
                c.drawCircle(x,y,i%9==0?1.7f:1.05f,p);
            }
        }

        // Sun or moon along its real arc: rises on the right (east), sets on the left (west).
        float bx=1110f-940f*progress, by=HORIZON-(float)Math.sin(Math.PI*progress)*185f;
        float veil=unknown?0.35f:fog?0.5f:wet||snow?0.3f:1f-0.6f*cover;
        if(night){
            int moon=0xffe9eef3;
            glow(c,bx,by,150,Color.argb(Math.round(55*veil),200,214,236),1f);
            if(!unknown&&veil>0.45f){
                Path disc=new Path();disc.addCircle(bx,by,25,Path.Direction.CW);
                Path bite=new Path();bite.addCircle(bx+12,by-7,22,Path.Direction.CW);
                disc.op(bite,Path.Op.DIFFERENCE);
                p.setColor(moon);p.setAlpha(Math.round(255*Math.min(1f,veil+0.2f)));c.drawPath(disc,p);p.setAlpha(255);
            }
        }else{
            int warm=low?0xffffae78:0xfff9dba4;
            glow(c,bx,by,low?330:260,(Math.round((low?150:95)*veil)<<24)|(warm&0xffffff),low?1.7f:1f);
            if(!unknown&&veil>0.5f){
                int save=c.save();c.clipRect(0,0,W,HORIZON);
                p.setShader(new RadialGradient(bx,by,low?40:34,new int[]{low?0xfffcca9c:0xfffff8e4,low?0xfff4a173:0xfffbdc9b},null,Shader.TileMode.CLAMP));
                p.setAlpha(veil>=0.6f?255:200);c.drawCircle(bx,by,low?40:34,p);p.setShader(null);p.setAlpha(255);
                c.restoreToCount(save);
            }
        }

        // Soft cloud banks: layered radial blobs, no outlines.
        if(!unknown&&(cover>0.05f)){
            int tint=night?Model.lighten(mid,0.16f):low?Model.mix(Model.lighten(mid,0.3f),horizon,0.35f):Model.lighten(mid,0.42f);
            if(wet)tint=Model.lighten(mid,night?0.1f:0.25f);
            int banks=2+Math.round(cover*5);
            int seed=31;
            for(int b=0;b<banks;b++){
                seed=seed*1103515245+12345;float cx=80+((seed>>>8)%1120);
                seed=seed*1103515245+12345;float cy=250+((seed>>>8)%190);
                cloudBank(c,cx,cy,0.8f+0.5f*((b*37)%10)/10f,tint,Math.round((wet?95:snow?80:60)*(0.45f+cover*0.55f)));
            }
            if(cover>0.8f||wet){ // low overcast ceiling
                p.setShader(new LinearGradient(0,0,0,300,new int[]{Color.argb(wet?70:45,ch(tint,16),ch(tint,8),ch(tint,0)),0},null,Shader.TileMode.CLAMP));
                c.drawRect(0,0,W,300,p);p.setShader(null);
            }
        }

        // Sea
        int seaTop=Model.scale(Model.mix(horizon,mid,0.55f),0.72f), seaBottom=Model.scale(zenith,0.78f);
        p.setShader(new LinearGradient(0,HORIZON,0,H,seaTop,seaBottom,Shader.TileMode.CLAMP));
        c.drawRect(0,HORIZON,W,H,p);p.setShader(null);

        // Far coast across the Strait (right) and the Tangier headland (left).
        int land=Model.scale(Model.mix(mid,horizon,0.35f),night?0.7f:0.62f);
        Path coast=new Path();coast.moveTo(640,HORIZON+1);
        coast.cubicTo(760,HORIZON-6,820,HORIZON-20,905,HORIZON-17);coast.cubicTo(980,HORIZON-14,1010,HORIZON-31,1075,HORIZON-29);
        coast.cubicTo(1150,HORIZON-27,1200,HORIZON-12,1280,HORIZON-15);coast.lineTo(1280,HORIZON+1);coast.close();
        p.setColor(land);p.setAlpha(fog?70:200);c.drawPath(coast,p);
        Path cape=new Path();cape.moveTo(0,HORIZON-34);cape.cubicTo(70,HORIZON-40,150,HORIZON-22,230,HORIZON-8);cape.cubicTo(270,HORIZON-2,300,HORIZON,330,HORIZON+1);cape.lineTo(0,HORIZON+1);cape.close();
        p.setColor(Model.scale(land,0.8f));p.setAlpha(fog?90:230);c.drawPath(cape,p);p.setAlpha(255);
        p.setColor(Model.lighten(horizon,0.3f));p.setAlpha(fog?20:night?45:70);c.drawRect(330,HORIZON-0.5f,W,HORIZON+1f,p);p.setAlpha(255);

        // Soft light on the water beneath the sun or moon.
        if(!unknown&&veil>0.45f){
            int light=night?0xffdfe8f3:low?0xffffb582:0xfffbe2b0;
            int a=Math.round((night?60:low?120:85)*veil);
            int save=c.save();c.clipRect(0,HORIZON,W,H);c.translate(bx,HORIZON);c.scale(0.42f,1f);
            p.setShader(new RadialGradient(0,0,night?150:low?230:190,new int[]{(a<<24)|(light&0xffffff),(a/3<<24)|(light&0xffffff),0},new float[]{0f,0.45f,1f},Shader.TileMode.CLAMP));
            c.drawRect(-600,0,600,H-HORIZON,p);p.setShader(null);c.restoreToCount(save);
            for(int i=0;i<4;i++){
                float y=HORIZON+14+i*i*9+i*10,half=(night?16:26)+i*14;
                p.setColor((Math.round(a*0.45f*(1f-i/4f))<<24)|(light&0xffffff));
                c.drawRoundRect(bx-half,y,bx+half,y+1.6f,1,1,p);
            }
        }

        // Precipitation (static).
        if(wet){
            boolean storm=kind.equals("storm");
            p.setStrokeWidth(1.6f);p.setStrokeCap(Paint.Cap.ROUND);
            int seed=97;int drops=storm?320:220;
            for(int i=0;i<drops;i++){
                seed=seed*1103515245+12345;float x=(seed>>>8)%1300;
                seed=seed*1103515245+12345;float y=(seed>>>8)%720;
                float len=14+(i%5)*3;
                // Lighter in the information band so the streaks never compete with the text.
                int a=storm?50:38;if(y<300)a=a*(40+Math.round(y/5))/100;
                p.setColor(Color.argb(a,214,228,240));
                c.drawLine(x,y,x-len*0.28f,y+len,p);
            }
        }else if(snow){
            int seed=53;
            for(int i=0;i<170;i++){
                seed=seed*1103515245+12345;float x=(seed>>>8)%1280;
                seed=seed*1103515245+12345;float y=(seed>>>8)%720;
                p.setColor(Color.argb(110+(i%4)*25,245,248,252));
                c.drawCircle(x,y,1.4f+(i%4)*0.7f,p);
            }
        }
        if(fog){
            for(int i=0;i<4;i++){
                float y=330+i*62;
                p.setShader(new LinearGradient(0,y-60,0,y+60,new int[]{0,Color.argb(60+i*10,226,232,238),0},null,Shader.TileMode.CLAMP));
                c.drawRect(0,y-60,W,y+60,p);
            }
            p.setShader(null);
        }

        // Gentle top shade keeps the information band legible over any sky.
        p.setShader(new LinearGradient(0,0,0,300,0x55000000,0,Shader.TileMode.CLAMP));
        c.drawRect(0,0,W,300,p);p.setShader(null);
    }
    private void glow(Canvas c,float x,float y,float r,int color,float stretch){
        int save=c.save();c.translate(x,y);c.scale(stretch,1f);
        p.setShader(new RadialGradient(0,0,r,new int[]{color,(Color.alpha(color)/3<<24)|(color&0xffffff),0},new float[]{0f,0.35f,1f},Shader.TileMode.CLAMP));
        c.drawCircle(0,0,r,p);p.setShader(null);c.restoreToCount(save);
    }
    private void cloudBank(Canvas c,float x,float y,float scale,int tint,int alpha){
        float[][] blobs={{-120,10,70},{-55,-12,82},{20,-20,92},{95,-4,76},{160,14,58},{40,20,90},{-40,24,80}};
        for(float[] b:blobs){
            float r=b[2]*scale;
            p.setShader(new RadialGradient(x+b[0]*scale,y+b[1]*scale,r,new int[]{(alpha<<24)|(tint&0xffffff),(alpha/2<<24)|(tint&0xffffff),0},new float[]{0f,0.55f,1f},Shader.TileMode.CLAMP));
            c.drawCircle(x+b[0]*scale,y+b[1]*scale,r,p);
        }
        p.setShader(null);
    }
    private static int ch(int c,int shift){return (c>>shift)&0xff;}
}
