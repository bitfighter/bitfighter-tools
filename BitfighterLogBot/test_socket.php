#!/usr/bin/php

<?php
$fp = fsockopen("localhost", 25959, $errno, $errstr, 5);

if (!$fp) {
	echo "$errstr ($errno)\n";
}

else {
	fwrite($fp, "some text\nraptor\nBREAK\nanother\ncommit\nBREAK\ntry3\nstill try3\n still try 3");
	//fwrite($fp, "single commit no break");
	fclose($fp);
}
?>

