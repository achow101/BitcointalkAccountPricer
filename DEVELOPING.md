#Developing
This document provides instructions for properly setting up the workspace to develop and work on this application. It will
assume that you are using Eclipse and have already set it up. It will also explain how to install on a web server.

## Setting up
1. Install the Google Plugin for Eclipse. Instructions [here](https://developers.google.com/eclipse/docs/download)
2. Import the project
  * Clone the project to your working directory
  * Go to File > New > Java Project. Set the project name to BitcointalkAccountPricer and the directory to the directory where you downloaded the project.
3. Add HTml Unit to the project
  * Download [HtmlUnit](http://htmlunit.sourceforge.net/)
  * Add it to the project by right clicking the project and selecting Build Path > Configure Build Path and going to the Libraries tab. Click Add External JARs... and select the HtmlUnit JAR files.
4. Add the GWT libraries by going again to the Libraries tab and clicking Add Library... then selecting Google Web Toolkit.

##Compiling In Eclipse
1. Right click on the project and select Google > GWT Compile and compile the application. You may also need to Project > clean and make sure that it automatically builds after the clean.
2. Check that all of the classes are in the war folder along with several JS files for GWT

##Testing and Debugging from Ecplise
1. Click the Debug button and select Super Dev Mode.
2. Open up a browser and go to the URL that appears in the Development Mode tab at the bottom of Eclipse.

##Installing on a server
1. Install Apache tomcat.
2. Compress the war folder into a zip file named BitcointalkAccountPricer. Change the file extension to ".war"
 -OR-
2. Copy the war folder to the webapps folder inside the Tomcat Home directory and rename it to BitcointalkAccountPricer.
3. Start the Tomcat server. Everything should now be ready to go.
