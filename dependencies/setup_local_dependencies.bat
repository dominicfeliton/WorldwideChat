@echo off
echo Attempting to install stripped deluxechat...
echo Please change the -Dfile/-DlocalRepositoryPath arguments below and ensure that maven is installed, otherwise this WILL fail!
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile="C:\Users\Dominic\Desktop\WorldwideChat\WorldwideChat\dependencies\DeluxeChat-Stripped.jar" -DlocalRepositoryPath="C:\Users\Dominic\Desktop\WorldwideChat\WorldwideChat\dependencies\local-maven-repo" -Dfile=C:\Users\Dominic\Desktop\WorldwideChat\WorldwideChat\dependencies\DeluxeChat-Stripped.jar -DgroupId=com.expl0itz.stripped.deluxechat -DartifactId=stripped-deluxechat -Dpackaging=jar -Dversion=1.0.0 -DgeneratePom=true
if "%errorlevel%" == "9009" (
    echo Maven not found, please install it!
    exit /b
)