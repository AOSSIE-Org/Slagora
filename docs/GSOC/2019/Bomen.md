<img src="https://i.imgur.com/MXb1jv6.png" align="center" width="500" height="100"/>

# Agora Slack Application (Slagora)

**Student** : [Bomen Derick](https://gitlab.com/ponsipi)

**Organisation** : [AOSSIE](http://aossie.org/)

# Links  
- Project : 
  1. Slagora: https://gitlab.com/aossie/slagora
- Live demo of the Projects :  
  1. Slagora: https://slagora.herokuapp.com/
=  
# Slagora

The goal of this project is to provide a slack application that can be used by teams to create and manage polls directly in slack. Team admins can install the app and Signed in users can then use it to create elections in channels. At the end of every election results are sent to the channel and available to every member of the team.

### Use case modeling 

I have identified the following tasks in the project at the starting of the project.
1. Add to slack OAuth2 authentication for teams - **Done**
2. Sign in with slack OAuth2 authentication for team members - **Done** 
3. Users should be able to create election - **Done** 
4. Users should be able cast their vote  - **Done** 
5. Users should be able to view election details  - **Done** 
6. Users should be able to delete elections. - **Done** 
7. Users should be able to view results any time if election results are provided in real time - **Done** 
8. Application should be able to send final results at the end of each election  - **Done**
7. Application should be able handle the timeline of the Election. **Done**
8. User should be able to get help using slash command - **Done** 

### Deep view into the technology. 

This project is created using play framework 2.6 seeds [template](https://github.com/playframework/play-scala-seed.g8).

These are some of the main technologies, we have used in the project.

* [sbt](http://www.scala-sbt.org/) - Build tool for Scala.
* [Play framework](https://www.playframework.com/) - The Web framework is used to build the project.
* [Slack API](https://api.slack.com/) - Slack API.
* [Swagger Play](https://github.com/swagger-api/swagger-play) - Plugin that provides swagger documentation for play framework
* [Scala](https://www.scala-lang.org/) - Language which is used to write the server site.
* [MongoDB](https://docs.mongodb.com/) – Database used for the project. 
* [Silhouette Documentation](https://www.silhouette.rocks/docs) - Handle the user authentication and authorization for the project.
* [Play2-ReactiveMongoDB](http://reactivemongo.org/releases/0.1x/documentation/tutorial/play.html) - Used to connect with the MongoDB
* [specs2](https://github.com/etorreborre/specs2) - Used to unit tests.


We started the Google Summer of Code by defining the features that needed to be implemented. After which we created a slack application and added all the setups such as slack command and permissions required by Slagora. After that, we started to work with abstract schema of the models and create controllers to handle actions on those models. Then we started to work with the team and user authentication part. We use silhouette as our authentication library. After which we spent time to configure the Silhouette module and created and OAuth2 implementation for Slack team signup and OAuth2 implementation for slack user sign in still using silhouette. Finally we had to test our implementation with slack API making sure authentication is done correctly.


After that we started working on the election services and models.Then we searched a cloud solution for MongoDB. We use Mlab as our cloud database and found it to be great for development. We used [Play2-ReactiveMongoDB](http://reactivemongo.org/releases/0.1x/documentation/tutorial/play.html) to connect to mongoDB in order to store and get our election and user data.

 
We then created endpoints to receive slash command payloads from Slack. Implemented services to communicated with the Slack API and built interactive messages directly from our application. We created a fully functional system that enables slagora to determine which action the user has taken within slack and take appropriate actions. Some of which are create election, request for help, view election details, vote, request election results, delete election etc. 

We created results endpoints to serve the results for finished elections or elections that have support for real-time results. Head over to [Slagora](https://slagora.herokuapp.com/) to install the application in your team and start creating elections. I discussed a lot with my mentor on each step that was to be taken and he has always helped me with valuable guidance on what is needed.Though Slagora supports various features I can't really say it is production ready since we have to test it with real users and observe how it responds and we have in our roadmap a feature that will enable users communicate directly with Slagora Bot.

We structured the project to ease future development. Created models, view and controllers to ease application development. This will ease the work of new developers, I was greatly helped by mentor in coming up with a simple architecture for the application. 


### Merge Requests 
1. [ Merge request !1](https://gitlab.com/aossie/slagora/merge_requests/1) - Initialized the project with scala play seed template that includes: - status *Merged*
    * Play 2.6.13
    * MongoDB
    * Silhouette

2. [Merge request !2](https://gitlab.com/aossie/slagora/merge_requests/2) - Added continuous integration to Slagora repository: - status *Merged*
    *  Added a test job for the application

3. [Merge request !3](https://gitlab.com/aossie/slagora/merge_requests/3) - Added slack user authentication  - Status: *Merged*
    *  Created custom implementation of silhouette OAuth2 provider for Slack users
    *  Implemented endpoint for slack user authentication

4. [Merge request !4](https://gitlab.com/aossie/slagora/merge_requests/4) - Implemented add to slack authentication. - Status: *Merged*
    *  Created custom implementation of silhouette OAuth2 provider for Slack teams
    *  Implemented endpoint for slack team authentication


5. [Merge request !5](https://gitlab.com/aossie/slagora/merge_requests/5) - Create election - Status: *Merged*
    * Implement slash command for creating elections
    * Implement Dialog message for users to enter election data
    * Implement route that will collect payload data for election data
    * Implemented interactive message for users to enter election start and end date.

6. [Merge request !6](https://gitlab.com/aossie/slagora/merge_requests/6) - Invite voters and Send Results - Status: *Merged*
    * Implemented call to vote interactive message when a new election is created in the channel
    * Implemented an interactive message that enables users to vote
    * Implemented an interactive message to confirm users vote has been taken into account
    * Implemented an action for users to request for election results
    * Implemented an interactive message to display election results.

7. [Merge request !7](https://gitlab.com/aossie/slagora/merge_requests/7) – Implemented auto send result to channel for finished elections - Status: *Open*
    * Implemented a job to send results automatically at the end of the election. This will enable teams to view election results at the end of the election.

8. [Merge request !8](https://gitlab.com/aossie/slagora/merge_requests/7) - Included welcome page for slagora - Status: *Open*
    * Created a simple website for the application. It describes what the application is all about and provide links to other apps developed by AOSSIE. It also includes a means for Slack teams to directly install the app from the website

9. [Merge request !9](https://gitlab.com/aossie/slagora/merge_requests/9) - Implemented view election command - Status: *Open*
    * Implemented a means for users to view details of elections they created

10. [Merge request !10](https://gitlab.com/aossie/slagora/merge_requests/10) - Documentation - Status: *Open*
    * Improved readme file and included documentation about my GSOC work
    
