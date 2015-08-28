# meteoclima-android

This is an open source wheather app for android based on gridded and in situ data provided from the Atmosphere and Climate Dynamics Group of the Harokopio University of Athens (ACDG/HUA).

All the files except the php files inside the folder named "server-side" are part of the gradle project created using the Android Studio.

The files inside "server-side" are the php code used to retrieve the closest forecasts' locations from the server's MySQL database.

It is a simple webserver that takes as parameters the longtitude and the latitude of a point (users current location) and returns, in JSON format, closest locations' forecasts for the current day and 2 days forward. It uses the Haversine formula to calculate the closest forecasts' locations.
