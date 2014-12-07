<!DOCTYPE html>
<html>
<head>
    <title>Bitfighter MOTD</title>
    <!--#include virtual="/inc/header.html" -->
</head>
<body id="new_motd"  onload="document.motd.motd.focus();">
<!--#include virtual="/inc/page_header.html" -->

<div class="simplebody" style="margin-top: 0">

<form action="savemotd.php" name="motd" method="post">
<h1 style="color: #e0bf07">Bitfighter MOTD</h1>
<p>Use this form to set a new MOTD message that will be displayed when players start Bitfighter.</p>
<h3>Guidelines</h3>
<ul>
<li>Keep it clean
<li>Keep is clear
<li>Keep it simple
<li>Keep it factual
<li>Don't piss off the devs
</ul>
<h3>Existing MOTD</h3>
<div id="motd" style="border: 1px solid #ccc; padding: 10px; margin-top: 10px; margin-bottom: 20px;">
<!--#include virtual="./motd" -->
</div>
<h3>Enter New MOTD</h3>
<input type="text" name="motd" size="80" maxlength="200" />
<p><input type="checkbox" name="twitter" value="yes" checked="checked"/>Send to Twitter</p>
<p><b>Verify the text and options before submitting.</b></p>
<input type="submit" value="Submit" />
</form> 

</div>
</body>
</html>
