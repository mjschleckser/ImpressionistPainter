# ImpressionistPainter
An app for CMSC434 that allows for impressionist painting using a source image.

To use this app, you must have images in your Android image gallery. If you do not have any, you can download some using the Download Images button.

Once you have picked out an image to create an impressionist painting of, load it into the frame by using the Load Image button, and selecting it from your images gallery.

Then, you can begin painting. The default brush type is a circle brush, but there is also a paint splatter brush, and a speed brush that changes brush size based on how fast you move your finger. You can switch between brushes at any time by tapping the "brush" button.

To clear the painting, just press the "clear" button, and then select "OK". 

To save your painting, just press the "save" button and it will automatically save to your image gallery.

While painting, you may also notice a green circle appear on the source image. This is a brush locator. The app works by sampling color from your base image and spattering it on the canvas. The green circle hovers over the part of the image you are currently using the color from, so you can tell what color you'll be painting in next.






Sources:

Jon Froelich's skeleton code: http://cmsc434-f16.wikispaces.com/

Android Developer API - tracking movement: https://developer.android.com/training/gestures/movement.html 

StackOverflow - post on downloading images: http://stackoverflow.com/questions/15549421/how-to-download-and-save-an-image-in-android

StackOverflow - post on saving images: http://stackoverflow.com/questions/8560501/android-save-image-into-gallery 
