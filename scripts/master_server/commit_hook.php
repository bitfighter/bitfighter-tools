<?php

$SECRET_TOKEN = "REPLACEME";

$data = file_get_contents("php://input");

/*
// Test dump headers and body
$headers=print_r($_SERVER, true);
$test="BODY:\n";
$test.=$data;
$test.="\nEND\nHEADERS\n";
$test.=$headers;
$rn = rand();
file_put_contents("test$rn.txt",$test);
*/

if (!isset($_SERVER["HTTP_X_HUB_SIGNATURE"])) {
	die("No authentication hash.");
}

$hmac = $_SERVER["HTTP_X_HUB_SIGNATURE"];
$digest = hash_hmac("sha1", $data, $SECRET_TOKEN);

if ("sha1=$digest" != $hmac) {
	die("Invalid authentication hash.");
}

$event = $_SERVER["HTTP_X_GITHUB_EVENT"];
print("Event: $event\n");
print("success!\n");

// We've been authenticated!  Now do the important things
$json = json_decode($data, true);

// This hook works for all repos in our organization
$repo = $json["repository"]["name"];

$messages = array();

switch ($event) {
	case "commit_comment":
		$comment = $json["comment"];
		
		$commit = substr($comment["commit_id"], 0, 10);
		$user = $comment["user"]["login"];
		$message = substr($comment["body"], 0, 100);
		$url = $comment["html_url"];
		
		$log = "Repo: $repo | Comment on commit $commit | User: $user | Comment: $message ... | $url";
		
		array_push($messages, $log);
		break;
		
	case "issue_comment":
		$issue = $json["issue"];
		$comment = $json["comment"];
		
		$number = $issue["number"];
		$user = $comment["user"]["login"];
		$message = substr($comment["body"], 0, 120);
		$url = $comment["html_url"];
		
		$log = "Repo: $repo | Comment on issue #$number | User: $user | Comment: $message ... | $url";
		
		array_push($messages, $log);
		break;
		
	case "issues":
		$action = $json["action"];
		
		// We'll only handle 'opened','reopened', or 'closed' actions
		if($action != "opened" and $action != "reopened" and $action != "closed") {
			break;
		}
		
		$issue = $json["issue"];
		
		$number = $issue["number"];
		$title = $issue["title"];
		$user = $issue["user"]["login"];
		$url = $issue["html_url"];
		
		$log = "Repo: $repo | Issue #$number $action by $user | Title: $title | $url";
		
		array_push($messages, $log);
		break;
		
	case "pull_request":
		$action = $json["action"];
		
		// We'll only handle 'opened','reopened', or 'closed' actions
		if($action != "opened" and $action != "reopened" and $action != "closed") {
			break;
		}
		
		$pull = $json["pull_request"];
		
		$number = $pull["number"];
		$title = $pull["title"];
		$user = $pull["user"]["login"];
		$url = $pull["html_url"];
		
		$log = "Repo: $repo | Pull request #$number $action by $user | Title: $title | $url";
		
		array_push($messages, $log);
		break;
		
	case "push":
		$commits = $json["commits"];
		foreach($commits as $commit) {
			$author = $commit["author"]["username"];
			$commit_id = substr($commit["id"], 0, 10);
			$message = $commit["message"];

			$log = "Repo: $repo | Commit: $commit_id | Author: $author | Message: $message";

			array_push($messages, $log);
		}
		
		break;
		
	default:
		print("This event is not handled by this hook");
		break;
}


// Open a connection to logbot and post them
$fp = fsockopen("localhost", 25959, $errno, $errstr, 5);

if (!$fp) {
	die("Error posting to logbot: $errstr ($errno)");
}

// 'BREAK' will indicate a new line in stream to the socket (I wrote BFLogBot 
// to look for this)
foreach($messages as $log) {
	fwrite($fp, "$log\nBREAK\n");
}

fclose($fp);


?>
