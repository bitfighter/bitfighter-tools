#!/bin/bash
#
# Ugly script to create source tarball for bitfighter
#
# note that tags are handled differently in mercurial: they are essentially
#   an alias to a changeset.

# hardcoded vars
output_dir=/tmp


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
  echo "Usage: " `basename $0` "BITFIGHTER_VERSION CLONE_PATH_OR_URL"
  echo
  echo "Example: `basename $0` 019a /home/blah/hg/bitfighter"
  echo
  exit 0
fi

# Argument variables
version="$1"
server_side_clone="$2"
tag="bitfighter-$version"  # Always follows this pattern
tarball_root="bitfighter-$version"

echo " => You chose version $version"

echo " => output dir: $output_dir"

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

# Our excludes from the main bitfighter source
exclude_file="$output_dir"/bf_excludes.txt

excludes=$(cat <<EOF > $exclude_file
$tarball_root/.hg*
$tarball_root/lib
$tarball_root/zap/resource.h
$tarball_root/zap/ZAP.rc
$tarball_root/resource/fonts/*
$tarball_root/resource/bitfighter.xpm
$tarball_root/resource/redship64.png
$tarball_root/bitfighter_test
$tarball_root/build/*
$tarball_root/gtest
$tarball_root/libmodplug
$tarball_root/libogg
$tarball_root/libpng
$tarball_root/libsdl
$tarball_root/libspeex
$tarball_root/libvorbis
$tarball_root/misc
$tarball_root/mysql++
$tarball_root/openal
$tarball_root/other
$tarball_root/zlib
$tarball_root/updater/gpl.txt
$tarball_root/updater/License.txt
$tarball_root/updater/bin
$tarball_root/updater/lib
$tarball_root/updater/resource
$tarball_root/updater/src
$tarball_root/resource/sfx/gofast.sfs
$tarball_root/master/master?support*
$tarball_root/master/schema
EOF
)

# Compress
## Check for old file, and remove it
if [ -f "$output_dir"/"$tarball_root.tar.gz" ]; then
  echo " => Removing old archive"
  rm -f "$output_dir"/"$tarball_root.tar.gz"
fi

echo " => Compressing $tarball_root.tar.gz"
tar cfz "$tarball_root.tar.gz" --exclude-from="$exclude_file" "$tarball_root"

if [ $? -ne 0 ]; then
  echo "Tar did not compress properly.  Exiting"
  exit 1
fi

# Move to output_dir
mv $tarball_root.tar.gz "$output_dir/"

# Clean up my mess
echo " => Cleaning up"
rm -rf "$tmp_dir"
rm -f $exclude_file

echo "Done!"
echo
echo "Archive found at: $output_dir/$tarball_root.tar.gz"

exit 0
