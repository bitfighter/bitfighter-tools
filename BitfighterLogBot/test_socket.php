#!/usr/bin/php

<?php
$fp = fsockopen("localhost", 25959, $errno, $errstr, 5);

if (!$fp) {
	echo "$errstr ($errno)\n";
}

else {
	fwrite($fp, "some text\nraptor");
	fclose($fp);
}
?>

