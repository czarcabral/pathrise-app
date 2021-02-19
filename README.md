# Pathrise App Backend (Java)
This repo contains the server side code for the Pathrise App. This was written
 in Java Spring Boot and links to a PostgreSQL database.

### Link to the Frontend:
https://github.com/czarcabral/pathrise-app-angular

### How to run:
To save any time and headache, I've decided to deploy the app as is (in this 
repo) to Heroku. Here is the url to the backend of the web app. 
https://cabral-pathrise-app-java.herokuapp.com/

However, you can reach the main interface of this application though the 
frontend app. https://cabral-pathrise-app-angular.herokuapp.com/home

note: Due to the limitations of Heroku's hobby tier, Heroku places apps to 
sleep if there is no activity for at least 30 min. So both web apps may take 
up to a minute to load up initially, but on subsequent refreshes, it should 
be instantaneous.

### Third Party Technologies
- Java Spring Boot Framework: Starting by creating a skeleton app with the 
Spring Initializr tool, I like Spring Boot because this framework allows me 
to develop REST web services with very little configuration, so I can focus 
on the actual logic.
- Angular Framework: Similarly, after using the Angular CLI tool to create a 
skeleton angular app, I am able to jump straight in to developing the business
 logic. Developing in Angular is very straightforward and organized.
- Heroku: Hosting both applications as well as adding on PostgreSQL was 
extremely simple on Heroku.
- PostgreSQL: Initially attempting to use MySQL for it's simplicity, I 
migrated to PostgreSQL for its ability to do batch inserts. Especially due to 
having to insert 20,000 records from the job_opportunities.csv file.  

### How it works:
1. First, every time the server starts, it immediately parses the saved 
job_boards.json and job_opportunities.csv files, extracts the corresponding 
data from the files and persists them to the JOB_BOARD and JOB tables in the 
database, respectively. For each job opportunity that is read, I decided to 
extract the job source immediately from the url that is provided.
    1. This happens in the background and only takes about 15 seconds on 
    average.
    2. To extract the job source, I compared the domain of each url to the 
    root domains of the given 31 Job Boards.
    3. To check for if the job source was the Company Name, I checked if any 
    word of the company name appeared in the URL as well. I understand that 
    there is a chance that this may produce false positives, however, I 
    believe that this was more valuable because in many instances, only a 
    token of the company name appeared in the url e.g. `Veson Nautical` 
    `https://careers-veson.icims.com`
    4. Initially, while using MySQL, I had realized that MySQL does not 
    support batch inserts and therefore saving 20,000 records (or increments 
    of that) was taking a bit of time. I knew that I could speed up this 
    initialization of the DB records by saving in batches, so after much 
    research. I found that the best way would be to switch to another 
    Relational DB. I switched to PostgreSQL due to Heroku's support of it. 
2. Then I provided 2 REST APIs to provide access to the Job Board and Job 
(by company name) resources
3. When the Angular app loads, it immediately makes an API call to retrieve 
the Job Boards.
4. After populating the list of Job Boards and creating a card for each of 
them, you can click on one of them (including the Company Name and Unknown 
cards) to take you to another page.
5. In this page, I make another API call to retrieve all the Job opportunities
associated with this Job Board formatting the list into a table.

- Notes: Because this was a demonstration and not a production app, I decided
to clear and repopulate the database every time the server restarted in order
to demonstrate the correct parsing of the csv and json files. If this were to
be deployed to a production environment, I would remove that feature.
 