;Incluimos el Modern UI
  !include "MUI.nsh"
  !include "nsProcess.nsh"
  !include "Sections.nsh"

;Seleccionamos el algoritmo de compresion utilizado para comprimir nuestra aplicacion
SetCompressor lzma

;--------------------------------
;Con esta opcion alertamos al usuario y le pedimos confirmacion para abortar
;la instalacion
  !define MUI_ABORTWARNING
  !define MUI_HEADERIMAGE
  !define MUI_HEADERIMAGE_BITMAP "ic_head.bmp"
  !define MUI_HEADERIMAGE_UNBITMAP "ic_head.bmp"
  !define MUI_WELCOMEFINISHPAGE_BITMAP "ic_install.bmp"
  !define MUI_UNWELCOMEFINISHPAGE_BITMAP "ic_install.bmp"
  !define VersionCheckNew "!insertmacro MVersionCheck"
  !define StrContains '!insertmacro "_StrContainsConstructor"'
  
;Definimos el valor de la variable VERSION, en caso de no definirse en el script
;podria ser definida en el compilador
!define VERSION "1.5.0"

;--------------------------------
;Paginas del instalador
  
  ;Mostramos la pagina de bienvenida
  !insertmacro MUI_PAGE_WELCOME
  ;Pagina donde mostramos el contrato de licencia 
  !insertmacro MUI_PAGE_LICENSE "licencia.txt"
  ;Pagina donde se selecciona el directorio donde instalar nuestra aplicacion
  !insertmacro MUI_PAGE_DIRECTORY
  ;Pagina de instalacion de ficheros
  !insertmacro MUI_PAGE_INSTFILES
  ;Pagina final
  !insertmacro MUI_PAGE_FINISH
  
;Paginas referentes al desinstalador
  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  !insertmacro MUI_UNPAGE_FINISH
  
 
;--------------------------------
;Idiomas
 
  !insertmacro MUI_LANGUAGE "Spanish"

; Para generar instaladores en diferentes idiomas podemos escribir lo siguiente:
;  !insertmacro MUI_LANGUAGE ${LANGUAGE}
; De esta forma pasando la variable LANGUAGE al compilador podremos generar
; paquetes en distintos idiomas sin cambiar el script

;;;;;;;;;;;;;;;;;;;;;;;;;
; Configuration General ;
;;;;;;;;;;;;;;;;;;;;;;;;;
;Nuestro instalador se llamara si la version fuera la 1.0: Ejemplo-1.0.exe
OutFile AutoFirma_32_installer.exe

;Aqui comprobamos que en la version Inglesa se muestra correctamente el mensaje:
;Welcome to the $Name Setup Wizard
;Al tener reservado un espacio fijo para este mensaje, y al ser
;la frase en espanol mas larga:
; Bienvenido al Asistente de Instalacion de Aplicacion $Name
; no se ve el contenido de la variable $Name si el tamano es muy grande
Name "AutoFirma"
Caption "Instalador de AutoFirma (Cliente @firma)"
Icon ic_launcher.ico

;Comprobacion de integridad del fichero activada
CRCCheck on
;Estilos visuales del XP activados
XPStyle on

/*
	Declaracion de variables a usar
*/
# tambien comprobamos los distintos
; tipos de comentarios que nos permite este lenguaje de script

Var PATH
Var PATH_ACCESO_DIRECTO
Var chromePath
;Indicamos cual sera el directorio por defecto donde instalaremos nuestra
;aplicacion, el usuario puede cambiar este valor en tiempo de ejecucion.
InstallDir "$PROGRAMFILES\AutoFirma"

; check if the program has already been installed, if so, take this dir
; as install dir
InstallDirRegKey HKLM SOFTWARE\AutoFirmacon@firma "Install_Dir"
;Mensaje que mostraremos para indicarle al usuario que seleccione un directorio
DirText "Elija un directorio donde instalar la aplicaci�n:"

;Indicamos que cuando la instalacion se complete no se cierre el instalador automaticamente
AutoCloseWindow false
;Mostramos todos los detalles del la instalacion al usuario.
ShowInstDetails show
;En caso de encontrarse los ficheros se sobreescriben
SetOverwrite on
;Optimizamos nuestro paquete en tiempo de compilacion, es altamente recomendable habilitar siempre esta opcion
SetDatablockOptimize on
;Habilitamos la compresion de nuestro instalador
SetCompress auto
;Personalizamos el mensaje de desinstalacion
UninstallText "Desinstalador de AutoFirma."

!macro MVersionCheck Ver1 Ver2 OutVar
 Push "${Ver1}"
 Push "${Ver2}"
  Call VersionCheck
 Pop "${OutVar}"
!macroend

!macro _StrContainsConstructor OUT NEEDLE HAYSTACK
  Push `${HAYSTACK}`
  Push `${NEEDLE}`
  Call un.StrContains
  Pop `${OUT}`
!macroend

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Instalacion de la aplicacion y configuracion de la misma            ;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

Section "Programa" sPrograma

	; Hacemos esta seccion de solo lectura para que no la desactiven
	SectionIn RO
	StrCpy $PATH "AutoFirma"
	StrCpy $PATH_ACCESO_DIRECTO "AutoFirma"
	; Comprueba que no este ya instalada
	  ClearErrors
	  ReadRegStr $R0 HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH\" "UninstallString"
	  ${If} ${Errors} 
		Goto Install
	  ${EndIf}
	  ReadRegStr $R1 HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH\" "DisplayVersion"
	  ${VersionCheckNew} $R1 ${VERSION} "$R2"
	  ${If} $R2 = 0
        MessageBox MB_OK "Esta versi�n de AutoFirma ya est� instalada." 
	  ${else}
		${If} $R2 = 2
		MessageBox MB_YESNO "Existe una versi�n anterior de AutoFirma en el equipo. �Desea desinstalarla?" /SD IDYES IDNO Exit
		Goto UninstallOlderVersion
		${else}
		MessageBox MB_OK "La versi�n actual de AutoFirma es m�s nueva que la que se quiere instalar."
		${EndIf}
      ${EndIf}
	Exit:
	  Quit
	UninstallOlderVersion:
	  ;Ejecuta el desinstalador cuya ruta ha sido obtenida del registro
	  ExecWait '"$R0" /S _?=$INSTDIR'
	Install:
	
	SetOutPath $INSTDIR\$PATH

	;Incluimos todos los ficheros que componen nuestra aplicacion
	File  AutoFirma.exe
	File  AutoFirmaCommandLine.exe
	File  AutoFirmaConfigurador.exe
	File  AutoFirmaTrayIcon.exe
	File  licencia.txt
	File  ic_firmar.ico

	;Hacemos que la instalacion se realice para todos los usuarios del sistema
   SetShellVarContext all
   
;Creamos tambien el aceso directo al instalador

	;creamos un acceso directo en el escitorio
	CreateShortCut "$DESKTOP\AutoFirma.lnk" "$INSTDIR\AutoFirma\AutoFirma.exe"

	;Menu items
	CreateDirectory "$SMPROGRAMS\AutoFirma"
	CreateShortCut "$SMPROGRAMS\AutoFirma\AutoFirma.lnk" "$INSTDIR\AutoFirma\AutoFirma.exe"
	CreateShortCut "$SMPROGRAMS\AutoFirma\AutoFirma TryIcon.lnk" "$INSTDIR\AutoFirma\AutoFirmaTryIcon.exe"
	;CreateShortCut "$SMPROGRAMS\AutoFirma\Desinstalar AutoFirma.lnk" "$INSTDIR\uninstall.exe"

	;Anade el TrayIcon al registro para que se inicie con Windows
	WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Run" "AutoFirma" "$INSTDIR\AutoFirma\AutoFirmaTrayIcon.exe"
	
	;Anade una entrada en la lista de "Program and Features"
		WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH\" "DisplayName" "AutoFirma"
		WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH\" "UninstallString" "$INSTDIR\uninstall.exe"
		WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH\" "DisplayIcon" "$INSTDIR\AutoFirma\AutoFirma.exe"
		WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH\" "NoModify" "1"
		WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH\" "NoRepair" "1"
		WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH\" "EstimatedSize" "100000"
		WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH\" "Publisher" "Gobierno de Espa�a"
		WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH\" "DisplayVersion" "${VERSION}"

	WriteUninstaller "$INSTDIR\uninstall.exe"

	WriteRegStr HKLM SOFTWARE\$PATH "InstallDir" $INSTDIR
	WriteRegStr HKLM SOFTWARE\$PATH "Version" "${VERSION}"

	;Exec "explorer $SMPROGRAMS\$PATH_ACCESO_DIRECTO\"
	
	;Registro
	;Firmar
	WriteRegStr HKEY_CLASSES_ROOT "*\shell\afirma.sign" "" "Firmar con AutoFirma"
	WriteRegStr HKEY_CLASSES_ROOT "*\shell\afirma.sign" "Icon" "$INSTDIR\AutoFirma\AutoFirma.exe"
	WriteRegStr HKEY_CLASSES_ROOT "*\shell\afirma.sign\command" "" "$INSTDIR\AutoFirma\AutoFirma.exe sign -gui -i %1" 

	;Cifrar / Crear sobre digital simple
	WriteRegStr HKEY_CLASSES_ROOT "*\shell\afirma.simpleenvelop" "" "Cifrar"
	WriteRegStr HKEY_CLASSES_ROOT "*\shell\afirma.simpleenvelop" "Icon" "$INSTDIR\AutoFirma\AutoFirma.exe"
	WriteRegStr HKEY_CLASSES_ROOT "*\shell\afirma.simpleenvelop\command" "" "$INSTDIR\AutoFirma\AutoFirma.exe createenvelope -gui -type simple -i %1" 
	
	;Cifrar y firmar / Crear sobre digital firmado
	WriteRegStr HKEY_CLASSES_ROOT "*\shell\afirma.signedenvelop" "" "Cifrar y firmar"
	WriteRegStr HKEY_CLASSES_ROOT "*\shell\afirma.signedenvelop" "Icon" "$INSTDIR\AutoFirma\AutoFirma.exe"
	WriteRegStr HKEY_CLASSES_ROOT "*\shell\afirma.signedenvelop\command" "" "$INSTDIR\AutoFirma\AutoFirma.exe createenvelope -gui -type signed -i %1" 

	;Descifrar / Abrir sobres digitales (enveloped)
 	WriteRegStr HKEY_CLASSES_ROOT ".enveloped\shell\afirma.enveloped" "" "Descifrar"
	WriteRegStr HKEY_CLASSES_ROOT ".enveloped\shell\afirma.enveloped" "Icon" "$INSTDIR\AutoFirma\AutoFirma.exe"
	WriteRegStr HKEY_CLASSES_ROOT ".enveloped\shell\afirma.enveloped\command" "" "$INSTDIR\AutoFirma\AutoFirma.exe openenvelope -i %1" 

	;Descifrar / Abrir sobres digitales (p7m)
 	WriteRegStr HKEY_CLASSES_ROOT ".p7m\shell\afirma.enveloped" "" "Descifrar"
	WriteRegStr HKEY_CLASSES_ROOT ".p7m\shell\afirma.enveloped" "Icon" "$INSTDIR\AutoFirma\AutoFirma.exe"
	WriteRegStr HKEY_CLASSES_ROOT ".p7m\shell\afirma.enveloped\command" "" "$INSTDIR\AutoFirma\AutoFirma.exe openenvelope -i %1" 

	;Verify
	; .csig
	WriteRegStr HKEY_CLASSES_ROOT ".csig" "" "Firma binaria CMS/CAdES"
	WriteRegStr HKEY_CLASSES_ROOT ".csig\DefaultIcon" "" "$INSTDIR\AutoFirma\ic_firmar.ico"
	WriteRegStr HKEY_CLASSES_ROOT ".csig\shell\Verify" "" "Verificar con AutoFirma"
	WriteRegStr HKEY_CLASSES_ROOT ".csig\shell\Verify\command" "" "$INSTDIR\AutoFirma\AutoFirma.exe verify -gui -i %1"
	;verify -gui -i %1"	

	;Verify
	; .xsig
	WriteRegStr HKEY_CLASSES_ROOT ".xsig" "" "Firma XMLDSig/XAdES"
	WriteRegStr HKEY_CLASSES_ROOT ".xsig\DefaultIcon" "" "$INSTDIR\AutoFirma\ic_firmar.ico"
	WriteRegStr HKEY_CLASSES_ROOT ".xsig\shell\Verify" "" "Verificar con AutoFirma"
	WriteRegStr HKEY_CLASSES_ROOT ".xsig\shell\Verify\command" "" "$INSTDIR\AutoFirma\AutoFirma.exe verify -gui -i %1"	
	
	;Verify
	; .p7s
	WriteRegStr HKEY_CLASSES_ROOT ".p7s" "" "Firma binaria CMS/CAdES"
	WriteRegStr HKEY_CLASSES_ROOT ".p7s\DefaultIcon" "" "$INSTDIR\AutoFirma\ic_firmar.ico"
	WriteRegStr HKEY_CLASSES_ROOT ".p7s\shell\Verify" "" "Verificar con AutoFirma"
	WriteRegStr HKEY_CLASSES_ROOT ".p7s\shell\Verify\command" "" "$INSTDIR\AutoFirma\AutoFirma.exe verify -gui -i %1"	
	
	;Protocolo afirma
	WriteRegStr HKEY_CLASSES_ROOT "afirma" "" "URL:Afirma Protocol"
	WriteRegStr HKEY_CLASSES_ROOT "afirma\DefaultIcon" "" "$INSTDIR\AutoFirma\ic_firmar.ico"
	WriteRegStr HKEY_CLASSES_ROOT "afirma" "URL Protocol" ""
	WriteRegStr HKEY_CLASSES_ROOT "afirma\shell\open\command" "" "$INSTDIR\AutoFirma\AutoFirma.exe %1"	
	
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Instalacion de la JRE
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

	; Hacemos esta seccion de solo lectura para que no la desactiven
	SectionIn RO

	StrCpy $PATH "AutoFirma\JRE"
	File /r "jre32b"
	Rename "$INSTDIR\AutoFirma\jre32b" "$INSTDIR\AutoFirma\jre"

	; Eliminamos los certificados generados en caso de que existan por una instalacion previa
	IfFileExists "$INSTDIR\AutoFirma\AutoFirma_ROOT.cer" 0 +1
	Delete "$INSTDIR\AutoFirma\AutoFirma_ROOT.cer"
	IfFileExists "$INSTDIR\AutoFirma\autofirma.pfx" 0 +1
	Delete "$INSTDIR\AutoFirma\autofirma.pfx"
	
	;Se cierra Firefox y Chrome si est�n abiertos
	${nsProcess::FindProcess} "firefox.exe" $R2
	StrCmp $R2 0 0 +1
	${nsProcess::KillProcess} "firefox.exe" $R0
	
	${nsProcess::FindProcess} "chrome.exe" $R3
	StrCmp $R3 0 0 +1
	${nsProcess::KillProcess} "chrome.exe" $R0
	Sleep 2000
	
	; Configuramos la aplicacion (generacion de certificados) e importacion en Firefox
	ExecWait '"$INSTDIR\AutoFirma\AutoFirmaConfigurador.exe" /passive'
	; Eliminamos los certificados de versiones previas del sistema
	Call DeleteCertificateOnInstall
	
	; Importamos el certificado en el sistema
	Push "$INSTDIR\AutoFirma\AutoFirma_ROOT.cer"
	Sleep 2000
	Call AddCertificateToStore
	Pop $0
	${If} $0 != success
	  ;MessageBox MB_OK "Error en la importaci�n: $0"
	${EndIf}
	
	; Obtenemos la ruta de los ficheros de GoogleChrome para cada usuario
	; System::Call "advapi32::GetUserName(t .r0, *i ${NSIS_MAX_STRLEN} r1) i.r2"
	
	FindFirst $0 $1 "C:\Users\*"
	loop1:
	StrCmp $1 "" done1
	StrCpy $chromePath "C:\Users\$1\AppData\Local\Google\Chrome\User Data"
	${If} ${FileExists} "$chromePath\Local State"

	;Se incluye AutoFirma como aplicaci�n de confianza en Google Chrome
	Push '"afirma":false,' #text to be replaced
	Push '' #replace with
	Push all #replace all ocurrences
	Push all #replace all other occurrences
	Push '$chromePath\Local State' #file to replace in
	Call AdvReplaceInFile
	
	Push '"protocol_handler":{"excluded_schemes":{' #text to be replaced
	Push '"protocol_handler":{"excluded_schemes":{"afirma":false,' #replace with
	Push all #replace all ocurrences
	Push all #replace all other occurrences
	Push '$chromePath\Local State' #file to replace in
	Call AdvReplaceInFile
	
	${EndIf}
	FindNext $0 $1
	Goto loop1
	done1:
	FindClose $0
	
	;Se restauran los navegadores cerrados
	${If} $R2 == 0
		ClearErrors
		ReadRegStr $R0 HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\firefox.exe\" ""
		${If} ${Errors} 
			Goto EndFirefox
		${EndIf}
		exec "$R0 --restore-last-session"
	${EndIf}
	EndFirefox:
	${If} $R3 == 0
		ClearErrors
		ReadRegStr $R0 HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\chrome.exe\" ""
		${If} ${Errors} 
			Goto EndChrome
		${EndIf}
		exec "$R0"
	${EndIf}
	EndChrome:
	${nsProcess::Unload}
SectionEnd


!define CERT_STORE_CERTIFICATE_CONTEXT  1
!define CERT_NAME_ISSUER_FLAG           1
!define CERT_NAME_SIMPLE_DISPLAY_TYPE   4

!define CERT_QUERY_OBJECT_FILE 1
!define CERT_QUERY_CONTENT_FLAG_ALL 16382
!define CERT_QUERY_FORMAT_FLAG_ALL 14
!define CERT_STORE_PROV_SYSTEM 10
!define CERT_STORE_OPEN_EXISTING_FLAG 0x4000
!define CERT_SYSTEM_STORE_LOCAL_MACHINE 0x20000
!define CERT_STORE_ADD_ALWAYS 4

;Function AddCertificateToStore

Function AddCertificateToStore
 
  Exch $0
  Push $1
  Push $R0
 

  System::Call "crypt32::CryptQueryObject(i ${CERT_QUERY_OBJECT_FILE}, w r0, \
    i ${CERT_QUERY_CONTENT_FLAG_ALL}, i ${CERT_QUERY_FORMAT_FLAG_ALL}, \
    i 0, i 0, i 0, i 0, i 0, i 0, *i .r0) i .R0"
 
  ${If} $R0 <> 0
 
    System::Call "crypt32::CertOpenStore(i ${CERT_STORE_PROV_SYSTEM}, i 0, i 0, \
      i ${CERT_STORE_OPEN_EXISTING_FLAG}|${CERT_SYSTEM_STORE_LOCAL_MACHINE}, \
      w 'ROOT') i .r1"
 
    ${If} $1 <> 0
 
      System::Call "crypt32::CertAddCertificateContextToStore(i r1, i r0, \
        i ${CERT_STORE_ADD_ALWAYS}, i 0) i .R0"
      System::Call "crypt32::CertFreeCertificateContext(i r0)"
 
      ${If} $R0 = 0
 
        StrCpy $0 "Unable to add certificate to certificate store"
 
      ${Else}
 
        StrCpy $0 "success"
 
      ${EndIf}
 
      System::Call "crypt32::CertCloseStore(i r1, i 0)"
 
    ${Else}
 
      System::Call "crypt32::CertFreeCertificateContext(i r0)"
 
      StrCpy $0 "No fue posible abrir el almac�n de certificados"
 
    ${EndIf}
 
  ${Else}
 
    StrCpy $0 "No fue posible abrir el fichero de certificados"
 
  ${EndIf}
 
  Pop $R0
  Pop $1
  Exch $0
 
FunctionEnd
 

Function VersionCheck
 Exch $R0 ; second version number
 Exch
 Exch $R1 ; first version number
 Push $R2
 Push $R3
 Push $R4
 Push $R5 ; second version part
 Push $R6 ; first version part
 
  StrCpy $R1 $R1.
  StrCpy $R0 $R0.
 
 Next: StrCmp $R0$R1 "" 0 +3
  StrCpy $R0 0
  Goto Done
 
  StrCmp $R0 "" 0 +2
   StrCpy $R0 0.
  StrCmp $R1 "" 0 +2
   StrCpy $R1 0.
 
 StrCpy $R2 0
  IntOp $R2 $R2 + 1
  StrCpy $R4 $R1 1 $R2
  StrCmp $R4 . 0 -2
    StrCpy $R6 $R1 $R2
    IntOp $R2 $R2 + 1
    StrCpy $R1 $R1 "" $R2
 
 StrCpy $R2 0
  IntOp $R2 $R2 + 1
  StrCpy $R4 $R0 1 $R2
  StrCmp $R4 . 0 -2
    StrCpy $R5 $R0 $R2
    IntOp $R2 $R2 + 1
    StrCpy $R0 $R0 "" $R2
 
 IntCmp $R5 0 Compare
 IntCmp $R6 0 Compare
 
 StrCpy $R3 0
  StrCpy $R4 $R6 1 $R3
  IntOp $R3 $R3 + 1
  StrCmp $R4 0 -2
 
 StrCpy $R2 0
  StrCpy $R4 $R5 1 $R2
  IntOp $R2 $R2 + 1
  StrCmp $R4 0 -2
 
 IntCmp $R3 $R2 0 +2 +4
 Compare: IntCmp 1$R5 1$R6 Next 0 +3
 
  StrCpy $R0 1
  Goto Done
  StrCpy $R0 2
 
 Done:
 Pop $R6
 Pop $R5
 Pop $R4
 Pop $R3
 Pop $R2
 Pop $R1
 Exch $R0 ; output
FunctionEnd

Function DeleteCertificateOnInstall
  ; Save registers
  Push $0
  Push $1
  Push $2
  Push $3
  Push $4
  Push $5
  ; Abre el almacen de CA del sistema
	    System::Call "crypt32::CertOpenStore(i ${CERT_STORE_PROV_SYSTEM}, i 0, i 0, \
      i ${CERT_STORE_OPEN_EXISTING_FLAG}|${CERT_SYSTEM_STORE_LOCAL_MACHINE}, \
      w 'ROOT') i .r1"
  ${If} $1 != 0
     StrCpy $2 0
     ; Itera sobre el almacen de certificados CA
     ${Do}
         System::Call "crypt32::CertEnumCertificatesInStore(i r1, i r2) i.r2"
         ${If} $2 != 0
            ; Obtiene el nombre del certificado
            System::Call "crypt32::CertGetNameString(i r2, \\
               i ${CERT_NAME_SIMPLE_DISPLAY_TYPE}, i 0, i 0, \\
               t .r4, i ${NSIS_MAX_STRLEN}) i.r3"
            ${If} $3 != 0
               ; Obtiene el emisor del certificado
               System::Call "crypt32::CertGetNameString(i r2, \\
                  i ${CERT_NAME_SIMPLE_DISPLAY_TYPE}, \\
                  i ${CERT_NAME_ISSUER_FLAG}, i 0, \\
                  t .r4, i ${NSIS_MAX_STRLEN}) i.r3"
               ${If} $3 != 0
				  ;Si el emisor es el AutoFirma ROOT
                  ${If} $4 == "AutoFirma ROOT"
                    System::Call "crypt32::CertDuplicateCertificateContext(i r2) i.r5"
				    System::Call "crypt32::CertDeleteCertificateFromStore(i r5)"
				  ${EndIf}
               ${EndIf}
               
            ${EndIf} 
         ${Else}
            ${ExitDo}
         ${EndIf}
     ${Loop}
     System::Call "crypt32::CertCloseStore(i r1, i 0)"
  ${EndIf}
  
  ; Restore registers
  Pop $5
  Pop $4
  Pop $3
  Pop $2
  Pop $1
  Pop $0
FunctionEnd 

Function un.DeleteCertificate
  ; Save registers
  Push $0
  Push $1
  Push $2
  Push $3
  Push $4
  Push $5
  ; Abre el almacen de CA del sistema
	    System::Call "crypt32::CertOpenStore(i ${CERT_STORE_PROV_SYSTEM}, i 0, i 0, \
      i ${CERT_STORE_OPEN_EXISTING_FLAG}|${CERT_SYSTEM_STORE_LOCAL_MACHINE}, \
      w 'ROOT') i .r1"
  ${If} $1 != 0
     StrCpy $2 0
     ; Itera sobre el almacen de certificados CA
     ${Do}
         System::Call "crypt32::CertEnumCertificatesInStore(i r1, i r2) i.r2"
         ${If} $2 != 0
            ; Obtiene el nombre del certificado
            System::Call "crypt32::CertGetNameString(i r2, \\
               i ${CERT_NAME_SIMPLE_DISPLAY_TYPE}, i 0, i 0, \\
               t .r4, i ${NSIS_MAX_STRLEN}) i.r3"
            ${If} $3 != 0
               ; Obtiene el emisor del certificado
               System::Call "crypt32::CertGetNameString(i r2, \\
                  i ${CERT_NAME_SIMPLE_DISPLAY_TYPE}, \\
                  i ${CERT_NAME_ISSUER_FLAG}, i 0, \\
                  t .r4, i ${NSIS_MAX_STRLEN}) i.r3"
               ${If} $3 != 0
				  ;Si el emisor es el AutoFirma ROOT
                  ${If} $4 == "AutoFirma ROOT"
                    System::Call "crypt32::CertDuplicateCertificateContext(i r2) i.r5"
				    System::Call "crypt32::CertDeleteCertificateFromStore(i r5)"
				  ${EndIf}
               ${EndIf}
               
            ${EndIf} 
         ${Else}
            ${ExitDo}
         ${EndIf}
     ${Loop}
     System::Call "crypt32::CertCloseStore(i r1, i 0)"
  ${EndIf}
  
  ; Restore registers
  Pop $5
  Pop $4
  Pop $3
  Pop $2
  Pop $1
  Pop $0
FunctionEnd 

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Configuracion de la desinstalacion ;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

Section "uninstall"
	StrCpy $PATH "AutoFirma"
	StrCpy $PATH_ACCESO_DIRECTO "AutoFirma"
	SetShellVarContext all
	
	;Se cierra Firefox y Chrome si est�n abiertos
	${nsProcess::FindProcess} "firefox.exe" $R2
	StrCmp $R2 0 0 +1
	${nsProcess::KillProcess} "firefox.exe" $R0
	
	;Se cierra Firefox si est� abierto
	${nsProcess::FindProcess} "chrome.exe" $R3
	StrCmp $R3 0 0 +1
	${nsProcess::KillProcess} "chrome.exe" $R0
	Sleep 2000
	
	;Eliminamos los certificados del sistema
	Call un.DeleteCertificate
	ExecWait '"$INSTDIR\AutoFirma\AutoFirmaConfigurador.exe" -uninstall /passive'
	
	; Obtenemos la ruta de los ficheros de GoogleChrome para cada usuario
	; System::Call "advapi32::GetUserName(t .r0, *i ${NSIS_MAX_STRLEN} r1) i.r2"
	
	FindFirst $0 $1 "C:\Users\*"
	loop2:
	StrCmp $1 "" done2
	StrCpy $chromePath "C:\Users\$1\AppData\Local\Google\Chrome\User Data"
	${If} ${FileExists} "$chromePath\Local State"
	;Se elimina AutoFirma como aplicaci�n de confianza en Google Chrome
	Push '"afirma":false,' #text to be replaced
	Push '' #replace with
	Push all #replace all ocurrences
	Push all #replace all other occurrences
	Push '$chromePath\Local State' #file to replace in
	Call un.AdvReplaceInFile
	
	${EndIf}
	FindNext $0 $1
	Goto loop2
	done2:
	FindClose $0
	
	RMDir /r $INSTDIR\$PATH
	;Borrar directorio de instalacion si es un directorio valido (contiene "AutoFirma" o es una subcarpeta de Program Files)
	${StrContains} $0 "Program Files (x86)\" $INSTDIR
	StrCmp $0 "Program Files (x86)\" DirectorioValido
	${StrContains} $0 "Program Files\" $INSTDIR
	StrCmp $0 "Program Files\" DirectorioValido
	${StrContains} $0 $PATH $INSTDIR
	StrCmp $0 "" PostValidacion
	DirectorioValido:
		RMDir /r $INSTDIR 
	PostValidacion:
	;Borrar accesos directorios del menu inicio
	Delete "$DESKTOP\AutoFirma.lnk"
	RMDir /r $SMPROGRAMS\$PATH_ACCESO_DIRECTO
	
	DeleteRegKey HKLM "SOFTWARE\$PATH"
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$PATH" 

	DeleteRegKey HKEY_CLASSES_ROOT "*\shell\afirma.sign"
	DeleteRegKey HKEY_CLASSES_ROOT "*\shell\afirma.simpleenvelop"
	DeleteRegKey HKEY_CLASSES_ROOT "*\shell\afirma.signedenvelop"
	DeleteRegKey HKEY_CLASSES_ROOT "*\shell\afirma.enveloped"
	DeleteRegKey HKEY_CLASSES_ROOT ".enveloped\shell\afirma.enveloped"
	DeleteRegKey HKEY_CLASSES_ROOT "*\shell\afirma.verify"

	DeleteRegKey HKEY_CLASSES_ROOT ".csig\shell\Verify"
	DeleteRegKey HKEY_CLASSES_ROOT ".xsig\shell\Verify"

	DeleteRegKey HKEY_CLASSES_ROOT "afirma"
	
	;Borramos las claves de registro en las que se almacenan las preferencias de la aplicacion
	DeleteRegKey HKCU "SOFTWARE\JavaSoft\Prefs\es\gob\afirma\standalone"
	DeleteRegKey /ifempty HKCU "SOFTWARE\JavaSoft\Prefs\es\gob\afirma"
	DeleteRegKey /ifempty HKCU "SOFTWARE\JavaSoft\Prefs\es\gob"
	DeleteRegKey /ifempty HKCU "SOFTWARE\JavaSoft\Prefs\es"

	;Se restauran los navegadores cerrados
	
	${If} $R2 == 0
		ClearErrors
		ReadRegStr $R0 HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\firefox.exe\" ""
		${If} ${Errors} 
			Goto EndFirefox1
		${EndIf}
		exec "$R0 --restore-last-session"
	${EndIf}
	EndFirefox1:
	${If} $R3 == 0
		ClearErrors
		ReadRegStr $R0 HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\chrome.exe\" ""
		${If} ${Errors} 
			Goto EndChrome1
		${EndIf}
		exec "$R0"
	${EndIf}
	EndChrome1:
	${nsProcess::Unload}

SectionEnd


; StrContains
; This function does a case sensitive searches for an occurrence of a substring in a string. 
; It returns the substring if it is found. 
; Otherwise it returns null(""). 
; Written by kenglish_hi
; Adapted from StrReplace written by dandaman32
 
 
Var STR_HAYSTACK
Var STR_NEEDLE
Var STR_CONTAINS_VAR_1
Var STR_CONTAINS_VAR_2
Var STR_CONTAINS_VAR_3
Var STR_CONTAINS_VAR_4
Var STR_RETURN_VAR
 
Function un.StrContains
  Exch $STR_NEEDLE
  Exch 1
  Exch $STR_HAYSTACK
  ; Uncomment to debug
  ;MessageBox MB_OK 'STR_NEEDLE = $STR_NEEDLE STR_HAYSTACK = $STR_HAYSTACK '
    StrCpy $STR_RETURN_VAR ""
    StrCpy $STR_CONTAINS_VAR_1 -1
    StrLen $STR_CONTAINS_VAR_2 $STR_NEEDLE
    StrLen $STR_CONTAINS_VAR_4 $STR_HAYSTACK
    loop:
      IntOp $STR_CONTAINS_VAR_1 $STR_CONTAINS_VAR_1 + 1
      StrCpy $STR_CONTAINS_VAR_3 $STR_HAYSTACK $STR_CONTAINS_VAR_2 $STR_CONTAINS_VAR_1
      StrCmp $STR_CONTAINS_VAR_3 $STR_NEEDLE found
      StrCmp $STR_CONTAINS_VAR_1 $STR_CONTAINS_VAR_4 done
      Goto loop
    found:
      StrCpy $STR_RETURN_VAR $STR_NEEDLE
      Goto done
    done:
   Pop $STR_NEEDLE ;Prevent "invalid opcode" errors and keep the
   Exch $STR_RETURN_VAR  
FunctionEnd

Function AdvReplaceInFile
Exch $0 ;file to replace in
Exch
Exch $1 ;number to replace after
Exch
Exch 2
Exch $2 ;replace and onwards
Exch 2
Exch 3
Exch $3 ;replace with
Exch 3
Exch 4
Exch $4 ;to replace
Exch 4
Push $5 ;minus count
Push $6 ;universal
Push $7 ;end string
Push $8 ;left string
Push $9 ;right string
Push $R0 ;file1
Push $R1 ;file2
Push $R2 ;read
Push $R3 ;universal
Push $R4 ;count (onwards)
Push $R5 ;count (after)
Push $R6 ;temp file name
 
  GetTempFileName $R6
  FileOpen $R1 $0 r ;file to search in
  FileOpen $R0 $R6 w ;temp file
   StrLen $R3 $4
   StrCpy $R4 -1
   StrCpy $R5 -1

loop_read:
 ClearErrors
 FileRead $R1 $R2 ;read line
 IfErrors exit
 
   StrCpy $5 0
   StrCpy $7 $R2
 
loop_filter:
   IntOp $5 $5 - 1
   StrCpy $6 $7 $R3 $5 ;search
   StrCmp $6 "" file_write1
   StrCmp $6 $4 0 loop_filter
 
StrCpy $8 $7 $5 ;left part
IntOp $6 $5 + $R3
IntCmp $6 0 is0 not0
is0:
StrCpy $9 ""
Goto done
not0:
StrCpy $9 $7 "" $6 ;right part
done:
StrCpy $7 $8$3$9 ;re-join
 
IntOp $R4 $R4 + 1
StrCmp $2 all loop_filter
StrCmp $R4 $2 0 file_write2
IntOp $R4 $R4 - 1
 
IntOp $R5 $R5 + 1
StrCmp $1 all loop_filter
StrCmp $R5 $1 0 file_write1
IntOp $R5 $R5 - 1
Goto file_write2
 
file_write1:
 FileWrite $R0 $7 ;write modified line
Goto loop_read
 
file_write2:
 FileWrite $R0 $R2 ;write unmodified line
Goto loop_read
 
exit:
  FileClose $R0
  FileClose $R1
 
   SetDetailsPrint none
  Delete $0
  Rename $R6 $0
  Delete $R6
   SetDetailsPrint lastused
 
Pop $R6
Pop $R5
Pop $R4
Pop $R3
Pop $R2
Pop $R1
Pop $R0
Pop $9
Pop $8
Pop $7
Pop $6
Pop $5
;These values are stored in the stack in the reverse order they were pushed
Pop $0
Pop $1
Pop $2
Pop $3
Pop $4
FunctionEnd

Function un.AdvReplaceInFile
Exch $0 ;file to replace in
Exch
Exch $1 ;number to replace after
Exch
Exch 2
Exch $2 ;replace and onwards
Exch 2
Exch 3
Exch $3 ;replace with
Exch 3
Exch 4
Exch $4 ;to replace
Exch 4
Push $5 ;minus count
Push $6 ;universal
Push $7 ;end string
Push $8 ;left string
Push $9 ;right string
Push $R0 ;file1
Push $R1 ;file2
Push $R2 ;read
Push $R3 ;universal
Push $R4 ;count (onwards)
Push $R5 ;count (after)
Push $R6 ;temp file name
 
  GetTempFileName $R6
  FileOpen $R1 $0 r ;file to search in
  FileOpen $R0 $R6 w ;temp file
   StrLen $R3 $4
   StrCpy $R4 -1
   StrCpy $R5 -1
loop_read:
 ClearErrors
 FileRead $R1 $R2 ;read line
 IfErrors exit
 
   StrCpy $5 0
   StrCpy $7 $R2
 
loop_filter:
   IntOp $5 $5 - 1
   StrCpy $6 $7 $R3 $5 ;search
   StrCmp $6 "" file_write1
   StrCmp $6 $4 0 loop_filter
 
StrCpy $8 $7 $5 ;left part
IntOp $6 $5 + $R3
IntCmp $6 0 is0 not0
is0:
StrCpy $9 ""
Goto done
not0:
StrCpy $9 $7 "" $6 ;right part
done:
StrCpy $7 $8$3$9 ;re-join
 
IntOp $R4 $R4 + 1
StrCmp $2 all loop_filter
StrCmp $R4 $2 0 file_write2
IntOp $R4 $R4 - 1
 
IntOp $R5 $R5 + 1
StrCmp $1 all loop_filter
StrCmp $R5 $1 0 file_write1
IntOp $R5 $R5 - 1
Goto file_write2
 
file_write1:
 FileWrite $R0 $7 ;write modified line
Goto loop_read
 
file_write2:
 FileWrite $R0 $R2 ;write unmodified line
Goto loop_read
 
exit:
  FileClose $R0
  FileClose $R1
 
   SetDetailsPrint none
  Delete $0
  Rename $R6 $0
  Delete $R6
   SetDetailsPrint lastused
 
Pop $R6
Pop $R5
Pop $R4
Pop $R3
Pop $R2
Pop $R1
Pop $R0
Pop $9
Pop $8
Pop $7
Pop $6
Pop $5
;These values are stored in the stack in the reverse order they were pushed
Pop $0
Pop $1
Pop $2
Pop $3
Pop $4
FunctionEnd