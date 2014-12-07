<!DOCTYPE html>
<html>
<head>
<title>Bitfighter MOTD</title>
<!--#include virtual="/inc/header.html" -->
</head>
<body id="new_motd">
<!--#include virtual="/inc/page_header.html" -->

<div class="simplebody" style="margin-top: 0">

<?php

require('post_twitter.php');

$myFile = "/home/master/bitfighter/exe/motd";
$fh = fopen($myFile, 'w') or die("Can't open file");
$motd = $_POST["motd"]."\n";
fwrite($fh, $motd);
fclose($fh);
print "<h1 style=\"color: #e0bf07\">MOTD has been updated!</h1>";

$twit = isset($_POST["twitter"]) ? $_POST["twitter"] : null;
if($twit == "yes") {
	post_to_twitter($motd);
}

?>
<p>Please restart Bitfighter and verify that the message displays correctly.</p>
<p>New MOTD:</p>
<div id="motd" style="border: 1px solid #ccc; padding: 10px; margin-top: 10px; margin-bottom: 20px;">
<!--#include virtual="./motd" -->
</div>

</div>
</body>
</html>
