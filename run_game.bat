@echo off
cd /d "%~dp0"
javac game.java Store.java GameGUI.java
if errorlevel 1 (
    echo Could not build the game. Make sure Java is installed.
    pause
    exit /b 1
)
start "" javaw game
