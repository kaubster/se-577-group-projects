Software Architecture of a system for defining, populating and visualizing datasets. The architecture highlighted a focus on modifiability, interoperability and performance quality attributes. The architecture central focus was designed bearing extensbility and seperation of concerns in mind. 

The group earned an A. The project was very much a group effort. My contributions were the frontend site (src\main\resources), parsers (main.java.edu.drexel.se577.grouptwo.vis.filetypes) and relative to the REST API and its integration with the front end.

## Building with Gradle

This comes with the gradle wrapper, so all one needs to do to run the build
is `./gradlew build` or `gradlew.bat build` on windows (needs verification).

-----------------------------------------
Running the software
-----------------------------------------

## Run with Gradle
To run the software and host the website `./gradlew run` or `gradlew.bat run`.

## Access the web site
Browse to the application website at http://localhost:4567/

The website provides links to each of our UCs within the left navigation bar.  

Use case features include:
* Upload a Dataset : Select a file to upload as a dataset. The file must conform to specific format. Click link for file format details.
* Define a Dataset : Name then describe a new dataset. Upload the dataset to the server.
* Append To Existing Dataset : User selects an existing dataset using the pages UI to add new samples. 
* Visualize Dataset : User selects an existing dataset and visualization method. Data displayed using the chosen visualization method.


The website is responsive. When the browsers screen bounds are narrow navigation options will apear within the top left navigation pull down as seen under "Responsive Website Layout" below.
