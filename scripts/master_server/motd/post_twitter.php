<?php

# Uses the twitter-php library found at: https://github.com/dg/twitter-php
require('twitter-php/twitter.class.php');


function post_to_twitter($message) {
	# These keys are for the @playbitfighter twitter account.
	# Log in and go to https://apps.twitter.com/ to manage them
	$consumerKey = "REPLACEME";
	$consumerSecret = "REPLACEME";
	$accessToken = "REPLACEME";
	$accessTokenSecret = "REPLACEME";

	$twitter = new Twitter($consumerKey, $consumerSecret, $accessToken, $accessTokenSecret);
	
	$twitter->send($message);
}

?>
