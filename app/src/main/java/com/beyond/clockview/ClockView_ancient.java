package com.beyond.clockview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2017/1/13.
 */
public class ClockView_ancient extends View {
    private Thread refreshThread;
    private float mWidth=1000;
    private float mHeight=1000;
    private double millSecond,second,minute,hour;
    private float width_hour;
    private float density_hour;
    private float refresh_time;
    private Bitmap clockDial;
    private Bitmap new_clockDial;
    private float newWidth;
    private float newHeight;
    private float width_circle;
    private float radius_center = 15;//表盘正中心的半径长度 radius_center
    private float width_minute;
    private float density_minute;
    private float density_second;
    private float width_second;
    private float text_size;
    private float density_text;

    public ClockView_ancient(Context context){this(context,null,0);}

    //读取资源
    public ClockView_ancient(Context context, AttributeSet attrs){
        this(context,attrs,0);
        Resources r=this.getContext().getResources();
        clockDial= BitmapFactory.decodeResource(r,R.drawable.clockdial_ancient);
    }

    //设置属性
    public ClockView_ancient(Context context,AttributeSet attrs,int defStyleAttr){
        super(context,attrs,defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.clock);
        width_hour = ta.getDimension(R.styleable.clock_width_hour, 20);//时针宽度
        width_minute=ta.getDimension(R.styleable.clock_density_minute,15);//分针宽度
        width_second=ta.getDimension(R.styleable.clock_density_second,10);//秒针宽度
        density_hour = ta.getFloat(R.styleable.clock_density_hour, 0.35f);//时针长度比例
        density_minute=ta.getFloat(R.styleable.clock_density_minute,0.50f);//分针长度比例
        density_second=ta.getFloat(R.styleable.clock_density_second,0.70f);//秒针长度比例
        refresh_time = ta.getFloat(R.styleable.clock_refresh_time, 1000);
        text_size = ta.getDimension(R.styleable.clock_text_size, 50);//表盘中文字大小
        density_text=ta.getFloat(R.styleable.clock_density_second,0.50f);
        ta.recycle();
    }
    private void init(){refreshThread=new Thread();}

    //测量屏幕大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //添加了适应wrap_content的界面计算
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int) mWidth, (int) mHeight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int) mWidth, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, (int) mHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取宽高参数
        this.mWidth = Math.min(getWidth(), getHeight());
//        this.mWidth = getWidth();
//        this.mHeight = getHeight();
        this.mHeight = Math.max(getWidth(), getHeight());
        //获取时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        millSecond = calendar.get(Calendar.MILLISECOND);
        second = calendar.get(Calendar.SECOND);
        minute = calendar.get(Calendar.MINUTE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);//二十四小时制

        /*Paint paintCircle = new Paint();
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setAntiAlias(true);
        paintCircle.setStrokeWidth(width_circle);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2 - width_circle, paintCircle);
        */
        //建立背景图
        Paint ClockDial_ancient_paint=new Paint();
        drawClockDial(canvas,ClockDial_ancient_paint);

        //表盘文字选择
        Paint paintDegree=new Paint();
        paintDegree.setTextSize(text_size);
        String targetText[] = getContext().getResources().getStringArray(R.array.clock_60);

        //绘制时间文字
        /*float startX = mWidth / 2 - paintDegree.measureText(targetText[1]) / 2;
        float startY = mHeight / 2 - mWidth / 2 + 120;
        float textR = (float) Math.sqrt(Math.pow(mWidth / 2 - startX, 2) + Math.pow(mHeight / 2 - startY, 2))*density_text;

        for (int i = 0; i < 12; i++) {
            float x = (float) (startX + Math.sin(Math.PI / 6 * i) * textR);
            float y = (float) (startY + textR - Math.cos(Math.PI / 6 * i) * textR);
            if (i != 11 && i != 10 && i != 0) {
                y = y + paintDegree.measureText(targetText[i]) / 2;
            } else {
                x = x - paintDegree.measureText(targetText[i]) / 4;
                y = y + paintDegree.measureText(targetText[i]) / 4;
            }
            canvas.drawText(targetText[i], x, y, paintDegree);
        }
        */
        paintDegree.setARGB(1000,0,0,0);
        paintDegree.setFakeBoldText(true);
        float startX = mWidth / 2 - paintDegree.measureText(targetText[0]) / 2 ;//
        float startY = mHeight / 2 - mWidth / 2*density_text;
        float textR = (float) Math.sqrt(Math.pow(mWidth / 2 - startX, 2) + Math.pow(mHeight / 2 - startY, 2));

        for (int i = 0; i < 12; i++) {
            float x = (float) (startX + Math.sin(Math.PI / 6 * i) * textR);
            float y = (float) (startY + textR - Math.cos(Math.PI / 6 * i) * textR);
            if (i != 11 && i != 10 && i != 0) {
                y = y + paintDegree.measureText(targetText[i]) / 2;
            } else {
                x = x - paintDegree.measureText(targetText[i]) / 4;
                y = y + paintDegree.measureText(targetText[i]) / 4;
            }
            canvas.drawText(targetText[i], x, y, paintDegree);
        }
        //秒针要在分针之前，分针要在时针之前。不然分针会跳
        //加入秒针
        Paint paintSecond = new Paint();
        paintSecond.setAntiAlias(true);
        paintSecond.setStrokeWidth(width_second);
        paintSecond.setColor(Color.BLUE);
        drawSecond(canvas, paintSecond);

        //加入分针
        Paint paintMinute = new Paint();
        paintMinute.setAntiAlias(true);
        paintMinute.setStrokeWidth(width_minute);
        paintMinute.setColor(Color.RED);
        drawMinute(canvas, paintMinute);

        //加入时针
        Paint hour_paint=new Paint();
        hour_paint.setAntiAlias(true);
        hour_paint.setStrokeWidth(width_hour);
        hour_paint.setColor(getResources().getColor(R.color.colorYellow));
        drawHour(canvas, hour_paint);

        //加入圆心
        Paint paintPointer = new Paint();
        paintPointer.setAntiAlias(true);
        paintPointer.setStyle(Paint.Style.FILL);
        paintPointer.setColor(getResources().getColor(R.color.colorYellow));
        canvas.drawCircle(mWidth / 2, mHeight / 2, radius_center, paintPointer);
    }

    //绘制背景图
    private void drawClockDial(Canvas canvas,Paint paint){
        int bWidth=clockDial.getWidth();
        int bHeight=clockDial.getHeight();
        //System.out.println("mwidth="+mWidth);
        //System.out.println("bWidth="+bWidth);
        float newWidth=mWidth;
        float newHeight=mWidth;
        float scaleWidth=newWidth/bWidth;
        float scaleHeight=newHeight/bHeight;
        Matrix matrix=new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);
        new_clockDial=Bitmap.createBitmap(clockDial,0,0,bWidth,bHeight,matrix,true);
        canvas.drawBitmap(new_clockDial,0,mHeight/2-newHeight/2,paint);
    }

    //绘制时针
    private void drawHour(Canvas canvas,Paint paint){
        float degreeHour = (float) hour * 360 / 24;
        float degreeMinute = (float) minute / 60 * 360 / 24;
        float degree = degreeHour + degreeMinute;
        canvas.rotate(degree, mWidth / 2, mHeight / 2);
        canvas.drawLine(mWidth / 2, mHeight / 2, mWidth / 2, mHeight / 2 - (mWidth/2 * density_hour), paint);
        canvas.rotate(-degree, mWidth / 2, mHeight / 2);
    }

    //绘制分针
    private void drawMinute(Canvas canvas,Paint paint){
        float degreeMinute = (float) minute * 360 / 60;
        float degreeSecond = (float) second / 60 / 60 * 360;
        float degree = degreeMinute + degreeSecond;
        canvas.rotate(degree, mWidth / 2, mHeight / 2);
        canvas.drawLine(mWidth / 2, mHeight / 2, mWidth / 2, mHeight / 2 - (mWidth/2 * density_minute), paint);
        canvas.rotate(-degree, mWidth / 2, mHeight / 2);

    }

    //绘制秒针
    private void drawSecond(Canvas canvas,Paint paint) {
        float degreeSecond = (float) second / 60  * 360;
        float degree =degreeSecond;
        canvas.rotate(degree, mWidth / 2, mHeight / 2);
        canvas.drawLine(mWidth / 2, mHeight / 2, mWidth / 2, mHeight / 2 - (mWidth / 2 * density_second), paint);
        canvas.rotate(-degree, mWidth / 2, mHeight / 2);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //在添加到Activity的时候启动线程
        refreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //设置更新界面的刷新时间
                    SystemClock.sleep((long) refresh_time);
                    postInvalidate();
                }
            }
        });
        refreshThread.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //停止刷新线程
        refreshThread.interrupt();
    }

}
