# MeasureDistance
Measures distance from an object using the camera

Native Android app written in Java.

CS 4750 Project

Tested on Google Pixel 6a, Galaxy A50, and Galaxy J3.

I had around 2.5% error




![demonstration](https://user-images.githubusercontent.com/28831749/213052375-149ec79d-8157-4062-b801-f5f6791f47b2.png)

Calculations Explained:

    - theta: Camera FOV
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
    
![visualization](https://user-images.githubusercontent.com/28831749/213051167-187f4a8c-e756-4f16-87a3-c8824c383a78.png)
