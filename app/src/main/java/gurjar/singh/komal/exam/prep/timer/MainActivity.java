package gurjar.singh.komal.exam.prep.timer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
//import com.google.android.material.button.MaterialButton;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
	
	private static final int PERMISSION_REQUEST_CODE = 1234;
	
	private WindowManager windowManager;
	private LinearLayout chatHead;
	private TextView textView;
	
	private boolean isStop=false;
	private boolean isRunning = false;
	private long startTime, elapsedTime;
	private Handler handler = new Handler();
	
	Switch switchWidget;
	
	private int timerSize;
	
	private Button removeButton;
	private Button startStopButton;
	private Button resetButton;
	
	private boolean enableMillisec=true;
	
	MyDatabaseHelper dbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		
		
		 
		
		uiInit();
		checkOverlayPermission();
		
		

	}
	
	
	private void init(){
		dbHelper = new MyDatabaseHelper(this);
		// Retrieving a boolean value
enableMillisec = dbHelper.getBoolean("enableMillisec", true); // Default value if key not found
		timerSize=dbHelper.getInt("timerSize", 35);
	}
	private void uiInit(){
		

SeekBar seekBar = findViewById(R.id.seekBar);

seekBar.setProgress(timerSize);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Calculate new text size based on seek bar progress
                //timerSize = 10 + (int) progress * 2; // Change 10 and 2 according to your preference
				
                timerSize=progress;
				textView.setTextSize(timerSize);
				//dbHelper.storeInt("timerSize", timerSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed for this example
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed for this example
				dbHelper.storeInt("timerSize", timerSize);
            }
        });

		
	



		switchWidget = findViewById(R.id.switch_enable_milliseconds);
        switchWidget.setChecked(enableMillisec);
		
		switchWidget.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        enableMillisec = isChecked;
		if (!isRunning){
			if(!isStop){
			resetTimer();
			}
		}
		dbHelper.storeBoolean("enableMillisec", enableMillisec);
		
    }
});


		removeButton = findViewById(R.id.removeButton);
		removeButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Actions to perform when the button is clicked
        finish();
    }
      });

startStopButton = findViewById(R.id.startStopButton);

startStopButton.setOnClickListener(new View.OnClickListener() {
	@Override
	public void onClick(View v) {
		if (isRunning) {
			stopTimer();
			startStopButton.setText("Start");
			startStopButton.setTextColor(Color.parseColor("#55FF55")); // Change color for start (Green)
			} else {
			startTimer();
			startStopButton.setText("Stop");
			startStopButton.setTextColor(Color.parseColor("#FF5555")); // Change color for stop (Red)
		}
	}
});
	  
resetButton = findViewById(R.id.resetButton);
		resetButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Actions to perform when the button is clicked
        resetTimer();
    }
      });
		
	}
	
	private void checkOverlayPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
			askForOverlayPermission();
			} else {
			createChatHead();
		}
	}
	
	private void askForOverlayPermission() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Overlay Permission Required");
		builder.setMessage("Please grant overlay permission to display the Floating Timer Widget.");
		builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
				intent.setData(Uri.parse("package:" + getPackageName()));
				startActivityForResult(intent, PERMISSION_REQUEST_CODE);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			}
		});
		builder.setCancelable(false);
		builder.show();
	}
	
	private void createChatHead() {
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		
		chatHead = new LinearLayout(this);
		chatHead.setOrientation(LinearLayout.VERTICAL);
		
		textView = new TextView(this);
		if(enableMillisec){
		textView.setText("00:00:00");
		}else{
		textView.setText("00:00");
		}
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(timerSize);
		textView.setTextColor(Color.WHITE);
		textView.setPadding(1,1,1,1);
		textView.setTypeface(null, Typeface.BOLD);
		textView.setShadowLayer(5, 0, 0, Color.BLACK);
		
		chatHead.addView(textView);
		
		int overlayType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
		WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
		WindowManager.LayoutParams.TYPE_PHONE;
		
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
		WindowManager.LayoutParams.WRAP_CONTENT,
		WindowManager.LayoutParams.WRAP_CONTENT,
		overlayType,
		WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
		PixelFormat.TRANSLUCENT);
		
		
		params.gravity = Gravity.CENTER;
		params.x = 0;
		params.y = 0;
		
		chatHead.setOnTouchListener(new View.OnTouchListener() {
			private float initialTouchX, initialTouchY;
			private int initialX, initialY;
			private long lastClickTime = 0;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					initialX = params.x;
					initialY = params.y;
					initialTouchX = event.getRawX();
					initialTouchY = event.getRawY();
					return true;
					case MotionEvent.ACTION_MOVE:
					int deltaX = (int) (event.getRawX() - initialTouchX);
					int deltaY = (int) (event.getRawY() - initialTouchY);
					params.x = initialX + deltaX;
					params.y = initialY + deltaY;
					windowManager.updateViewLayout(chatHead, params);
					return true;
					case MotionEvent.ACTION_UP:
                        float distanceX = Math.abs(event.getRawX() - initialTouchX);
                        float distanceY = Math.abs(event.getRawY() - initialTouchY);
                        // Check if it's a click (allowing for slight movement)
                        if (distanceX < 5 && distanceY < 5) {
                            handleButtonClick(); // Handle button click action
                        }
					
					
					return true;
				}
				return false;
			}
			
			
			private void handleButtonClick(){
				long clickTime = System.currentTimeMillis();
					if (clickTime - lastClickTime < 300) {
						resetTimer();
						} else {
						if (isRunning) {
							stopTimer();
							} else {
							startTimer();
						}
					}
					lastClickTime = clickTime;
				
			}
		});
		
		windowManager.addView(chatHead, params);
	}
	
	
	
	private void startTimer() {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		if (vibrator != null) {
			vibrator.vibrate(50);
		}
		if (isRunning) {
			return;
		}
		isStop=false;
		isRunning = true;
		startTime = System.currentTimeMillis();
		
		handler.postDelayed(timerRunnable, 0);
		textView.setTextColor(Color.RED);
		textView.setShadowLayer(5, 0, 0, Color.BLACK);
	}
	
	private void stopTimer() {
		
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		if (vibrator != null) {
			vibrator.vibrate(50);
		}
		
		if (!isRunning) {
			return;
		}
		isStop=true;
		isRunning = false;
		elapsedTime += System.currentTimeMillis() - startTime;
		
		handler.removeCallbacks(timerRunnable);
		textView.setTextColor(Color.BLACK);
		textView.setShadowLayer(5, 0, 0, Color.WHITE);
	}
	
	private void resetTimer() {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		if (vibrator != null) {
			vibrator.vibrate(50);
		}
		
		
		if (timerRunnable != null && isRunning) {
         handler.removeCallbacks(timerRunnable);
         }
isStop=false;
		isRunning = false;
		elapsedTime = 0;
		
		if (enableMillisec){
		textView.setText("00:00:00");
		}else{textView.setText("00:00");}
		textView.setTextColor(Color.WHITE);
		textView.setShadowLayer(5, 0, 0, Color.BLACK);
	}
	
	private Runnable timerRunnable = new Runnable() {
		@Override
		public void run() {
			long currentTime = System.currentTimeMillis();
			long elapsedTimeMillis = elapsedTime + (currentTime - startTime);
			
			int totalSeconds = (int) (elapsedTimeMillis / 1000);
			int hours = totalSeconds / 3600;
			int minutes = (totalSeconds % 3600) / 60;
			int seconds = totalSeconds % 60;
			int milliseconds = (int) (elapsedTimeMillis % 1000);
			
			String timeString;
			if (hours > 0) {
				if(enableMillisec){
				timeString = String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, (int)(milliseconds/10));
				}else{
					timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
				}
				} else {
					if(enableMillisec){
						timeString = String.format("%02d:%02d:%02d", minutes, seconds, (int)(milliseconds)/10);
						}else{
							timeString = String.format("%02d:%02d", minutes, seconds);
					}
				
			}
			
			textView.setText(timeString);
			if(enableMillisec){
			handler.postDelayed(this, 15); // Update every 10 milliseconds
			}else{
				handler.postDelayed(this, 1000); //To consume less resources
			}
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (chatHead != null) {
			windowManager.removeView(chatHead);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PERMISSION_REQUEST_CODE) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
				createChatHead();
				} else {
				finish();
			}
		}
	}
}