# MeasureDistance
If you know the height of an object, you can get your distance from it using only your device's camera.

Native Android app written in Java, and published to the Google Play Store. 

Tested on Pixel 6a, Galaxy A50, and Galaxy J3 with ~ 98% accuracy

[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" width="250" height="97">](https://play.google.com/store/apps/details?id=com.johnathanmitri.measuredistance)

## How to Use
1. Enter the height of the object
2. Hold your device level
3. Select the top and bottom of the object on screen 
4. The device's distance from the object is calculated automatically 
<br>

![Demo](https://github.com/johnathanmitri/MeasureDistance/assets/28831749/c978ac8c-6d69-45fb-8f19-fe9e88152876)
### Calculations explained:

    - theta: Camera FOV (Retrieved from Camera API)
    - h: represents the full height that can be seen with the camera at the given distance
    - s: physical height (size) of the object
    - d: distance from the object

    -The ratio of the Full Screen to the Object on screen can be calculated using pixels
       -example ratio:  1920 : 960  =>  Therefore, h = (1920/960) * s.

    -If the object takes up half the screen, then h is twice the size of the object.

    -tan(theta/2) can be considered constant when not zooming

    -(h/2) / d = tan(theta/2)

    -Therefore, d = (h/2)/tan(theta/2), where h = (physical height of object) * (total screen height) / (object height on screen)

    This requires the camera to be perpendicular to the object. If the camera is not perpendicular it will be inaccurate.
    
### Here's a crude drawing for reference:

![visualization](https://user-images.githubusercontent.com/28831749/213051167-187f4a8c-e756-4f16-87a3-c8824c383a78.png)
