' This script is just to avoid the black terminal box 
' that happens when you launch a .BAT file in Windows

Set WshShell = CreateObject("WScript.Shell")

ArgText = " "
Set objArgs = WScript.Arguments
For I = 0 to objArgs.Count - 1
   ArgText = ArgText & objArgs(I) & " "
Next

WshShell.Run chr(34) & Replace(WScript.ScriptFullName,".vbs",".bat") & Chr(34) & ArgText , 0
Set WshShell = Nothing
