package com.github.mccxj.igo.view;

import static com.github.mccxj.go.GameConstants.BLACK;
import static com.github.mccxj.go.GameConstants.SIZE;
import static com.github.mccxj.go.GameConstants.WHITE;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.github.mccxj.go.GoGame;
import com.github.mccxj.go.sgf.SGFGame;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Paint paint = new Paint();
    public GoGame goGame;
    private float oX = -1f;// 左上角默认的x坐标
    private float oY = -1f;// 左上角默认的y坐标
    private int oS = -1;// 棋子默认的大小
    private float mX = -1;// 右下角默认的x坐标
    private float mY = -1;// 右下角默认的y坐标
    private float iX = 20f;
    private float iY = 20f;
    private int iS = -1;
    private int radius = 3;
    private volatile boolean isLoop = true;

    private GestureDetector gd;
    private ScaleGestureDetector sgd;
    private SurfaceHolder holder;

    public GameView(Context context) {
        super(context);

        holder = getHolder();
        holder.addCallback(this);
        this.setFocusable(true);
        goGame = new GoGame();
        paint.setAntiAlias(true);
        gd = new GestureDetector(context, new GameGestureListener());
        sgd = new ScaleGestureDetector(context, new GameScaleGestureListener());
    }

    protected void draw() {
        Canvas canvas = holder.lockCanvas();

        paint.setColor(Color.YELLOW);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        if (iS == -1) {
            oS = iS = getWidth() / 20;
            oX = iX = (getWidth() - (SIZE - 1) * iS) / 2;
            oY = iY = iX;
            mX = oX + (SIZE - 1) * oS;
            mY = oY + (SIZE - 1) * oS;

            iX = iX - 40;
            iY = iY - 50;
            iS = iS * 2;
        }
        paint.setColor(Color.BLACK);

        canvas.translate(iX, iY);
        for (int i = 0; i < SIZE; i++) {
            canvas.drawLine(0, iS * i, iS * (SIZE - 1), iS * i, paint);
            canvas.drawLine(iS * i, 0, iS * i, iS * (SIZE - 1), paint);
        }

        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(iS * 3, iS * 3, radius, paint);
        canvas.drawCircle(iS * 3, iS * 15, radius, paint);
        canvas.drawCircle(iS * 15, iS * 3, radius, paint);
        canvas.drawCircle(iS * 15, iS * 15, radius, paint);
        canvas.drawCircle(iS * 9, iS * 9, radius, paint);

        int[][] stones = goGame.getStones();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (BLACK == stones[i][j]) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.BLACK);
                    canvas.drawCircle(iS * i, iS * j, iS / 2, paint);
                } else if (WHITE == stones[i][j]) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(iS * i, iS * j, iS / 2, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.BLACK);
                    canvas.drawCircle(iS * i, iS * j, iS / 2, paint);
                }
            }
        }
        canvas.restore();
        holder.unlockCanvasAndPost(canvas);
    }

    public void setSGF(SGFGame game) {
        goGame.setSGF(game);
        goGame.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointCount = event.getPointerCount();
        if (pointCount == 1)
            return gd.onTouchEvent(event);
        else
            return sgd.onTouchEvent(event);
    }

    // if (MotionEvent.ACTION_UP == event.getAction()) {
    // int posX = (int) Math.round((event.getX() - iX) / s);
    // int posY = (int) Math.round((event.getY() - iY) / s);
    // int result = goGame.next(posX, posY);
    // if (result != INVALID) {
    // if (goGame.addStone(posX, posY)) {
    // if (result == SUCC)
    // System.out.println("SUCC");
    // else if (result == FAIL)
    // System.out.println("FAIL");
    // else {
    // if (result >= 1000)
    // result -= 1000;
    // goGame.addStone(result / SIZE, result % SIZE);
    // }
    // postInvalidate();
    // }
    // }
    // }
    // return true;
    // }

    private class GameGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            iX -= distanceX;
            // 往右边移动
            if (distanceX <= 0) {
                // 判断是否超出左边
                if (iX >= oX)
                    iX = oX;
            } else {
                float zX = iX + (SIZE - 1) * iS;
                zX -= distanceX;
                // 判断是否超出右边
                if (zX <= mX)
                    iX += mX - zX;
            }

            iY -= distanceY;
            // 往下边移动
            if (distanceY <= 0) {
                // 判断是否超出上边
                if (iY >= oY)
                    iY = oY;
            } else {
                float zY = iY + (SIZE - 1) * iS;
                zY -= distanceX;
                // 判断是否超出下边
                if (zY <= mY)
                    iY += mY - zY;
            }
            return true;
        }
    }

    private class GameScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            final float factor = detector.getScaleFactor();
            Log.d("IGO", "factor: " + factor);
            // s *= factor;
            return true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isLoop = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isLoop = false;
    }

    @Override
    public void run() {
        while (isLoop) {
            try {
                Thread.sleep(50L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (holder) {
                draw();
            }
        }
    }
}
