@echo off
set currentDir=%~dp0
"E:\Program Files\iPuTTY\pscp" -pw phnw2search -r "%currentDir%war" search@10.141.6.198:/home/search/gwt/testbed/shin/
