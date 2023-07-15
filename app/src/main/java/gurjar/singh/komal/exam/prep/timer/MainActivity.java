package gurjar.singh.komal.exam.prep.timer;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.graphics.PixelFormat;
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
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private boolean isRunning = false;
	private long startTime, elapsedTime;
	private Handler handler = new Handler();

	private WindowManager windowManager;
	private LinearLayout chatHead;
	private TextView textView;

	private static final int PERMISSION_REQUEST_CODE = 1234;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (!Settings.canDrawOverlays(this)) {
				askForOverlayPermission();
			} else {
				createChatHead();
			}
		} else {
			createChatHead();
		}
	}

	private void askForOverlayPermission() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Overlay Permission Required");
		builder.setMessage("Please grant overlay permission to display the stopwatch.");
		builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
				intent.setData(Uri.parse("package:" + getPackageName()));
				startActivity(intent);
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
		// Inside a method or constructor
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int screenWidth = displayMetrics.widthPixels;
		int screenHeight = displayMetrics.heightPixels;
		
		
		
		
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		chatHead = new LinearLayout(this);
		chatHead.setOrientation(LinearLayout.VERTICAL);

		textView = new TextView(this);
		textView.setText("00:00");
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(40);
		textView.setTextColor(Color.WHITE);
		textView.setTypeface(null, Typeface.BOLD);
		textView.setShadowLayer(5, 0, 0, Color.BLACK);

		chatHead.addView(textView);

		WindowManager.LayoutParams params;
		int overlayType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
				? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
				: WindowManager.LayoutParams.TYPE_PHONE;

		params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT, overlayType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.START;
		params.x = (int)(screenWidth / 2.66);
		params.y = screenHeight / 2;

		chatHead.setOnTouchListener(new View.OnTouchListener() {
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;
			private boolean isDragging = false;
			private long lastClickTime = 0;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					initialX = params.x;
					initialY = params.y;
					initialTouchX = event.getRawX();
					initialTouchY = event.getRawY();
					isDragging = false;
					return true;
				case MotionEvent.ACTION_UP:
					if (!isDragging) {
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
					return true;
				case MotionEvent.ACTION_MOVE:
					if (Math.abs(event.getRawX() - initialTouchX) > 10
							|| Math.abs(event.getRawY() - initialTouchY) > 10) {
						isDragging = true;
					}
					params.x = initialX + (int) (event.getRawX() - initialTouchX);
					params.y = initialY + (int) (event.getRawY() - initialTouchY);
					windowManager.updateViewLayout(chatHead, params);
					return true;
				}
				return false;
			}
		});

		windowManager.addView(chatHead, params);
	}

	private void startTimer() {
		Vibrator vibrator = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
		if (vibrator != null) {
			vibrator.vibrate(50);
		}
		if (isRunning) {
			return;
		}

		isRunning = true;
		startTime = System.currentTimeMillis();

		handler.postDelayed(timerRunnable, 0);
		textView.setTextColor(Color.RED);
		textView.setShadowLayer(5, 0, 0, Color.BLACK);
		
	}

	private void stopTimer() {
		Vibrator vibrator = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
		if (vibrator != null) {
			vibrator.vibrate(50);
		}

		if (!isRunning) {
			return;
		}

		isRunning = false;
		elapsedTime += System.currentTimeMillis() - startTime;

		handler.removeCallbacks(timerRunnable);
		textView.setTextColor(Color.BLACK);
		textView.setShadowLayer(5, 0, 0, Color.WHITE);
		
	}

	private void resetTimer() {
		Vibrator vibrator = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
		if (vibrator != null) {
			vibrator.vibrate(50);
		}

		isRunning = false;
		elapsedTime = 0;

		handler.removeCallbacks(timerRunnable);

		textView.setText("00:00");
		textView.setTextColor(Color.WHITE);
		textView.setShadowLayer(5, 0, 0, Color.BLACK);
		
	}

	private Runnable timerRunnable = new Runnable() {
		@Override
		public void run() {
			long currentTime = System.currentTimeMillis();
			long updatedTime = elapsedTime + (currentTime - startTime);

			int seconds = (int) (updatedTime / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;

			textView.setText(String.format("%02d:%02d", minutes, seconds));

			handler.postDelayed(this, 0);
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (chatHead != null) {
			windowManager.removeView(chatHead);
		}
	}
}