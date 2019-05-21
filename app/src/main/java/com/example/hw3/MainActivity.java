package com.example.hw3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.MediaStore;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.StrictMath.abs;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    static public SensorManager mSensorManager;
    static Sensor SensorL;

    private ImageView emptyBall;
    private ImageView ball;
    private String[] answers;
    private TextView AnswerText;

    private int screenWidth;
    private int screenHeight;
    private int imgEdgeSize;
    private boolean animFlag = false;
    private boolean layout_ready = false;
    private ConstraintLayout MainContainer;
    private long lastUpdate = -1;
    protected boolean start = false;
    private int random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            SensorL = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            ball = findViewById(R.id.ball);
            emptyBall = findViewById(R.id.EmptyBall);
            AnswerText = findViewById(R.id.BallText);
            if (SensorL != null){
                AnswerText.setVisibility(View.INVISIBLE);
                emptyBall.setVisibility(View.INVISIBLE);
                ball.setVisibility(View.VISIBLE);
            }else {
                Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "Oh no! No light sensor!", Toast.LENGTH_SHORT).show();
        }

        layout_ready = false;
        MainContainer = findViewById(R.id.ActivityMain);
        MainContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                screenWidth = MainContainer.getWidth();
                screenHeight = MainContainer.getHeight();
                imgEdgeSize = emptyBall.getWidth();
                MainContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                layout_ready = true;
            }
        });

        answers = getResources().getStringArray(R.array.Answers);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (lastUpdate == -1){
            lastUpdate = event.timestamp;
        }else {
            lastUpdate = event.timestamp;
        }

        if (layout_ready)
            handleLightSensor(event.values[0], lastUpdate);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void handleLightSensor(final float LSensorValue, final long lUpdate){
        if (!animFlag && (lUpdate != -1)){
            if (abs(LSensorValue) < 5){
                animFlag = true;
                start = true;

                emptyBall.setVisibility(View.INVISIBLE);
                AnswerText.setVisibility(View.INVISIBLE);
                ball.setVisibility(View.VISIBLE);

                FlingAnimation flingX = new FlingAnimation(ball, DynamicAnimation.X);
                flingX.setStartVelocity(-1 * LSensorValue * screenWidth / 2f).setMinValue(5)
                        .setMaxValue(screenWidth - imgEdgeSize - 5).setFriction(1f);
                flingX.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean b, float v, float v1) {
                        if (v1 != 0){
                            final FlingAnimation reflingX = new FlingAnimation(ball, DynamicAnimation.X);
                            reflingX.setStartVelocity(-1 * v1).setMinValue(5).setMaxValue(screenWidth - imgEdgeSize - 5)
                                    .setFriction(1.25f).start();

                            reflingX.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                                @Override
                                public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean b, float v, float v1) {
                                    animFlag = false;
                                    random = ((int) abs(lUpdate * 100)) % 20;
                                }
                            });
                        }else {
                            animFlag = false;
                        }
                    }
                });
                flingX.start();
            }else if (start){
                ball.setVisibility(View.INVISIBLE);
                emptyBall.setVisibility(View.VISIBLE);
                AnswerText.setVisibility(View.VISIBLE);
                TextView SelectedAnswer = findViewById(R.id.BallText);
                SelectedAnswer.setText(answers[random]);
                start = false;
            }
        }
    }

    @Override
    protected void onResume (){
        super.onResume();

        if (SensorL != null)
            mSensorManager.registerListener(this, SensorL, 100000);
    }

    @Override
    protected void onPause (){
        super.onPause();

        if (SensorL != null){
            mSensorManager.unregisterListener(this, SensorL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (SensorL != null)
            mSensorManager.unregisterListener(this, SensorL);
    }
}
