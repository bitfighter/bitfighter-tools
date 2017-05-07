This is a ruby script that is used to sign the MacOS X packages for the Sparkle
updater.

Example usage:

   ./sign_update.rb /var/www/html/files/Bitfighter-019f-OSX-64bit-Intel.dmg dsa_priv.pem

'dsa_priv.pem' and 'dsa_pub.pem' must be generated beforehand.
'dsa_pub.pem' must be included into the MacOS X DMG during build.

Both of the key files are already generated and reside on the bitfighter server
