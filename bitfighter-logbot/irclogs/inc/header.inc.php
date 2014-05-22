<?php
include("config.inc.php");

# Turn off stupid notices
error_reporting(E_ALL ^ E_NOTICE);

// Common functions in more than one sub-page
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


$url_regex = "/((http|https|ftp|irc):\/\/[^\s]+)/";

function analyzeLine($line) {
	global $url_regex;

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
		$idx = strpos($message, "> ");
		$nick = substr($message, 1, $idx-1);
		$text = htmlspecialchars(substr($message, $idx+2));
		
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

	return array('class' => $class, 'col2' => $col2, 'col3' => $col3);
}

// End Common functions

print("
<!DOCTYPE html>
<html>
<head>
<title>IRC Log for $channel on $server, collected by $botnick</title>

<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />
<meta name=\"description\" content=\"IRC Log for $channel\" />
<meta name=\"keywords\" content=\"IRC Log for $channel\" />
<link rel=\"stylesheet\" type=\"text/css\" href=\"inc/style.css\">

</head>

<body>
<a name='top'></a>
<h1>$channel IRC Log</h1>
");

?>