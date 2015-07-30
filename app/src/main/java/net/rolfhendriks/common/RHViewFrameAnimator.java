package net.rolfhendriks.common;

import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by rolf on 7/5/15.
 *
 * Animates a view's position and size at the same time.
 *
 * ViewFrameAnimator does not use translation and scaling. Instead, it changes a view's
 * top/bottom/left/right. This implies that contents will not scale, and may
 * change layout mid animation. This is typically the desired effect when changing a view's
 * position and size, but may not be appropriate for all case\\s.
 */
public class RHViewFrameAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener{
    public View mView;
    public Rect mFromRect, mToRect;

    RHViewFrameAnimator (View v)
    {
        this (v, null);
    }

    public RHViewFrameAnimator(View v, Rect to)
    {
        this (v, null, to);
        mFromRect = new Rect (v.getLeft(), v.getTop(), v.getRight(), v.getBottom() );
    }


    public RHViewFrameAnimator(View v, Rect from, Rect to)
    {
        mView = v;
        mFromRect = from;
        mToRect = to;
    }

    @Override public void start(){
        setObjectValues ( new Rect (mFromRect),  new Rect (mToRect) );
        setEvaluator(new RectEvaluator());
        addUpdateListener(this);
        super.start();
    }

    @Override public void end(){
        super.end();
        removeUpdateListener(this);
    }

    public void onAnimationUpdate (ValueAnimator animator)
    {
        if (animator instanceof RHViewFrameAnimator)
        {
            RHViewFrameAnimator positionAnimator = (RHViewFrameAnimator)animator;
            Rect frame = (Rect)positionAnimator.getAnimatedValue();
            positionAnimator.mView.setLeft(frame.left);
            positionAnimator.mView.setRight(frame.right);
            positionAnimator.mView.setTop(frame.top);
            positionAnimator.mView.setBottom(frame.bottom);
        }
    }
}
