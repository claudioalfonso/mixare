#  mixare

mixare (mix Augmented Reality Engine) is a free open source augmented reality browser, 
which is published under the GPLv3.

This fork has been created in the wake of a Master Project of @MelanieWe and @pokerazor.
It continues to work on the library and app.

## Major additional features:
* introduced routing capabilities
* introduced 3D rendering of markers and route

## Some additional changes compared to the official repository:
* switched build system to gradle and IDE to Android Studio 1.5, bump API version to current 23
* merged master and develop branch and some other commits, pruned stale/orphaned branches
* removed all Google dependencies
* switched mapsforge to current 0.6 version
* cleaned code a bit, started fixing the lint warnings, removed deprecations, renamed artifacts and
heavily refactored internal code structure to be less monolithic, more spread over files/classes,
especially in the marker rendering of Mix/Augmentation Activity with it's structure of layered Views
* fixed some minor bugs in DataSourceList and with marker position rendering
* made some minor improvements in performance
* switched menu to side drawer
* introduced HUD to display status information (also moving Radar and RangeBar formerly known as ZoomBar into it)
* activated arrow rendering configurable per DataSource
* updated german translations

## More work to do:
* continue to clean and refactor
* rework concurrency structure to move work into background worker threads
* maybe introduce more GUI improvements
* maybe introduce caching and offline mode

Compatible to Android 3.0-6.0 (API levels 11-23)

## Some screenshots
![image 1](http://www-stud.uni-due.de/~sehawagn/roUDE/mixare-roude-route-1.png)

![image 2](http://www-stud.uni-due.de/~sehawagn/roUDE/mixare-roude-route-2.png)

![image 3](http://www-stud.uni-due.de/~sehawagn/roUDE/mixare-roude-route-3.png)
