<?php

$url = 'http://bitfighter.org/webhooks/commit_hook.php';
$email = "REPLACE_WITH_RAPTORS_GMAIL";
$body=<<<EOF
{"repository_path": "https://code.google.com/p/bitfighter.misc/", "revisions": [{"url": "http://misc.bitfighter.googlecode.com/hg-history/45ecc6ef29cb448f17a192dae48b17b24ba3074d/", "path_count": 1, "timestamp": 1404835294, "parents": ["0d4a130867b2917a376be61e61b55c36d6cf4a51"], "modified": ["/README.txt"], "message": "Test", "revision": "45ecc6ef29cb448f17a192dae48b17b24ba3074d", "removed": [], "author": "$email", "added": [], "branch": "default"}], "project_name": "bitfighter", "revision_count": 1}
EOF;

$headers=array(
	'Content-Type: application/json; charset=UTF-8',
	'GOOGLE_CODE_PROJECT_HOSTING_HOOK_HMAC: 196f5d264396a563bf5a429bffa93606'
);

$ch = curl_init($url);                                                
curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt($ch, CURLOPT_HEADER, 1);
curl_setopt($ch, CURLOPT_TIMEOUT, 10);                                                                         
curl_setopt($ch, CURLOPT_POST, 1);
curl_setopt($ch, CURLOPT_POSTFIELDS, $body);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
                                                            
$response = curl_exec($ch);

print($response);

?>