@echo off
call ant clean
call ant deploy -Dversion.name=%1