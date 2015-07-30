package net.rolfhendriks.common;

import android.animation.TimeInterpolator;

/**
 * Created by rolf on 7/9/15.
 *
 * Custom overshoot animation curve featuring configurable number of overshoots and
 * realistic spring physics. This can be used to replace OvershootInterpolator, or to implement a
 * spring animation that overshoots the destination multiple times.
 */
public class RHSpringInterpolator implements TimeInterpolator{

    /// how many times to shoot past the end point while springing.
    public int mOvershootCount = 1;

    /** tweak this to adjust how quickly the spring oscillation dampens. Must be >0 and <1. Low
        values result in more damping */
    public float mSpringDamping = 0.2f;


    public RHSpringInterpolator(){}
    public RHSpringInterpolator(int overshootCount, float springDamping){
        super();
        mOvershootCount = overshootCount;
        mSpringDamping = springDamping;
    }


    @Override
    public float getInterpolation (float x)
    {
        //
        // want an animation that begins at (0,0), ends at (1,1), and oscillates
        //  back and forth around y = 1. But how?
        //
        //  IDEA: create a function from (0,-1) to (0,0) oscillating around y=0,
        //  by using a sinusoidal wave multiplied by exponential decay. Then add 1.
        //

        // use a -cos curve to produce oscillations.
        //  0 overshoots = 1/4 curve
        //  1 overshoot = 3/4 curve
        //  n overshoots = n/2 + 1/4 = (2n+1)/4
        float sineWaveCount = .25f + .5f * mOvershootCount;
        float sineWaveValue = (float) (-Math.cos ( 2.f * Math.PI * x *
                sineWaveCount));

        float exponentialDecayValue = (float) Math.pow (mSpringDamping, x);

        float rawResult = 1 + exponentialDecayValue * sineWaveValue;

        return rawResult;
    }

}
