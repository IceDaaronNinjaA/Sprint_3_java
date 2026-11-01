Set shell = CreateObject("WScript.Shell")
Set files = CreateObject("Scripting.FileSystemObject")
folder = files.GetParentFolderName(WScript.ScriptFullName)
jarFile = folder & "\RPGGame.jar"

shell.Run "javaw.exe -jar " & Chr(34) & jarFile & Chr(34), 0, False
