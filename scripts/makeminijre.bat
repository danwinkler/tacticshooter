SETLOCAL
SET BUILD_JDK_PATH=jre\jdk-19.0.1
..\%BUILD_JDK_PATH%\bin\jlink.exe --add-modules java.base,java.datatransfer,java.desktop,java.scripting,jdk.unsupported,jdk.dynalink --output ..\jre\minijre
ENDLOCAL
