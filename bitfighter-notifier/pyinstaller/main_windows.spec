# This is the pyinstaller SPEC file for building a standalone EXE
#
# Get pyinstaller here:
#    http://sourceforge.net/projects/pyinstaller/
#
# To build you need an installation of pyinstaller and run a command like the following:
# 
#    python -O /path/to/pyinstaller.py --upx-dir=/path/to/upxdir main_windows.spec
#
# Also, change the 'root_path' to point to your notifier installation

# -*- mode: python -*-
root_path = 'c:/hg/bitfighter.tools/bitfighter-notifier'

a = Analysis([os.path.join(root_path, 'main_windows.py')],
             hiddenimports=[],
             hookspath=None)
pyz = PYZ(a.pure)
exe = EXE(pyz,
          a.scripts + [('O','','OPTION')],
          a.binaries,
          a.zipfiles,
          a.datas,
          icon=os.path.join(root_path, 'redship48.ico'),
          name=os.path.join('dist', 'main_windows.exe'),
          debug=False,
          strip=None,
          upx=True,
          console=False )
