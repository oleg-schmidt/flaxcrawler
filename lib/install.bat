call mvn install:install-file -DgroupId=com.sleepycat -DartifactId=je -Dversion=4.0.71 -Dpackaging=jar -Dfile=.\je-4.0.71.jar
call mvn install:install-file -DgroupId=com.sleepycat -DartifactId=je -Dversion=3.3.75 -Dpackaging=jar -Dfile=.\je-3.3.75.jar
call mvn install:install-file -DgroupId=dsiutils -DartifactId=dsiutils -Dversion=1.0.10 -Dpackaging=jar -Dfile=.\dsiutils-1.0.10.jar
call mvn install:install-file -DgroupId=fastutil -DartifactId=fastutil -Dversion=5.1.5 -Dpackaging=jar -Dfile=.\fastutil-5.1.5.jar