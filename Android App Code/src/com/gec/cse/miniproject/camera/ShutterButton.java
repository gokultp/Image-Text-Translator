

package com.gec.cse.miniproject.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.widget.ImageView;

public class ShutterButton extends ImageView {
	/**
	 * A callback to be invoked when a ShutterButton's pressed state changes.
	 */
	public interface OnShutterButtonListener {
		/**
		 * Called when a ShutterButton has been pressed.
		 
		 */
		void onShutterButtonFocus(ShutterButton b, boolean pressed);

		void onShutterButtonClick(ShutterButton b);
	}

	private OnShutterButtonListener mListener;
	private boolean mOldPressed;

	public ShutterButton(Context context) {
		super (context);
	}

	public ShutterButton(Context context, AttributeSet attrs) {
		super (context, attrs);
	}

	public ShutterButton(Context context, AttributeSet attrs,
			int defStyle) {
		super (context, attrs, defStyle);
	}

	public void setOnShutterButtonListener(OnShutterButtonListener listener) {
		mListener = listener;
	}

	/**
	 * Hook into the drawable state changing to get changes to isPressed -- the
	 * onPressed listener doesn't always get called when the pressed state
	 * changes.
	 */
	 @Override
	 protected void drawableStateChanged() {
		 super .drawableStateChanged();
		 final boolean pressed = isPressed();
		 if (pressed != mOldPressed) {
			 if (!pressed) {
				 // When pressing the physical camera button the sequence of
				 // events is:
				 //    focus pressed, optional camera pressed, focus released.
				 // We want to emulate this sequence of events with the shutter
				 // button. When clicking using a trackball button, the view
				 // system changes the the drawable state before posting click
				 // notification, so the sequence of events is:
				 //    pressed(true), optional click, pressed(false)
				 // When clicking using touch events, the view system changes the
				 // drawable state after posting click notification, so the
				 // sequence of events is:
				 //    pressed(true), pressed(false), optional click
				 // Since we're emulating the physical camera button, we want to
				 // have the same order of events. So we want the optional click
				 // callback to be delivered before the pressed(false) callback.
				 //
				 // To do this, we delay the posting of the pressed(false) event
				 // slightly by pushing it on the event queue. This moves it
				 // after the optional click notification, so our client always
				 // sees events in this sequence:
				 //     pressed(true), optional click, pressed(false)
				 post(new Runnable() {
					 public void run() {
						 callShutterButtonFocus(pressed);
					 }
				 });
			 } else {
				 callShutterButtonFocus(pressed);
			 }
			 mOldPressed = pressed;
		 }
	 }

	 private void callShutterButtonFocus(boolean pressed) {
		 if (mListener != null) {
			 mListener.onShutterButtonFocus(this , pressed);
		 }
	 }

	 @Override
	 public boolean performClick() {
		 boolean result = super.performClick();
		 playSoundEffect(SoundEffectConstants.CLICK);
		 if (mListener != null) {
			 mListener.onShutterButtonClick(this);
		 }
		 return result;
	 }
}