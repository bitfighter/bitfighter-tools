<?php

$auth_keys = array(
	"bitfighter" => "REPLACEME",
);

$data = file_get_contents("php://input");


/*
// Test dump headers and body
$headers=print_r($_SERVER, true);
$test="BODY:\n";
$test.=$data;
$test.="END\nHEADERS\n";
$test.=$headers;
file_put_contents("test.txt",$test);
*/


if (!isset($_SERVER["HTTP_GOOGLE_CODE_PROJECT_HOSTING_HOOK_HMAC"])) {
	die("No authentication hash.");
}

$hmac = $_SERVER["HTTP_GOOGLE_CODE_PROJECT_HOSTING_HOOK_HMAC"];

$json = json_decode($data, true);

if (!isset($json["project_name"]) || !in_array($json["project_name"],array_keys($auth_keys))) {
	die("Invalid project name.");
}

$p = $json["project_name"];
$digest = hash_hmac("md5", $data, $auth_keys[$p]);

if ($digest != $hmac) {
	die("Invalid authentication hash.");
}

// We've been authenticated!  Now do the important things

// Build up our commit messages.   'BREAK' will indicate a new line in strecm to the socket
$commits = array();

foreach($json["revisions"] as $revisions) {
	$author = name_from_email($revisions["author"]);
	$commit = substr($revisions["revision"], 0, 10);
	$message = $revisions["message"];

	$log = "Commit: $commit | Author: $author | Message: $message";

	array_push($commits, $log);
}


// Open a connection to logbot and post them
$fp = fsockopen("localhost", 25959, $errno, $errstr, 5);

if (!$fp) {
	die("Error posting to logbot: $errstr ($errno)");
}

foreach($commits as $log) {
	fwrite($fp, "$log\nBREAK\n");
}

fclose($fp);



function name_from_email($email) {
	$author = explode(' ', $email);
	$author = trim($author[0]);
	$author = ltrim(rtrim($author, '>'), '<');
	$author = explode('@', $author);
	$author = trim($author[0]);
	
	return $author;
}


?>
