!define MUI_VERSION "1.01"
;;NOTE: the FIRST LINE of the file must be the !define MUI_VERSION, as above, as the release script automatically changes it.
;; The NETLOGO_VERSION below must be changed manually... it just affects the default installation folder
!define NETLOGO_VERSION "NetLogo 5.0.3"

!define MUI_PRODUCT "BehaviorSearch ${MUI_VERSION}"

!define BSEARCH_FILE_TYPE "BehaviorSearch.Protocol"
!define BSEARCH_FILE_DESC "BehaviorSearch Protocol"
!define BSEARCH_FILE_EXT ".bsearch"

;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"
;  !include "FileAssociation.nsh"

;--------------------------------
;General
  Name "${MUI_PRODUCT}"
  OutFile "behaviorsearch_${MUI_VERSION}_installer.exe"
 
  ;Request application privileges for Windows Vista
  RequestExecutionLevel admin

 !define MUI_ICON "icon_installer_hq.ico"
; !define MUI_UNICON "icon.ico"
; !define MUI_SPECIALBITMAP "installer_img.bmp"
 
 
;--------------------------------
;Folder selection page
 
  InstallDir "$PROGRAMFILES\${NETLOGO_VERSION}\${MUI_PRODUCT}"
 
 
;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "../LICENSE.TXT"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH

  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  !insertmacro MUI_UNPAGE_FINISH

;--------------------------------
;Languages

  !insertmacro MUI_LANGUAGE "English"

 
;-------------------------------- 
;Installer Sections     
Section "BehaviorSearch" Installation_info
 
;Add files
  SetOutPath "$INSTDIR"
 
  File /r /x *.sh behaviorsearch/*

;create desktop shortcut
  CreateShortCut "$DESKTOP\${MUI_PRODUCT}.lnk" "$INSTDIR\bsearch_gui_launch.exe" "" ;"$INSTDIR\resources\icon_behaviorsearch256.ico"

;create start-menu items
  CreateDirectory "$SMPROGRAMS\NetLogo\${MUI_PRODUCT}"
  CreateShortCut "$SMPROGRAMS\NetLogo\${MUI_PRODUCT}\${MUI_PRODUCT}.lnk" "$INSTDIR\bsearch_gui_launch.exe" ;"" "$INSTDIR\resources\icon_behaviorsearch256.ico"
  CreateShortCut "$SMPROGRAMS\NetLogo\${MUI_PRODUCT}\Uninstall.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0

  ;register .bsearch extension
;  ${registerExtension} "$INSTDIR\bsearch_gui_launch.bat" "${BSEARCH_FILE_EXT}" "${BSEARCH_FILE_TYPE}"
  WriteRegStr HKCR ${BSEARCH_FILE_EXT} "" "${BSEARCH_FILE_TYPE}"  ; set our file association
  WriteRegStr HKCR "${BSEARCH_FILE_TYPE}" "" "${BSEARCH_FILE_DESC}"
  WriteRegStr HKCR "${BSEARCH_FILE_TYPE}\shell" "" "open"
  WriteRegStr HKCR "${BSEARCH_FILE_TYPE}\DefaultIcon" "" "$INSTDIR\bsearch_gui_launch.exe,0"
  WriteRegStr HKCR "${BSEARCH_FILE_TYPE}\shell\open\command" "" '"$INSTDIR\bsearch_gui_launch.exe" "%1"'
  WriteRegStr HKCR "${BSEARCH_FILE_TYPE}\shell\edit" "" "Edit ${BSEARCH_FILE_DESC}"
  WriteRegStr HKCR "${BSEARCH_FILE_TYPE}\shell\edit\command" "" 'wscript.exe "$INSTDIR\behaviorsearch_gui.vbs" "%1"'
 
;write uninstall information to the registry
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${MUI_PRODUCT}" "DisplayName" "${MUI_PRODUCT} (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${MUI_PRODUCT}" "UninstallString" "$INSTDIR\Uninstall.exe"
 
  WriteUninstaller "$INSTDIR\Uninstall.exe"


SectionEnd
 
 
;--------------------------------    
;Uninstaller Section  
Section "Uninstall"
 
;Delete Files 
  RMDir /r "$INSTDIR\*.*"    
 
;Remove the installation directory
  RMDir "$INSTDIR"
 
;Delete Start Menu Shortcuts
  Delete "$DESKTOP\${MUI_PRODUCT}.lnk"
  Delete "$SMPROGRAMS\NetLogo\${MUI_PRODUCT}\*.*"
  RmDir  "$SMPROGRAMS\NetLogo\${MUI_PRODUCT}"
 
;Delete Uninstaller And Unistall Registry Entries
  DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\${MUI_PRODUCT}"
  DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${MUI_PRODUCT}"  

;unregister .bsearch extension
;  ${unregisterExtension} "${BSEARCH_FILE_EXT}" "${BSEARCH_FILE_TYPE}"
  DeleteRegKey HKCR ${BSEARCH_FILE_EXT}
  DeleteRegKey HKCR "${BSEARCH_FILE_TYPE}" ;Delete key with association name settings
 
SectionEnd
 
 
;--------------------------------    
;MessageBox Section
 
 
;Function that calls a messagebox when installation finished correctly
Function .onInstSuccess
  MessageBox MB_OK "You have successfully installed ${MUI_PRODUCT}. Use the desktop icon to start the program."
FunctionEnd
 
 
Function un.onUninstSuccess
  MessageBox MB_OK "You have successfully uninstalled ${MUI_PRODUCT}."
FunctionEnd
 

