<?php

# Post-Commit Authentication Key -  I'm SECRET.  Don't commit me!
$commit_auth_key = 'IAMSECRET'; 

# Raw POST data
$raw = file_get_contents("php://input"); 

# HMAC keyed-hash header that Google Code sends
$hmac_header=$_SERVER['HTTP_GOOGLE_CODE_PROJECT_HOSTING_HOOK_HMAC'];

# Calculate the keyed-hash of the raw POST data
$verify=hash_hmac("md5", $raw, $commit_auth_key);


# Verify that the data is actually from Google Code.  If not, exit
if($hmac_header != $verify) {
	print("Unverified POST data!\nNot storing");
	exit;
}


# Now consume the JSON
$data = json_decode($raw, true);
#print_r($data);


# For each commit message, generate a new commit file
foreach($data["revisions"] as $d) {
	$revision = $d["revision"];
	$author = $d["author"];
	$message = $d["message"];

	#Generate the log for the logbot and write it to a file for consumption
	$log = "Commit: ".$revision." | Author: ".$author." | Message: ".$message."\n";

	# Open a socket to the logbot
	$port = 25959;
	$socket = fsockopen("localhost", $port, $errno, $errstr, 5);

	if(!$socket) {
		print("Could not create socket to log bot!");
		print("$errstr ($errno)");
		exit;
	}

	fwrite($socket, $log);
	fclose($socket);

	usleep(1500000);  # 1.5 seconds
}


?>
