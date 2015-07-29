package net.rolfhendriks.common;

import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.os.Handler;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by rolf on 6/27/15.
 */
public class RHGestureDetector{

    public interface RHGestureListener{

        /**
         Triggered immediately on touch down. A typical response might be to highlight a UI element.
         */
        void onTouchDown (MotionEvent e);

        /**
         * Triggered when dragging, but only if amount dragged exceeds our touch slop
         */
        void onTouchMove (MotionEvent e);

        /**
         * Triggered whenever the finger is lifted. This does not necessarily imply a tap event.
         */
        void onTouchUp (MotionEvent e);

        /**
         * triggered when a touch gets canceled by the system. For example, if a popup interrupts
         * the current flow.
         */
        void onTouchCancel (MotionEvent e);

        /**
         * triggered when touching and holding on a view without moving.
         * Caution: may need to reconfigure some parameters for the gesture detector to work as
         * expected after long press. For example: if dragging after a long press, disable
         * tracking and touch slop.
         */
        void onLongPress (MotionEvent e);

        /**
         * if using tracking, this is triggered when the finger moves outside of the view's
         * tracking area.
         */
        void onLeaveTrackingArea (MotionEvent e);

        /**
         * if using tracking, this is triggered when the finger moves from outside of the view's
         * tracking area back inside of it.
         */
        void onEnterTrackingArea (MotionEvent e);

    }

    /**
     * how many pixels from the original touch location a touch needs to move before triggering
     * drag events. Setting this too low can make it difficult to long press or tap a view.
     */
    public float touchSlop = 20;

    /**
     * if true, use long press gestures
     */
    public boolean enableLongPress = false;

    /**
     * how many milliseconds to wait until a long press gesture triggers, if using long press
     * gestures.
     */
    public long longPressDuration = 350;

    /**
     * The view that is processing the touches for this gesture. This is not used by the gesture
     * detector, but can be used by an app. Gesture detectors are almost always associated with a
     * view, so this parameter makes it clear which view to act on when receiving motion events.
     */
    public View view;

    protected RHGestureListener listener;
    protected PointF touchDownPosition, previousTouchPosition, currentTouchPosition;  // long
    // term, a full array of
    // positions and times may be useful, so these should be considered subject to change and
    // encapsulated.
    protected Handler eventHandler = new Handler();
    protected boolean useTouchSlop = true;
    protected Runnable longPressRunnable;
    protected Rect trackingArea;
    protected boolean isLongPressed;

    public RHGestureDetector (RHGestureListener listener, View v){
        this.listener = listener;
        this.view = v;
    }

    public PointF getOriginalTouchLocation() {
        return touchDownPosition;
    }

    public PointF getTotalDragOffset() {
        return new PointF ( currentTouchPosition.x - touchDownPosition.x, currentTouchPosition.y
                - touchDownPosition.y );
    }

    public PointF getLastDragOffset()
    {
        if (previousTouchPosition == null)
            return new PointF (0,0);
        return new PointF ( currentTouchPosition.x - previousTouchPosition.x,
                currentTouchPosition.y - previousTouchPosition.y );
    }

    /**
     * Defines a tracking area, which disables touch events if a gesture moves well outside of
     * its view. The tracking area encompasses the view and a configurable amount of
     * outside padding.
     *
     * When touch events are outside the tracking area, the gesture detector does not send
     * any events. This can be used to, for example, allow users to cancel a touch if
     * dragging away from a button.
     *
     * The gesture detector sends additional events when touches transition between the inside
     * and outside of the tracking area. See onTouchEnter and onTouchLeave. If using tracking, it
     * is typical to hook into these events to highlight and unhighlight a UI element to provide
     * visual feedback of the touch tracking mechanism.
     *
     * @param tracking how far outside of the view's bounds a motion event needs to go before
     *                 being considered outside
     */
    public void setTrackingArea (int tracking)
    {
        if (tracking == 0)
            trackingArea = null;
        else {
            assert this.view != null : "a gesture detector must have a view to enable tracking";
            trackingArea = new Rect(tracking, tracking, tracking, tracking);
        }
    }

    /**
     * input point for the gesture detector. The intended use is for a touchListener to forward
     * events to this gesture detector as it receives them.
     * @param e the touch received by a touch listener
     */
    public void onTouchEvent (MotionEvent e)
    {
        previousTouchPosition = currentTouchPosition;
        currentTouchPosition = new PointF (e.getX(), e.getY());

        switch (e.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                touchDownPosition = currentTouchPosition;
                previousTouchPosition = null;
                this.useTouchSlop = true;
                isLongPressed = false;
                listener.onTouchDown(e);
                this.delayedLongPress(e);
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d ( "touch", "Move from " + previousTouchPosition + " to " +
                        currentTouchPosition );
                // IMPLEMENT TOUCH SLOP
                //  Android motion + gesture events are extremely sensitive, and do not
                //  have touch slop built in (unlike iOS touch events). This is especially
                //  problematic for long presses, but can also lead to missed tap events.
                boolean shouldMove = true;
                if (useTouchSlop && !isLongPressed)
                {
                    PointF totalDrag = getTotalDragOffset();
                    double distance = Math.hypot (totalDrag.x, totalDrag.y);
                    if (distance >= this.touchSlop)
                    {
                        useTouchSlop = false;
                    }
                    else
                        shouldMove = false;
                }
                if (shouldMove) {
                    cancelLongPress();

                    // TRACKING
                    boolean isInside = true;
                    if ( this.isTracking() && !isLongPressed )
                    {
                        isInside = isPointInsideTrackingArea(this.currentTouchPosition);
                        boolean wasInside = isPointInsideTrackingArea(this.previousTouchPosition);

                        if (!isInside && wasInside)
                            listener.onLeaveTrackingArea(e);
                        else if (isInside && !wasInside)
                            listener.onEnterTrackingArea(e);
                    }
                    if (isInside) {
                        listener.onTouchMove(e);

                        // long press: let's include lingering after moving
                        if (enableLongPress && isPointInsideView(currentTouchPosition))
                            this.delayedLongPress(e);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                cancelLongPress();
                if (! (isTracking() && !isPointInsideTrackingArea (currentTouchPosition)))
                    listener.onTouchUp (e);
                break;

            case MotionEvent.ACTION_CANCEL:
                cancelLongPress();
                if (! (isTracking() && !isPointInsideTrackingArea(currentTouchPosition)))
                    listener.onTouchCancel(e);
                break;
        }
    }

    public class RHSimpleGestureListener implements RHGestureListener{

        public void onTouchDown (MotionEvent e){}
        public void onTouchMove (MotionEvent e){}
        public void onTouchUp (MotionEvent e){}
        public void onTouchCancel (MotionEvent e){}

        public void onLongPress (MotionEvent e){}

        public void onLeaveTrackingArea (MotionEvent e){}
        public void onEnterTrackingArea (MotionEvent e){}
    }

    protected void cancelLongPress()
    {
        if (enableLongPress && this.longPressRunnable != null)
        {
            eventHandler.removeCallbacks(this.longPressRunnable);
            this.longPressRunnable = null;
        }
    }

    protected void delayedLongPress(MotionEvent event)
    {
        if (enableLongPress && !isLongPressed) {
            final MotionEvent e = event;
            this.longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    isLongPressed = true;
                    listener.onLongPress(e);
                }
            };
            eventHandler.postDelayed(this.longPressRunnable, this.longPressDuration);
        }
    }

    protected boolean isTracking(){
        return !isLongPressed && trackingArea != null;
    }

    protected boolean isPointInsideTrackingArea(PointF p){
        return p.x > -trackingArea.left
                && p.x < this.view.getWidth() + trackingArea.right
                && p.y > -trackingArea.top
                && p.y < this.view.getHeight() + trackingArea.bottom;
    }

    protected boolean isPointInsideView (PointF p)
    {
        if (this.view == null)
            return true;
        View v = this.view;
        return p.x >= 0 && p.x <= v.getWidth() && p.y >= 0 && p.y <= v.getHeight();
    }
}
