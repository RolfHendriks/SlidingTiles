# SlidingTiles

<img src="/demo/slidingTiles-demo-short.gif" alt="Sliding Tiles App Demo"/>

## Overview

Demonstrates a tile based layout. Rearrange tiles by touching and holding on a tile, then 
dragging it. 

This project was intended for educational purposes - to learn layout, animation, and 
interaction fundamentals in depth. It could also be used as a starting point for a tile 
based menu system or image gallery.

## Features

* Flexible tile layout using zero lines of code
* Custom reusable hit detection, including configurable touch slop, long press duration, and touch tracking
* Custom spring animation curves using harmonic oscillation physics
* View frame animator to smoothly animate both the position and size of an object

## Implementation Details

### Layout

The tile layout was made using nested weighted LinearLayouts. A horizontal LinearLayout 
divides the tiles into a left and right column. Each column is a vertical LinearLayout. 
This divides the screen into five fixed spaces:

<img width="387" height="642" src="/demo/slidingTiles-spaces.png" alt="tile holders"/>

Each space is fixed but contains a movable and changeable tile. This architecture solves multiple problems at once:
* Drawing lines. The black dividing lines between tiles are just padding between the tiles and the spaces holding them.
* Seamless hit detection. When touching in between two tiles, the nearest tile responds. 
This is because the spaces holding the moving tiles, not the moving tiles themselves, detect touches and gestures.
* Improved touch event handling. Android gesture detection is inaccurate when moving a view while it is receiving touch input.

Using nested LinearLayouts was a questionable decision. It was an ideal solution to making 
a flexible tile layout with zero lines of code, but caused significant complications when rearranging tiles. If doing 
the project over again, I would consider flattening the hierarchy into a single 
RelativeLayout instead. However I would lose the benefit of having a fully flexible zero 
configuration layout, so I have mixed feelings about this change.

### Theming + Styling

<img width="387" height="642" src="/demo/slidingTiles-styling.png" alt="styled system widget" />za
<img src="/demo/slidingTiles-colorChange.gif" alt="color changing"/>

This project uses Android resource management to define all colors, margins, fonts, and styles for the app. I also used 
resource management to color coordinate all system widgets with the app's color palette. See the 'More Info' feature for an 
interesting example of this. Coming from the iOS development arena, I was blown away at Android's resource management capabilities. 
So far, I think that resource management is the single biggest advantage that Android developers have over iOS developers.

The color slider at the bottom of the screen uses a hue shifting algorithm to change colors of the tiles/artwork without losing the artistic integrity of 
the overall design. When moving the slider, all tiles keep the same color saturation and brightness, but just shift their hue. I have found that 
thinking of colors in hue, saturation, and brightness instead of in red, green, and blue is a useful mindset to have when designing an interface.


### Interaction

<img src="/demo/slidingTiles-tracking.gif" alt="Touch tracking"/>

This project uses a custom reusable gesture detector (*RHGestureDetector*) instead of Android's default gesture detector.
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
location and total distance dragged, which are not readily available using Android gesture detectors.

### Animation

This project features two reusable animation utilities: *ViewFrameAnimator*, to animate both the 
position and size of a view at once, and *SpringInterpolator* to implement springy animation curves.
Combining both results in the the animation you see when dropping a tile into a new position.

*ViewFrameAnimator* replaces my original attempts at animating tiles by using standard translation + 
scaling animations. It has two advantages over using standard scale + translation animations.

1. No issues when combining position changes, size changes, and custom animation curves. 
Using default scale + translation animations with custom animation curves leads to bugs and complications, 
since scaling a view changes the behavior of translating it.

2. No content distortions. Imagine the tiles in this demo being a menu system or photo gallery. 
If we used scaling instead of ViewFrameAnimator to move tiles into place , it would skew the text or photo contents 
inside of a tile when the tile changes columns. This would not make for a shippable app experience.

*SpringInterpolator* substitutes Android's OvershootInterpolator, but is more configurable. 
For example, SpringInterpolator can overshoot its destination any number of times.


### Rearranging Tiles

<img src="/demo/slidingTiles-move.gif" alt="Moving tiles"/>

Rearranging tiles on dragging was the most challenging part of the project, mainly due to 
using nested LinearLayouts. A flattened View hierarchy using RelativeLayout or AbsoluteLayout 
would have simplified this part significantly.

#### Challenge #1: z order

##### Problem

When touching and holding a tile, it pops to the front, obscuring all other tiles. This sounds easy to do, but was deceptively difficult.

*tileGallery.bringSubviewToFront (tile)* seems like all that is needed, but does not work due to several complications.

The main complication is due to using LinearLayouts. Due to the way LinearLayout works internally, a tile would snap a 
to the top of the screen when using bringSubviewToFront, instead of just drawing over the top of overlapping tiles.
This is because LinearLayouts use the z ordering of their children to determine positioning.

Another complication is that to bring a tile in front of all other tiles, we need to not just change the z order of that tile, but 
of all containers between the tile and the tile gallery. A flat view hierarchy would have avoided this complication.

Finally, views clip by default in Android (unlike iOS, which requires you to explicitly activate clipping). And they clip to both their bounds and to
their padding. So I had to discover both *setClipChildren* and *setClipToPadding* to get tiles moving around freely. It was a simple fix to 
apply, but a difficult one to discover.

##### Solution

Instead of changing child view orderings, I used *z translation* to bring tiles to the front. 
This has the added benefit of adding a drop shadow to the dragged tile. But has the drawback 
of abandoning backwards compatibility, since z translation was recently introduced in 
Android Lollipop/5.1.



#### Challenge #2: changing spaces

Changing which tile is in which space was a challenge as well. The approach I wanted to take 
was:
* on long press, take a tile out of its space and make it a direct child of the tile gallery.
* on dragging, change parents for tiles as they shift positions.
* on dropping, make the active tile a child of the active space currently dragged over.

This would have been intuitive. The problem however is that it was not feasible to do this because changing the hierarchy 
of a view while it is receiving touch input causes bugs and complications, both on iOS and on Adroid. A flat view 
hierarchy, or making the tile collection as a whole responsible for all touch events, would probably have resolved this 
issue. But would have been risky and required significant structural changes.

Instead, I took a different approach. I kept parent/child relations constant when rearranging 
tiles. But I added an explicit reference between a space and the tile it contains.
I then encapsulated methods for going between tiles and their spaces. This kept the logic 
for rearranging tiles simple (and changeable in a future iteration!) by abstracting away the 
detail that the view hierarchy is arranged in an unintuitive way.


## Conclusion

I made this project to explore the fundamentals of layout, animation, and interaction on Android in depth. I spent many hours 
and dove deep into the vast pool of Android SDKs and documentation to make the interface look and feel exactly the way I wanted it to, 
without compromise. If you are getting your feet wet in Android development, I hope this project will help you master layout, 
animation, and interaction without diving into the deep end of the pool.
