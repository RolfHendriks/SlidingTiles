package net.rolfhendriks.modernart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by rolf on 7/18/15.
 *
 * Represents a rectangular area holding a tile. Unlike tiles, tile holders should never move.
 */
public class TileHolder extends FrameLayout {

    ///////////////////
    //
    // CONSTRUCTORS
    //
    ///////////////////

    public TileHolder(Context context) {
        super(context);
    }

    public TileHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TileHolder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TileHolder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected View currentTile;

    public View getcurrentTile() {
        return currentTile;
    }

    public void setcurrentTile(View currentTile) {
        this.currentTile = currentTile;
    }
}
