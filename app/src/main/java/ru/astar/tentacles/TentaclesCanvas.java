package ru.astar.tentacles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by molot on 26.07.2017.
 */

public class TentaclesCanvas extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread;

    public TentaclesCanvas(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawThread = new DrawThread(getHolder());
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException ie) {}
        }
    }

    class DrawThread extends Thread {

        public static final int S = 10; // кол во щупалец
        public static final int N = 36; // кол во звеньев
        public static final int W = 2;  // ширина щупалец

        private int i, j;
        private double x, y, tx, ty;
        private double k;               // коэффициент поворота
        private double d;

        // углы поворота относительно звеньев
        private double[] a = new double[N];
        private double len;             // длина одного звена
        private Random random;
        private Paint paint;


        private SurfaceHolder surfaceHolder;
        private boolean running = false;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            Canvas canvas;
            paint = new Paint();
            paint.setStrokeWidth(W);

            random = new Random();

            if (getWidth() > getHeight())
                len = getHeight() / 1.8 / N;
            else
                len = getWidth() / 1.8 / N;

            k = random.nextInt(360) * Math.PI / 180;
            d = Math.PI * 2 / S;

            while (running) {
                canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    if (canvas == null)
                        continue;
                    canvas.drawColor(Color.BLACK);

                    if (getWidth() > getHeight())
                        len = getHeight() / 1.8 / N;
                    else
                        len = getWidth() / 1.8 / N;

                    // Расчет коэффициента поворота
                    if (random.nextInt(50) == 0)
                        k = random.nextInt(360) * Math.PI / 180;
                    // Поворот всех щупалец
                    a[1] = a[1] + Math.sin(k) / 15;

                    // интерполяция углов между щупальцами
                    for (i = 2; i < N; i++)
                        a[i] = a[i] + (a[i - 1] - a[i]) * 0.1;
                    for (j = 0; j < S; j++) {
                        x = 0.5 * getWidth();
                        y = 0.5 * getHeight();
                        for (i = 2; i < N; i++) {
                            paint.setColor(Color.rgb(255, Math.round(255 - 255 * i / N), 255));

                            // немного школьной тригонометрии
                            tx = x + Math.sin(j * d + a[i]) * len;
                            ty = y + Math.cos(j * d + a[i]) * len;
                            canvas.drawLine(
                                    (int) Math.round(x),
                                    (int) Math.round(y),
                                    (int) Math.round(tx),
                                    (int) Math.round(ty), paint);
                            x = tx;
                            y = ty;

                        }
                    }

                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}

