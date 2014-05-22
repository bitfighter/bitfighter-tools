<?php
//
//
//
include("inc/header.inc.php");

if(isset($_GET['action']))
	$action = $_GET['action'];

if(isset($_GET['searchstring']))
	$searchstring = $_GET['searchstring'];

$linelimit = 300;

print("
<p>
<a href=\"javascript:history.back()\">←Back</a>
<a href=\"index.php?index=true\">Index</a>
</p>
<h2>Search Logs</h2>
<p><b>Note:</b> Results will be limited to $linelimit lines.  If maximum is hit, results are not guaranteed to be sequential.</p>
");

if(isset($action) && $action == "search" && !empty($searchstring)) {
	$matchcase = isset($_GET['case']);
	$oldfirst = isset($_GET['oldfirst']);

	$caseflag = "i";
	if($matchcase)
		$caseflag = "";

	$output = array();

	$files = "*.log";

	// -n == show line number
	exec("grep -n$caseflag ".escapeshellarg($searchstring)." $files | head -n $linelimit", $output);


	if(count($output) == 0) {
		print("<b>No results found</b>");
	}
	else {
		// Sort output
		if($oldfirst)
			sort($output);
		else
			rsort($output);

		$currentdate = "";
		
		foreach($output as $line) {
			$pieces = explode(':', $line, 3);
			$date = substr($pieces[0], 0, 10);
			$linenumber = $pieces[1];
			$logline = $pieces[2];

			// Detect if we have a new log file
			$newdate = false;
			if($currentdate != "$date")
				$newdate = true;

			if($newdate === true) {
				// Close preceding table unless this is the first iteration
				if($currentdate != "")
					print("</table>\n");
				
				print("<h3><a href=\"index.php?date=$date\">$date</a></h3>\n");
				print("<table class=\"chat\">\n");
				
				$currentdate = $date;
			}
			
			$time = substr($logline, 1, 8);

			$results = analyzeLine($logline);

			$aref = "l$linenumber";
			print("<tr class=\"$results[class]\"><td class=\"time\" id=\"$aref\"><a href=\"index.php?date=$date#$aref\">$time</a></td><td class=\"nick\">$results[col2]</td><td class=\"message\">$results[col3]</td></tr>\n");
		}

		// Close the final table
		print("</table>\n");
	}
}

// Default search form
else {
	print("
<form>
	<input type=\"hidden\" name=\"action\" value=\"search\"/>
	<input type=\"text\" name=\"searchstring\" size=\"50\"/>
	<input type=\"submit\" value=\"Search\"/>
	<br/>
	<br/>
	<label><input type=\"checkbox\" name=\"case\"/>Case sensitive</label><br>
	<label><input type=\"checkbox\" name=\"oldfirst\"/>Oldest first</label><br>
</form>");

}

include("inc/footer.inc.php");

?>