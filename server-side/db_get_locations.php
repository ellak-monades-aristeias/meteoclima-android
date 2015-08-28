<?php
 
/*
 * Following code will get the nearest locations' forecasts
 */
 
// array for JSON response
$response = array();
 
// include db connect class
require_once __DIR__ . '/db_connect.php';
 
// connecting to db
$db = new DB_CONNECT();
 
// check for post data
if (isset($_GET["lat"]) && isset($_GET["lat"])) {
    $lat = $_GET['lat'];
    $lon = $_GET['lon'];
 
    // get the nearest position
    $result = mysql_query("SELECT *, 
	 ( 6371 * acos( cos( radians($lat) ) * cos( radians( lat ) ) 
	  * cos( radians( lon ) - radians($lon) ) + sin( radians($lat) ) 
	  * sin( radians( lat ) ) ) ) AS distance 
	FROM meteoclima
	WHERE DATE(CONCAT(`yy`,'-',LPAD(`mm`,2,'00'),'-',LPAD(`dd`,2,'00'))) BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 2 DAY)
	ORDER BY distance,yy,mm,dd,hh LIMIT 24;") or die(mysql_error());
 
    // check for empty result
	if (mysql_num_rows($result) > 0) {
		// looping through all results
		// locations node
		$response["locations"] = array();
	 
		while ($row = mysql_fetch_array($result)) {
			// temp user array
			$location = array();
			$location["id"] = $row["id"];
			$location["yy"] = $row["yy"];
			$location["mm"] = $row["mm"];
			$location["dd"] = $row["dd"];
			$location["hh"] = $row["hh"];
			$location["lat"] = $row["lat"];
			$location["lon"] = $row["lon"];
			$location["mslp"] = $row["mslp"];
			$location["temp"] = $row["temp"];
			$location["rain"] = $row["rain"];
			$location["snow"] = $row["snow"];
			$location["windsp"] = $row["windsp"];
			$location["winddir"] = $row["winddir"];
			$location["relhum"] = $row["relhum"];
			$location["lcloud"] = $row["lcloud"];
			$location["mcloud"] = $row["mcloud"];
			$location["hcloud"] = $row["hcloud"];
			$location["weatherImage"] = $row["weatherImage"];
			$location["windWaveImage"] = $row["windWaveImage"];
			$location["landOrSea"] = $row["landOrSea"];
	 
			// push single location into final response array
			array_push($response["locations"], $location);
		}
		// success
		$response["success"] = 1;
	 
		// echoing JSON response
		echo json_encode($response);
	} else {
		// no locations found
		$response["success"] = 0;
		$response["message"] = "No locations found";
	 
		// echo no users JSON
		echo json_encode($response);
	}
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
 
    // echoing JSON response
    echo json_encode($response);
}
?>
