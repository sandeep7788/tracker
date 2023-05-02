package com.vline.activity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.vline.R;

import java.lang.ref.WeakReference;

/**
 * Example activity to manage a long-running timer, which survives the destruction of the activity
 * by using a foreground service and notification
 * <p>
 * Add the following to the manifest:
 * <service android:name=".MainActivity$TimerService" android:exported="false" />
 */

public class StopwatchActivity extends AppCompatActivity {

    private static final String TAG = "@@@@";
    // Message type for the handler
    private final static int MSG_UPDATE_TIME = 0;
    // Handler to update the UI every second when the timer is running
    private final Handler mUpdateTimeHandler = new UIUpdateHandler(this);
    private TimerService timerService;
    private boolean serviceBound;
    private Button timerButton;
    private TextView timerTextView;
    /**
     * Callback for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service bound");
            }
            TimerService.RunServiceBinder binder = (TimerService.RunServiceBinder) service;
            timerService = binder.getService();
            serviceBound = true;
            // Ensure the service is not in the foreground when bound
            timerService.background();
            // Update the UI if the service is already running the timer
            if (timerService.isTimerRunning()) {
                updateUIStartRun();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service disconnect");
            }
            serviceBound = false;
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        timerButton = (Button) findViewById(R.id.start_button);
        timerTextView = (TextView) findViewById(R.id.time_view);

        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runButtonClick(view);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("startTimer MSG_UPDATE_TIME  "+MSG_UPDATE_TIME);
        System.out.println("startTimer endTime MSG_UPDATE_TIME ");

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Starting and binding service");
        }
        Intent i = new Intent(this, TimerService.class);
        startService(i);
        bindService(i, mConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("startTimer onStop  "+MSG_UPDATE_TIME);
        System.out.println("startTimer endTime onStop ");

        updateUIStopRun();
        if (serviceBound) {
            // If a timer is active, foreground the service, otherwise kill the service
            if (timerService.isTimerRunning()) {
                timerService.foreground();
            } else {
                stopService(new Intent(this, TimerService.class));

            }
            // Unbind the service
            unbindService(mConnection);
            serviceBound = false;
        }
    }

    public void runButtonClick(View v) {
        if (serviceBound && !timerService.isTimerRunning()) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Starting timer");
            }
            timerService.startTimer();
            updateUIStartRun();
        } else if (serviceBound && timerService.isTimerRunning()) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Stopping timer");
            }
            timerService.stopTimer();
            updateUIStopRun();
        }
    }

    /**
     * Updates the UI when a run starts
     */
    private void updateUIStartRun() {
        mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
        timerButton.setText("stoped");
    }

    /**
     * Updates the UI when a run stops
     */
    private void updateUIStopRun() {
        mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
        timerButton.setText("start");
    }

    /**
     * Updates the timer readout in the UI; the service must be bound
     */
    private void updateUITimer() {
        if (serviceBound) {
            timerTextView.setText(timerService.elapsedTime() + " seconds");
        }
    }

    /**
     * When the timer is running, use this handler to update
     * the UI every second to show timer progress
     */
    static class UIUpdateHandler extends Handler {

        private final static int UPDATE_RATE_MS = 1000;
        private final WeakReference<StopwatchActivity> activity;

        UIUpdateHandler(StopwatchActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (MSG_UPDATE_TIME == message.what) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "updating time");
                }
                activity.get().updateUITimer();
                sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_RATE_MS);
            }
        }
    }

    /**
     * Timer service tracks the start and end time of timer; service can be placed into the
     * foreground to prevent it being killed when the activity goes away
     */
    public static class TimerService extends Service {

        private static final String TAG = TimerService.class.getSimpleName();
        // Foreground notification id
        private static final int NOTIFICATION_ID = 1;
        // Service binder
        private final IBinder serviceBinder = new RunServiceBinder();
        // Start and end times in milliseconds
        private long startTime, endTime;
        // Is the service tracking time?
        private boolean isTimerRunning;

        @Override
        public void onCreate() {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Creating service");
            }
            startTime = 0;
            endTime = 0;
            isTimerRunning = false;

            System.out.println("startTime  "+startTime);
            System.out.println("endTime  "+endTime);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Starting service");
            }
            return Service.START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Binding service");
            }
            return serviceBinder;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Destroying service");
            }
        }

        /**
         * Starts the timer
         */
        public void startTimer() {
            System.out.println("startTimer startTime  "+startTime);
            System.out.println("startTimer endTime  "+endTime);
            if (!isTimerRunning) {
                startTime = System.currentTimeMillis();
                isTimerRunning = true;
            } else {
                Log.e(TAG, "startTimer request for an already running timer");
            }
        }

        /**
         * Stops the timer
         */
        public void stopTimer() {
            if (isTimerRunning) {
                endTime = System.currentTimeMillis();
                isTimerRunning = false;
            } else {
                Log.e(TAG, "stopTimer request for a timer that isn't running");
            }
        }

        /**
         * @return whether the timer is running
         */
        public boolean isTimerRunning() {
            return isTimerRunning;
        }

        /**
         * Returns the  elapsed time
         *
         * @return the elapsed time in seconds
         */
        public long elapsedTime() {
            // If the timer is running, the end time will be zero
            return endTime > startTime ?
                    (endTime - startTime) / 1000 :
                    (System.currentTimeMillis() - startTime) / 1000;
        }

        /**
         * Place the service into the foreground
         */
        public void foreground() {
            startForeground(NOTIFICATION_ID, createNotification());
        }

        /**
         * Return the service to the background
         */
        public void background() {
            stopForeground(true);
        }

        /**
         * Creates a notification for placing the service into the foreground
         *
         * @return a notification for interacting with the service when in the foreground
         */
        private Notification createNotification() {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle("Timer Active")
                    .setContentText("Tap to return to the timer")
                    .setSmallIcon(R.mipmap.ic_launcher);

            Intent resultIntent = new Intent(this, StopwatchActivity.class);
//            PendingIntent resultPendingIntent =
//                    PendingIntent.getActivity(this, 0, resultIntent,
//                            PendingIntent.FLAG_UPDATE_CURRENT);
//            builder.setContentIntent(resultPendingIntent);

            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(this,
                        0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            }else {
                pendingIntent = PendingIntent.getActivity(this,
                        0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            }
            builder.setContentIntent(pendingIntent);
            return builder.build();
        }

        public class RunServiceBinder extends Binder {
            TimerService getService() {
                return TimerService.this;
            }
        }
    }

}