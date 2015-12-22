#Installing
Instructions on how to compile and install the source on a server

##Prerequisites
Download the following and extract them to a directory if the download is compressed:
- Google Web Toolkit SDK: http://www.gwtproject.org/download.html
- Apache Tomcat: https://tomcat.apache.org/download-90.cgi
- JSoup: http://jsoup.org/download
- Apache Ant: https://ant.apache.org/bindownload.cgi

##Compiling
1. Open src/com/achow101/bctalkaccountpricer/server/AccountPricer.java and change the strings in ACCOUNT_NAME and ACCOUNT_PASS to an actual working account username and password.
2. Open a command window in the BitcointalkAccountPricer directory.
3. Either add Apache Ant to the PATH or just copy the path to the bin folder. Run:
	ant build
4. In war/WEB-INF create a new folder lib
5. Add the JSoup jar to the lib directory
6. Add the gwt-servlet.jar file from the extracted gwt directory to the lib directory

##Running
1. Copy the war directory from the BitcointalkAccountPricer directory to the webapps directory in the Apache Tomcat directory
2. Rename that folder to bctalkaccountpricer
3. Open a command window in apache-tomcat/bin and run 
	startup
4. Open a browser and go to http://localhost:8080/bctalkaccountpricer/
