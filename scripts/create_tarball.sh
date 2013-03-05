#!/bin/bash
#
# Ugly script to create source tarball for bitfighter and upload to google code.
#
# note that tags are handled differently in mercurial: they are essentially
#   an alias to a changeset.

# hardcoded vars
# replace e-mail and google code password in between the quotes
gc_username="buckyballreaction@gmail.com"
gc_password="RD9Ex4rT7Hb8"
gc_upload_script_location="http://support.googlecode.com/svn/trunk/scripts/googlecode_upload.py"
gc_upload_script=`basename $gc_upload_script_location`

server_side_clone="https://bitfighter.googlecode.com/hg/"


# Check for mercurial
hg="`which hg`"

if [ -z $hg ]
then
    echo "This script requires mercurial Please install it."
    exit 1
fi

# Check for parameters
if [ -z "$2" ] || [ "$1" == "-h" ] || [ "$1" == "--help" ] 
then
  echo "Usage: " `basename $0` "TAG_NAME_OR_CHANGESET BITFIGHTER_VERSION"
  echo
  echo "Example: `basename $0` bitfighter-014a 014a"
  echo
  exit 0
fi

# Argument variables
tag="$1"
version="$2"
tarball_root="bitfighter-$version"

echo " => You chose version $version"

# Create temp dir
tmp_dir="`mktemp -d`"
echo " => tmp dir: $tmp_dir"
cd "$tmp_dir"

# Do the clone
echo " => Cloning $server_side_clone"
echo "    (may take a minute)"
hg clone "$server_side_clone" "$tarball_root" 1>/dev/null

if [ ! -d $tarball_root ]; then
  echo "Your source did not check out properly.  Check the path. Exiting"
  exit 1
fi

# Change to tag
echo " => Changing to tag/changeset $tag"
cd $tarball_root
hg up -r "$tag" 1>/dev/null
cd ..

# Compress
echo " => Compressing $tarball_root.tar.gz"
tar cfz "$tarball_root.tar.gz" --exclude=.hg "$tarball_root"

if [ $? -ne 0 ]; then
  echo "Tar did not compress properly.  Exiting"
  exit 1
fi

# Uncomment to not upload to google code
# exit

# Download the upload script on the fly
echo " => Downloading Google Code upload script, $gc_upload_script"
wget -q "$gc_upload_script_location"
chmod +x "$gc_upload_script"

if [ ! -f $gc_upload_script ]; then
  echo "The Google Code upload script was not downloaded correctly.  Check the URL. Exiting"
  exit 1
fi

# Upload to google code
echo " => Uploading to Google Code (may take a minute)"
./$gc_upload_script --summary="bitfighter $version source archive" --project=bitfighter --user="$gc_username" --password="$gc_password" $tarball_root.tar.gz

if [ $? -ne 0 ]; then
  echo "There was a failure in uploading.  Exiting"
  exit 1
fi

# Clean up my mess
echo " => Cleaning up"
rm -rf "$tmp_dir"

echo "Done!"

exit 0
