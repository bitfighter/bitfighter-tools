<?php
# Scrape to get levels from pleiades and output them to a level dir
#
#

date_default_timezone_set('UTC');

$server_dir = "/home/master/dedicated_servers/pleiades";
//$server_dir = "/home/raptor/temp";

$levels_dir = "$server_dir/levels";
$log_file = "$server_dir/pleiades_export.log";
$lastrun_file = "$server_dir/PLEIADES_EXPORT_LASTRUN.txt";


// Database garbage
$username = "REPLACEME";
$password = "REPLACEME";
#$server = "127.0.0.1";  // Don't use localhost for cron job
#$server = "localhost";  // Don't use localhost for cron job
$server = ":/var/lib/mysql/mysql.sock";  // Don't use localhost for cron job
$database = "pleiades";

$query = "

SELECT id, content, levelgen, levelgen_filename, level_filename, last_updated
FROM levels
where last_updated > '?1'

";


function connect_to_db() {
	global $username;
	global $password;
	global $server;
	global $database;
	
	$connection  = mysql_pconnect($server, $username, $password) or die("Could not connect: \n" . mysql_error());
	mysql_select_db($database, $connection) or die("Cannot select db $dbname: \n" . mysql_error());
	return $connection;
}


function check_extensions(array $array) {
	$return = true;
	$missing_ext_array = array();
	
	foreach ($array as $extension) {
		if (!extension_loaded($extension)) {
			$return = false;
			array_push($missing_ext_array, $extension);
		}
	}
	
	if (!$return) {
		logprint("You are missing the following php extensions:");
		foreach ($missing_ext_array as $missing) {
			logprint("  $missing");
		}
		exit;
	}
}

# Log a string to out log file
#
# This function adds a new line to the string
function logprint($string) {
	global $log_file;
	
	# We'll use error_log even though it is not an error..
	#error_log($string."\n", 3, $log_file);
	
	# Also write to stdout for fun!
	print($string."\n");
}


function get_lastrun_date() {
	global $lastrun_file;
	$latest_time = 0;
	
	if(!is_file($lastrun_file))
		logprint("No last run time.  Scraping entire database");

	else
		$latest_time = filemtime(realpath($lastrun_file));
		
	return date("Y-m-d H:i:s", $latest_time);
}


function get_database_data($date) {
	global $query;
	$array = array();
	
	# poor-mans parameter substitution
	$new_query = str_replace("?1", $date, $query);
	
	$connection = connect_to_db();
	
	$statement = mysql_query($new_query, $connection) or die('Could not execute query\n' . mysql_error());

	while ($row = mysql_fetch_array($statement, MYSQL_ASSOC)) {
		$id = $row["id"];
		$level = $row["content"];
		$levelgen = $row["levelgen"];
		$levelgen_filename = $row["levelgen_filename"];
		$level_filename = $row["level_filename"];
		$modified_date = $row["last_updated"];
		
		$new_row_array = array(
			'id' => $id,
			'level_content' => $level,
			'level_filename' => $level_filename,
			'levelgen_content' => $levelgen,
			'levelgen_filename' => $levelgen_filename,
			'modified_date' => $modified_date
		);
		
		array_push($array, $new_row_array);
	}
	
	mysql_close($connection);
	
	return $array;
}



function export_to_files(array $array) {
	global $levels_dir;
	
	foreach($array as $level) {
		$id = $level['id'];
		$level_content = $level['level_content'];
		$level_filename = $level['level_filename'];
		$levelgen_content = $level['levelgen_content'];
		$levelgen_filename = $level['levelgen_filename'];
		$modified_date = $level['modified_date'];
		
		logprint("Exporting $level_filename");
		
		# Now write out our level file
		$f = fopen("$levels_dir/$level_filename", 'w');
		fwrite($f, $level_content . "\nLevelDatabaseID $id\n");
		fclose($f);
		
		# Set modified date of file
		touch("$levels_dir/$level_filename", strtotime($modified_date));
		
		
		# See if we need to write out a levelgen file, too
		if(!empty($levelgen_filename)) {
			$f = fopen("$levels_dir/$levelgen_filename", 'w');
			fwrite($f, $levelgen_content);
			fclose($f);
			
			# Set modified date of file
			touch("$levels_dir/$levelgen_filename", strtotime($modified_date));
		}
	}
}


function export_pleiades() {
	global $levels_dir;
	global $lastrun_file;
	
	# Make sure our export folder is there
	if(!is_dir($levels_dir))
		passthru("mkdir -p $levels_dir");
	if(!is_dir($levels_dir)) {
		logprint("Output directory does not exist and failed to be created.");
		exit;
	}
	
	# Start export
	logprint("\nExporting pleiades levels to folder '$levels_dir'");
	$lastrun_time = get_lastrun_date();
	$master_array = get_database_data($lastrun_time);
	
	$level_count = count($master_array);
	if($level_count > 0) {
		logprint("Exporting $level_count updated files");
		export_to_files($master_array);
	}
	else
		logprint("No new levels to export.");
		
	# Finally udpate our lastrun file
	# And alter timestamp to match modified time
	touch("$lastrun_file", strtotime(date("Y-m-d H:i:s")));
}

## Start the script!
check_extensions(array('mysql'));

logprint("\nScript started on: ".date("Y-m-d H:i:s"));

export_pleiades();

?>
