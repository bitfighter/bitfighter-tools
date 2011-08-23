#!/usr/bin/python
#
#

import re     # for regex
import os     # for filesystem 
import sys    # system
import glob   # glob parsing


masterMap = {}

def parseLuaFunctions(filename):
  handle = open(filename)
  blockAlmostStarted = 0
  blockStarted = 0
  blockEnded = 0
  gameObject = ""
  
  for line in handle:
    if not blockStarted and re.search("^Lunar\s*\<(.*)\>::.*::.*\[\]\s*\=\s*", line, 0):
      blockAlmostStarted = 1
      gameObject = re.sub("^Lunar\s*\<(.*)\>::.*::.*\[\]\s*\=\s*[{]*", r'\1', line, 0).rstrip()
      
      masterMap[gameObject] = []
    
    if blockAlmostStarted and not blockStarted:
      if re.search("{", line, 0):
        blockStarted = 1
        continue

    if not blockEnded and re.search("^\s*};\s*$", line, 0):
      blockEnded = 1

    if blockStarted and not blockEnded and re.search("^\s*method.*$", line, 0):
      method = re.sub("^.*method\s*\(.*,\s*(.*)\).*$", r'\1', line, 0).rstrip()
      
      masterMap[gameObject].append(method)
      
  handle.close()
  
  return


def walkThewalk(path):
  for f in glob.glob( os.path.join(path, '*.cpp') ):
    parseLuaFunctions(f)


def printTheMap():
  for gameObject in sorted(masterMap.iterkeys()):
    print "\n" + gameObject + ":"
    
    for method in sorted( masterMap.get(gameObject) ):
      print "  " + method


## Now run the script
directory = ""

if len(sys.argv) > 1 and len(sys.argv) < 3:
  directory = sys.argv[1]
else:
  print "provide 1 directory to parse as argument"
  sys.exit(1)
  
walkThewalk(directory)
printTheMap()
