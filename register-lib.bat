%~d0
cd %~p0
:: If you want to keep the Command Prompt Window,you can use 'cmd /k' option.
:: If you want to close the Commond Prompt Window,you can comment out the bellow.
cmd /c mvn install:install-file -Dfile=libs/rabbit-4.10.jar -DgroupId=org.khelekore -DartifactId=rabbit -Dversion=4.10 -Dpackaging=jar
cmd /c mvn install:install-file -Dfile=libs/rnio-1.2.jar -DgroupId=org.khelekore -DartifactId=rnio -Dversion=1.2 -Dpackaging=jar
