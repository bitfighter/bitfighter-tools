<?php
//
//
//
include("inc/header.inc.php");

if(isset($_GET['date']))
	$date = $_GET['date'];
if(isset($_GET['index']))
	$index = $_GET['index'];

// Individual log pages
if (isset($date) && preg_match("/^\d\d\d\d-\d\d-\d\d$/", $date)) {

	$prev_date = date('Y-m-d', strtotime($date .' -1 day'));
	$next_date = date('Y-m-d', strtotime($date .' +1 day'));

	print("
<p>
	<a href=\"./index.php?index=true\">Index</a>
	<a href=\"./search.php\">Search</a>
	<a href=\"./index.php?date=$prev_date\">←Prev date</a>
	<a href=\"./index.php?date=$next_date\">Next date→</a>
</p>
<h2>IRC Log for $date</h2>
<p>Timestamps are in GMT/BST.</p>
<pre>");

	// Load file into an array
	$filearray = @file($date.".log", FILE_IGNORE_NEW_LINES);
	if($filearray === false) {
		print("<b>No logs available for $date</b>");
	}
	else {
		print("<table class=\"chat\">");
		$linenumber = 0;
			
		foreach($filearray as $line) {
			
			$linenumber++;
			// Extract the 12:34:45 from between the brackets []
			$time = substr($line, 1, 8);
			
			$results = analyzeLine($line);
			
			$aref = "l$linenumber";
			print("<tr class=\"$results[class]\"><td class=\"time\" id=\"$aref\"><a href=\"#$aref\">$time</a></td><td class=\"nick\">$results[col2]</td><td class=\"message\">$results[col3]</td></tr>\n");
		}
		
		print("</table></pre>");

		print("
<p>
	<a href=\"./index.php?index=true\">Index</a>
	<a href=\"./search.php\">Search</a>
	<a href=\"./index.php?date=$prev_date\">←Prev date</a>
	<a href=\"./index.php?date=$next_date\">Next date→</a>
</p>");
	}
}

// This is the index page
else if (isset($index) && $index == "true") {
	$dir = opendir(".");
	while (false !== ($file = readdir($dir))) {
		if (strpos($file, ".log") == 10) {
			$filearray[] = $file;
		}
	}
	closedir($dir);
	
	rsort($filearray);
	
	$path = $_SERVER['PHP_SELF'];
	
	print("<ul>");
	
	
	foreach ($filearray as $file) {
		$file = substr($file, 0, 10);
		print("<li><a href=\"$path?date=$file\">$file</a></li>");
	}

	print("</ul>");
}

// Else we default to the latest log
else {
	// Grab latest log file
	$date = date("Y-m-d");

	// Strip any parameters so we don't redirect infinitely
	$path = $_SERVER['REQUEST_URI'];

	$pos = strpos($path,'?');
	if($pos !== false) {
		$path = substr($path, 0, $pos);
	}

	// Redirect!
	header('Location: http://'.$_SERVER['SERVER_NAME'].$path."?date=$date");
}

include("inc/footer.inc.php");

?>