# SlidingTiles

<video width="416" height="842" controls>
  <source src="/demo/Android-slidingTiles.mp4" type="video/mp4"/>
  <img width="387" height="642" src="/demo/slidingTiles.png" alt="Sliding Tiles app"/>
</video>

## Overview

Demonstrates a tile based layout. Rearrange tiles by touching and holding on a tile, then 
dragging it.

This project is intended primarily for educational purposes. It could be used as a starting 
point for a menu system or photo gallery, but the main intent for developing SlidingTiles 
was to demonstrate how to do layout, animation, and interaction on Android.

## Features

* Custom reusable gesture detection with faster long press and configurable touch slop
* Custom reusable spring animations
* Custom reusable view frame animator to smoothly animate both the position and size of an object
* Reusable view layout and debugging utilities
* Flexible tile layout using zero lines of code
* Seamless hit detection

## Implementation Details

### Layout

The tile layout was made using nested weighted LinearLayouts. A horizontal LinearLayout 
divides the tiles into a left and right column. Each column is a vertical LinearLayout. 
This divides the screen into five spaces, which I will call tile holders:

<img width="387" height="642" src="/demo/tileholders.png" alt="tile holders"/>

Each tile holder has a fixed position and padding, but contains a movable tile. This architecture 
solves multiple problems at once:
* It implements the black dividing lines between tiles. The tile view as a whole has a black 
background, so the padding inside each tile holder creates our dividing lines.
* It makes hit detection seamless. When touching in between two tiles, the nearest tile responds. 
This is because the fixed tile holders, not moving tiles, detect touches and gestures.
* It simplifies gestures for moving tiles. Android gesture detectors break down if responding 
to touch events on an object while applying translations to it.

Using nested LinearLayouts was a questionable decision. It was an ideal solution to making 
a static tile layout, but caused significant complications when rearranging tiles. If doing 
the project over again from scratch, I would consider flattening the hierarchy into a single 
RelativeLayout instead. However I would lose the benefit of having a fully flexible zero 
configuration layout, so I have mixed feelings about this change.

### Interaction

This project uses a custom gesture detector (*RHGestureDetector*) in place of Android's default gesture detector.
There are many reasons I thought this was appropriate
* *Faster long press*. Android's long press duration is very long and not configurable. I wanted a much faster 
long press gesture for this demo.
* *Touch slop*. Android has no automatic support for touch slop. This makes long press and tap gestures 
difficult, because moving your finger by just a pixel will invalidate a tap or long press. The 
custom gesture detector has configurable touch slop, defaulting to 10px.
* *Touch tracking*. in iOS, controls support tracking, which lets you cancel a touch by dragging 
away from the object you are touching. RHGestureDetector brings this feature to Android. Notice 
that tiles stop responding when dragging far away from them, and resume responding when dragging near them.
* *Convenience methods*. *RHGestureDetector* has accessors for common data such as original touch 
location and total distance dragged, which are not easily available using Android defaults.

### Animation

This project features two reusable animation utilities: *ViewFrameAnimator*, to animate both the 
position and size of a view at once, and *SpringInterpolator* to implement springy animation curves.
Combining both results in the the animation you see when dropping a tile into a new position.

*ViewFrameAnimator* replaces my original attempts at animating tiles by using translation + 
scaling effects. It has two advantages compared to using scale + translation animations.

1. Combining translation + scaling animations at once, especially when using a 
custom animation curve, is difficult and bug prone. This is because scaling a 
view changes the behavior of translating it. ViewFrameAnimator, on the other hand, 
changes both position and size seamlessly without bugs or complications.

2. Scale animations distort contents. Imagine the tiles in this demo being a menu system 
or photo gallery. If we used scaling to move tiles, it would skew the text of photo contents 
inside of it when the tile changes columns.

ViewFrameAnimator also brings iOS-like animation features to Android. On iOS, animating the 
frame of a view is trivial - even easier than using ViewFrameAnimator - and is one of the 
most common tasks performed in any app.

*SpringInterpolator* substitutes Android's OvershootInterpolator, but is more configurable. 
For example, SpringInterpolator can overshoot its destination any number of times.


### Rearranging Tiles

Rearranging tiles on dragging was the most challenging part of the project, mainly due to 
using nested LinearLayouts. A flattened View hierarchy using RelativeLayout or AbsoluteLayout 
would have simplified this part significantly.

#### Challenge #1: z order

When touching and holding a tile, it pops to the front, obscuring all other tiles.

On iOS, this would be trivial: just use [tileLayout bringSubviewToFront:myTile].

On Android, this would fail because rearranding the order of children in a LinearLayout changes 
the layout. For example, bringing the bottom right tile to the front would position it at 
the top of the right column.

Instead of changing child view orderings, I used *z translation* to bring tiles to the front. 
This has the added benefit of adding a drop shadow to the dragged tile. But has the drawback 
of abandoning backwards compatibility. This approach also adds complexity. Using z translation 
on a tile to make it pop forward is not sufficient. Z translation only brings a subview to the 
front of its parent. So doing this right involved temporarily changing the z order of both the 
active tile and its parent containers.

#### Challenge #2: changing spaces

Changing which tile is in which space was a challenge as well. The approach I wanted to take 
was:
* on long press, take a tile out of its space and make it a direct child of the tile collection.
* on dragging, change parents for tiles as they shift positions.
* on dropping, make the active tile a child of the active space currently dragged over.

This would have been intuitive. The problem however is that rearranging views while 
they are interacting and animating is error prone.  Most notably, when I tried taking a 
tile out of its parent, the runtime would throw an exception saying that I needed to take 
the tile out of is parent first. Changing view hierarchies while touch events are active 
is also generally problematic, both on iOS and on Android.

Instead, I took a different approach. I kept parent/child relations constant when rearranging 
tiles. But I added an explicit reference between an area / tile holder and the tile it contains.
I then encapsulated methods for going between tiles and their containers. This kept the logic 
for rearranging tiles simple.

I also added basic reusable view hierarchy utilities to get, set, and convert view positions and frames.