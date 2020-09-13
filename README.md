## Dependencies
1. Java 11
2. jackson-core/jackson-annotations/jackson-databind (2.11.2)
3. common-csv (1.8)
4. common-io (2.5)
## Set Up & Run
1. run `mvn clean package`
2. run `cd target`
3. run `java -jar ai.quod.challenge.HealthScoreCalculator-1.0-SNAPSHOT-jar-with-dependencies.jar [startTime] [endTime]`
- Example: `java -jar ai.quod.challenge.HealthScoreCalculator-1.0-SNAPSHOT-jar-with-dependencies.jar 2019-08-01T00:00:00Z 2019-08-01T01:00:00Z`
## Technical Decision
I decided to download and extract events file by file, so that I can even test the code with time larger than 1 hour in my computer.
Since I deleted the file after extracting information, I can save a lot of storage. Also, I decided to store event objects in memory (in the map) instead of writing them to the file, and process later.
That helps me to do the metric calculation faster since I don't have to read the file whenever I want to do the calculation. However, there is a drawback with this approach,
if there are too many event objects to be stored, the program might crash. Nevertheless, we have to compromise between speed and space.
 
## Future Improvement
#### ***Improvement for readability and maintainability?***
- Write function definition (input, output), and use cases for each function<br>
#### ***Improve your code for performance?***
- Research and apply some data processing techniques to improve the performance
(Honestly, at the moment, I don't know exactly how to do it)
- Read the java documents to understand how its data structures (List, Map, etc) are implemented
to use them in the right context
- There is no mechanism to preprocess the error json format. So far, if the json cannot be mapped to the
Event Object, They would be written to the *error_file.txt*
#### ***Frameworks/libraries***
- Libraries: Jackson and Apache common <br>
They help to facilitate the coding process. For example, there are many useful common utility functions from apache common 
such as read/write json file.


 








