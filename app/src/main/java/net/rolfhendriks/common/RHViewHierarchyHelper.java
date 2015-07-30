package net.rolfhendriks.common;

import android.graphics.PointF;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by rolf on 7/11/15.
 *
 * General utilities for managing views in a view hierarchy.
 */
public class RHViewHierarchyHelper {

    static public Rect getFrame (View v)
    {
        return new Rect ( v.getLeft(), v.getTop(), v.getRight(), v.getBottom() );
    }

    static public void setFrame (View v, Rect frame)
    {
        v.setTop (frame.top);
        v.setLeft (frame.left);
        v.setBottom (frame.bottom);
        v.setRight (frame.right);
    }

    static public PointF getPosition(View v)
    {
        return new PointF (v.getLeft(), v.getTop());
    }

    static public void setPosition(View v, PointF p)
    {
        int w = v.getWidth();
        int h = v.getHeight();
        v.setLeft ((int)p.x);
        v.setTop((int) p.y);
        v.setRight((int) p.x + w);
        v.setBottom ((int)p.y + h);
    }

    static public Rect getPaddedFrame (View v)
    {
        return new Rect (v.getLeft() + v.getPaddingLeft(), v.getTop() + v.getPaddingTop(), v
                .getRight() - v.getPaddingRight(), v.getBottom() - v.getPaddingBottom() );
    }

    static public String getViewHierarchy (View v)
    {
        StringBuilder s = new StringBuilder();
        getViewHierarchy(v, s, 0);
        return s.toString();
    }

    protected static void getViewHierarchy (View v, StringBuilder accumulatedString, int
            indentation)
    {
        // append string for the passed in view
        for (int i = 0; i < indentation; ++i) {
            accumulatedString.append("|\t");
        }
        accumulatedString.append(String.format("%s : %d x %d @ (%d, %d)\n", v.getClass().toString
                (), v.getWidth(), v.getHeight(), v.getLeft(), v.getTop()));

        if (v instanceof ViewGroup)
        {
            ViewGroup g = (ViewGroup)v;
            ++indentation;
            for (int i = 0; i < g.getChildCount(); ++i) {
                getViewHierarchy (g.getChildAt(i), accumulatedString, indentation);
            }
        }
    }

    public static boolean isViewDescendantOfViewGroup (View v, ViewGroup parent)
    {
        for (ViewParent p = v.getParent(); p != null; p = p.getParent())
        {
            if (p == parent)
                return true;
        }
        return false;
    }

    public static PointF convertPointFromDescendant (ViewGroup parent, View descendant, PointF p)
    {
        PointF result = p;
        for (View v = descendant; v != parent; v = (View)v.getParent())
        {
            result.x += v.getLeft();
            result.y += v.getTop();
        }

        return result;
    }

    /**
     * Returns the descendant's frame relative to the parent. Applies conversion if views are
     * between the descendant and the parent.
     *
     * @param parent the returned frame will be relative to this view
     * @param descendant any view underneath the parent view.
     * @return the descendant's frame in parent view coordinates
     */

    public static Rect convertedRectForDescendant (ViewGroup parent, View descendant)
    {
        Rect result = new Rect( 0, 0, descendant.getWidth(),
                descendant.getHeight() );   // alternately: descendant.getDrawingRect() ?
        parent.offsetDescendantRectToMyCoords(descendant, result);
        return result;
    }

    public static PointF convertedPositionForDescendant (ViewGroup parent, View descendant)
    {
        Rect rect = convertedRectForDescendant(parent, descendant);
        return new PointF (rect.left, rect.top);
    }
}
