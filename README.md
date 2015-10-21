# meteoclima-android

(English - Αγγλικά)

This is an open source weather app for android desiged to use gridded and in situ data provided from the Atmosphere and Climate Dynamics Group of the Harokopio University of Athens (ACDG/HUA).

All the files except the php files inside the folder named "server-side" are part of the gradle project (Java) created using the Android Studio.

The files inside "server-side" are the php code used by the app to retrieve the forecasts of the closest gridded point to the current location from the server's MySQL database.

It is a simple web service that takes as parameters the longtitude and the latitude of a point (users current location) and returns, in JSON format, the closest gridded point's forecasts for the current day and the next 4 days. It uses the Haversine formula to calculate the closest location.

Due to disruptions of the ACDG/HUA weather forecast data service the app adapted to use data from http://openweathermap.org.

The source code of the original full working version of the app is available at https://github.com/ellak-monades-aristeias/meteoclima-android/tree/73eb2b70324187f065809d509d92a1e4beaffc08 (Depository at point of history Oct 5 2015).

You can read more details on the <a href="https://github.com/ellak-monades-aristeias/meteoclima-android/wiki">Wiki</a> (in Greek).

Readme file for users (in Greek): https://github.com/ellak-monades-aristeias/meteoclima-android/blob/master/README_USERS.md

Readme file for developers (in Greek): https://github.com/ellak-monades-aristeias/meteoclima-android/blob/master/README_DEVS.md

(Greek - Ελληνικά)

Το meteoclima-android μία open source εφαρμογή πρόγνωση καιρού για το Android που σχεδιάστικε για να προβάλλει τα δεδομένα που παρέχονται από την Ομάδα Ατμοσφαιρικής και Κλιματικής Δυναμικής του Χαροκοπείου Πανεπιστημίου Αθηνών (ACDG / HUA).

Όλα τα αρχεία εκτός από τα αρχεία php μέσα στο φάκελο με το όνομα «server-side» είναι μέρος του gradle project (Java) που δημιουργήθηκε με το Android Studio.

Τα αρχεία μέσα στον φάκελο server-side είναι ο PHP κώδικας που χρησιμοποιείται από την εφαρμογή για να ανακτήσει τις προβλέψεις για το πλησιέστερο πλεγματικό σημείο από τη βάση δεδομένων MySQL του διακομιστή.

Είναι ένα απλό web service που παίρνει ως παραμέτρους το γεωγραφικό μήκος και το γεωγραφικό πλάτος ενός σημείου (την τρέχουσα θέση του χρήστη) και επιστρέφει, σε μορφή JSON, τις προβλέψεις για το πλησιέστερο πλεγματικό σημείο για την τρέχουσα ημέρα και τις 4 επόμενες ημέρες. Χρησιμοποιεί τον τύπο Haversine για να υπολογίσει το πιο κοντινό πλεγματικό σημείο και να επιστρέψει τις σωστές προβλέψεις.

Λόγω διακοπών στην παροχή δεδομένων από την ομάδα ACDG / HUA η εφαρμογή προσαρμόστηκε ώστε να χρησιμοποιεί δεδομένα από το http://openweathermap.org.

Η κώδικας της αρχικής εφαρμογής είναι διαθέσιμος εδώ https://github.com/ellak-monades-aristeias/meteoclima-android/tree/73eb2b70324187f065809d509d92a1e4beaffc08 (το depository την ημερομηνία 5 Οκτωβρίου 2015)

Περισσότερες πληροφορίες μπορείτε να βρείτε στο <a href="https://github.com/ellak-monades-aristeias/meteoclima-android/wiki">Wiki</a> 

Αρχείο Readme για χρήστες: https://github.com/ellak-monades-aristeias/meteoclima-android/blob/master/README_USERS.md

Αρχείο Readme για developers: https://github.com/ellak-monades-aristeias/meteoclima-android/blob/master/README_DEVS.md

**Πίνακας Παραδοτέων**

<table>
<tr>
<th>No</th><th>Τίτλος Παραδοτέου</th><th>URLs</th>
</tr>
<tr>
<td>1</td><td>Οι κατάλληλα διαμορφωμένες βάσεις δεδομένων των ατμοσφαιρικών συνθηκών που επικρατούν στην Ευρώπη και την Ελλάδα.</td><td>https://github.com/ellak-monades-aristeias/meteoclima-android/wiki , https://github.com/ellak-monades-aristeias/meteoclima-android/tree/master/server-side</td>
</tr>
<tr>
<td>2</td><td>O πηγαίος κώδικας της εφαρμογής και το τελικό εκτελέσιμο πακέτο αρχείων (apk)</td><td>https://github.com/ellak-monades-aristeias/meteoclima-android/wiki/Η-Android-Εφαρμογή-Πρόγνωσης-Καιρού , https://github.com/ellak-monades-aristeias/meteoclima-android</td>
</tr>
</table>

Κώδικας με άδεια <a href="https://github.com/ellak-monades-aristeias/meteoclima-android/blob/master/LICENSE_GR.pdf">EUPL v1.1</a> και περιεχομένο με <a href="https://creativecommons.org/licenses/by-sa/4.0/">CC-BY-SA 4.0</a>.
