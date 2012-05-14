@echo off
setlocal
set h=%~dp0
set lib=%h%lib
java -Dprog=%0 -Ddebug= -cp %lib%/relaxngDatatype-20020414.jar;%lib%/xsom.jar;%lib%/xsddiff.jar sg.Runner %*

@REM -Ddebug=XSDDiff:XSD                 colon separated classnames
@REM -Ddebugfile=/temp/xsddiff.debug     debugfile name o/w print on stderr