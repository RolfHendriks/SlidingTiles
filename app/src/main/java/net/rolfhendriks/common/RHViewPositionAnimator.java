package net.rolfhendriks.common;

import android.animation.PointFEvaluator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.view.View;

/**
 * Created by rolf on 7/5/15.
 */
public class RHViewPositionAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener{
    public View mView;
    public PointF mFromPoint, mToPoint;

    RHViewPositionAnimator(View v, PointF to)
    {
        this (v, new PointF (v.getLeft(), v.getTop()), to);
    }


    RHViewPositionAnimator(View v, PointF from, PointF to)
    {
        mView = v;
        mFromPoint = from;
        mToPoint = to;
        setObjectValues (from, to);
        setEvaluator(new PointFEvaluator());
    }

    @Override public void start(){
        addUpdateListener(this);
        super.start();
    }

    @Override public void end(){
        super.end();
        removeUpdateListener(this);
    }

    public void onAnimationUpdate (ValueAnimator animator)
    {
        if (animator instanceof RHViewPositionAnimator)
        {
            RHViewPositionAnimator positionAnimator = (RHViewPositionAnimator)animator;
            PointF value = (PointF)positionAnimator.getAnimatedValue();
            PointF delta = new PointF ( value.x - mView.getLeft(), value.y - mView.getTop() );
            positionAnimator.mView.offsetLeftAndRight((int) delta.x);
            positionAnimator.mView.offsetTopAndBottom((int) delta.y);
        }
    }
}
