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

# Compress
echo " => Compressing $tarball_root.tar.gz"
tar cfz "$tarball_root.tar.gz" --exclude=.hg "$tarball_root"

if [ $? -ne 0 ]; then
  echo "Tar did not compress properly.  Exiting"
  exit 1
fi

# Move to output_dir
mv $tarball_root.tar.gz "$output_dir/"

# Clean up my mess
echo " => Cleaning up"
rm -rf "$tmp_dir"

echo "Done!"
echo
echo "Archive found at: $output_dir/$tarball_root.tar.gz"

exit 0
