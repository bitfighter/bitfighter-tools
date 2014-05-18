<?php
$ch = substr($channel, 1);
print("
<p>These logs were automatically created by <b>$botnick</b> on
<a href=\"irc://$server/$ch\">$server</a>.
</p>

</body>
</html>
");
?>