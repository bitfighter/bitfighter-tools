<?php
//
//
//

$nick_color_cache = array();

function getNickColor($nick) {
	global $nick_color_cache;
	
	// Lookup in cache first
	if(isset($nick_color_cache[$nick]))
		return $nick_color_cache[$nick];
	
	// Not found, calculate and add to cache
	$color = substr(bin2hex($nick), 0, 3);
	$nick_color_cache[$nick] = $color;
	
	return $color;
}


function startsWith($haystack, $needle) {
     $length = strlen($needle);
     return (substr($haystack, 0, $length) === $needle);
}



include("inc/header.inc.php");

$date = $_GET['date'];
$index = $_GET['index'];

// Individual log pages
if (isset($date) && preg_match("/^\d\d\d\d-\d\d-\d\d$/", $date)) {

	$prev_date = date('Y-m-d', strtotime($date .' -1 day'));
	$next_date = date('Y-m-d', strtotime($date .' +1 day'));

	print("
<p>
	<a href=\"./index.php?index=true\">Index</a>
	<a href=\"./index.php?date=$prev_date\">←Prev date</a>
	<a href=\"./index.php?date=$next_date\">Next date→</a>
</p>
<h2>IRC Log for $date</h2>
<p>Timestamps are in GMT/BST.</p>
<pre>");

	// Load file into an array
	$filearray = file($date.".log", FILE_IGNORE_NEW_LINES);

	print("<table class=\"chat\">");
	$url_regex = "/((http|https|ftp|irc):\/\/[^\s]+)/";
	$line_number = 0;
	
	foreach($filearray as $line) {
		
		$line_number++;
		// Extract the 12:34:45 from between the brackets []
		$time = substr($line, 1, 8);
		
		// Message starts at index 11
		$message = substr($line, 11);
		
		$col2 = "";
		$col3 = "";
		$class = "";
		
		// Analyze our lines
		
		// Person left the channel
		if(startsWith($message,"<-- ")) {
			$col3 = substr($message, 4);
			$class = "left";
		}
		
		// Message (have to do above check first)
		elseif(startsWith($message,"<")) {
			$index = strpos($message, "> ");
			$nick = substr($message, 1, $index-1);
			$text = htmlspecialchars(substr($message, $index+2));
			
			$col3 = preg_replace($url_regex, "<a href=\"$1\">$1</a>", $text);
			
			$color = getNickColor($nick);
			$col2 = "<span style=\"color:#$color\">$nick</span>";
			$class = "talk";
		}
		
		// Person joined the channel
		elseif(startsWith($message,"--> ")) {
			$col3 = substr($message, 4);
			$class = "join";
		}
		// Some channel action
		elseif(startsWith($message,"* ")) {
			$col3 = substr($message, 2);
			$class = "action";
		}
		// A notice of some sort
		elseif(startsWith($message,"-")) {
			$col3 = $message;
			$class = "notice";
		}
		
		// Anything else, just print for now
		else {
			$col3 = $message;
			$class = "other";
		}
		
		$aref = "l$line_number";
		print("<tr class=\"$class\"><td class=\"time\" id=\"$aref\"><a href=\"#$aref\">$time</a></td><td class=\"nick\">$col2</td><td class=\"message\">$col3</td></tr>\n");
	}
	
	print("</table>");

	print("<div align='center'><a href='#top'>top</a></div></pre>");
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