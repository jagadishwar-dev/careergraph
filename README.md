# CareerGraph - Graph-Based Job Recommendation System

CareerGraph is a Java web application that recommends suitable jobs based on a user's technical skills.

The application uses a graph database to represent relationships between jobs, skills, and career categories. It calculates how closely a user's skills match the skills required for each job and identifies missing skills.

---

## 1. Use Case

A user enters their technical skills, for example:

```text
Java, SQL, Spring
```

CareerGraph analyzes the graph and returns suitable jobs ranked by skill-match percentage.

For each job, the application provides:

- Job title
- Career category
- Match percentage
- Matched skills
- Missing skills / skill gap

Example:

```text
Java Backend Developer
Category: Backend Development
Match: 60%
Matched Skills: Java, SQL, Spring
Skill Gap: Spring Boot, Hibernate
```

---

## 2. Why Use a Graph Database?

Career recommendations are naturally based on relationships.

A job requires multiple skills, and each job belongs to a career category.

CareerGraph represents these relationships directly:

```text
Skill <---- REQUIRES ---- Job
                          |
                          |
                     BELONGS_TO
                          |
                          v
                       Category
```

This makes it natural to traverse connected information such as:

```text
Skill -> Job -> Category
```

The graph structure also makes it easier to extend the system with additional relationships such as:

```text
Job -> Skill
Job -> Category
```

A relational database could represent this information using multiple tables and JOIN operations. A graph database represents relationships directly as graph edges, making relationship-oriented queries easier to express and extend.

---

## 3. Graph Data Model

### Nodes

### Skill

The application currently contains skills such as:

- Java
- Spring
- Spring Boot
- SQL
- Hibernate
- HTML
- CSS
- JavaScript
- Python
- Machine Learning

### Job

The application currently contains:

- Java Backend Developer
- Full Stack Developer
- Software Engineer
- Machine Learning Engineer
- Database Developer

### Category

The application currently contains:

- Backend Development
- Full Stack Development
- Software Engineering
- Data Science

### Relationships

```text
(Job)-[:REQUIRES]->(Skill)

(Job)-[:BELONGS_TO]->(Category)
```

Example:

```text
(Java Backend Developer)
          |
          | REQUIRES
          +------> Java
          +------> Spring
          +------> Spring Boot
          +------> SQL
          +------> Hibernate
          |
          | BELONGS_TO
          v
(Backend Development)
```

---

## 4. Architecture

CareerGraph follows a layered Java web application architecture:

```text
                    Browser
                       |
                       v
                  index.html
                       |
                       v
            JobRecommendationServlet
                       |
                       v
              CareerGraphService
                       |
                       v
                 CareerGraphDAO
                       |
                       v
               Neo4j Java Driver
                       |
                       v
                 CognoDB / Neo4j
```

### Components

#### Servlet

`JobRecommendationServlet`

Responsible for:

- Receiving HTTP requests
- Reading user skills
- Validating input
- Calling the service layer
- Returning recommendation results
- Handling errors

#### Service

`CareerGraphService`

Acts as the service/business layer between the servlet and DAO.

#### DAO

`CareerGraphDAO`

Responsible for:

- Database communication
- Creating graph seed data
- Executing Cypher queries
- Finding suitable jobs
- Calculating match information

#### Database Connection

`CognoDBConnection`

Creates and manages the Neo4j Java Driver connection using environment variables.

---

## 5. Recommendation Logic

The user enters a comma-separated list of skills.

Example:

```text
Java, SQL, Spring
```

The application:

1. Reads the user's skills.
2. Splits the input using commas.
3. Removes unnecessary whitespace.
4. Removes empty values.
5. Removes duplicate skills.
6. Queries the graph database.
7. Finds the required skills for each job.
8. Determines matched skills.
9. Determines missing skills.
10. Calculates the match percentage.
11. Sorts jobs by match percentage.
12. Displays the recommendations.

The match percentage is calculated as:

```text
Match Percentage =
(Matched Required Skills / Total Required Skills) × 100
```

For example, if a job requires 5 skills and the user has 3 of them:

```text
(3 / 5) × 100 = 60%
```

---

## 6. Multi-Hop Graph Traversal

CareerGraph uses graph traversal involving job, skill, and category relationships.

The main relationship path is:

```text
Skill <- REQUIRES - Job - BELONGS_TO -> Category
```

This allows the application to retrieve:

- Job
- Required skills
- Job category
- Matched skills
- Missing skills

from the graph.

The application uses a parameterized Cypher query with:

```text
$skills
```

rather than directly inserting user input into the Cypher query.

---

## 7. Main Cypher Query

The recommendation query is:

```cypher
MATCH (j:Job)-[:REQUIRES]->(s:Skill)
WITH j, collect(s.name) AS requiredSkills

MATCH (j)-[:BELONGS_TO]->(c:Category)

WITH j,
     c,
     requiredSkills,
     [skill IN requiredSkills
      WHERE skill IN $skills] AS matchedSkills

WITH j,
     c,
     requiredSkills,
     matchedSkills,
     [skill IN requiredSkills
      WHERE NOT skill IN $skills] AS missingSkills

RETURN j.title AS title,
       c.name AS category,
       size(requiredSkills) AS totalSkills,
       size(matchedSkills) AS matchedCount,
       matchedSkills,
       missingSkills,
       (size(matchedSkills) * 100.0 /
        size(requiredSkills)) AS matchPercentage

ORDER BY matchPercentage DESC
```

The `$skills` parameter is supplied by the Java application.

---

## 8. Seeded Graph Data

CareerGraph creates the initial graph data through the DAO.

### Skills

```text
Java
Spring
Spring Boot
SQL
Hibernate
HTML
CSS
JavaScript
Python
Machine Learning
```

### Jobs

```text
Java Backend Developer
Full Stack Developer
Software Engineer
Machine Learning Engineer
Database Developer
```

### Categories

```text
Backend Development
Full Stack Development
Software Engineering
Data Science
```

### Job-Skill Relationships

```text
Java Backend Developer
    -> Java
    -> Spring
    -> Spring Boot
    -> SQL
    -> Hibernate

Full Stack Developer
    -> Java
    -> Spring
    -> HTML
    -> CSS
    -> JavaScript

Software Engineer
    -> Java
    -> SQL

Machine Learning Engineer
    -> Python
    -> Machine Learning
    -> SQL

Database Developer
    -> SQL
    -> Java
```

---

## 9. Technologies Used

- Java
- Maven
- Java Servlets
- Apache Tomcat
- HTML
- CSS
- Neo4j Java Driver
- CognoDB
- Cypher
- Graph Database

---

## 10. Project Structure

```text
careergraph/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── careergraph/
│       │           │
│       │           ├── config/
│       │           │   └── CognoDBConnection.java
│       │           │
│       │           ├── dao/
│       │           │   └── CareerGraphDAO.java
│       │           │
│       │           ├── service/
│       │           │   └── CareerGraphService.java
│       │           │
│       │           └── servlet/
│       │               └── JobRecommendationServlet.java
│       │
│       └── webapp/
│           └── index.html
│
├── pom.xml
├── README.md
└── target/
    └── careergraph-0.0.1-SNAPSHOT.war
```

---

## 11. Environment Variables

Database credentials are not hard-coded in the application.

The application reads the following environment variables:

```text
COGNODB_URI
COGNODB_USERNAME
COGNODB_PASSWORD
```

Example:

```text
COGNODB_URI=<your CognoDB URI>
COGNODB_USERNAME=<your username>
COGNODB_PASSWORD=<your password>
```

Do not commit actual database credentials to GitHub.

---

## 12. Requirements

Before running the project, install:

- JDK
- Maven
- Apache Tomcat
- CognoDB / Neo4j database

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

---

## 13. Build the Project

Open a terminal in the project directory containing `pom.xml`.

Run:

```bash
mvn clean package
```

A successful build should produce:

```text
BUILD SUCCESS
```

The WAR file will be generated inside:

```text
target/
```

Example:

```text
target/careergraph-0.0.1-SNAPSHOT.war
```

---

## 14. Run with Apache Tomcat

Deploy the generated WAR file to Apache Tomcat.

Start the Tomcat server.

Then open:

```text
http://localhost:8080/careergraph/
```

The CareerGraph homepage will be displayed.

---

## 15. Using the Application

Enter technical skills into the skill input field.

Example:

```text
Java, SQL
```

Click:

```text
Find Matching Jobs
```

CareerGraph returns the jobs ranked according to their skill-match percentage.

Example:

```text
Software Engineer
Software Engineering
100% match
Matched Skills: Java, SQL

Database Developer
Backend Development
100% match
Matched Skills: Java, SQL

Java Backend Developer
Backend Development
40% match
Matched Skills: Java, SQL
Skill Gap: Spring, Spring Boot, Hibernate
```

The exact percentage depends on the skills entered by the user.

---

## 16. Input Validation

The application validates user input before querying the database.

If no skills are provided, the application returns:

```text
Please provide skills.
```

The application also:

- Trims whitespace
- Removes empty values
- Removes duplicate skills

For example:

```text
Java, Java, SQL
```

becomes:

```text
Java, SQL
```

---

## 17. Unknown Skills

If a user enters a skill that is not present in the graph, the application can return:

```text
No matching jobs found.
Try entering different skills.
```

For example:

```text
COBOL
```

will not match the currently seeded graph because COBOL is not one of the stored skills.

---

## 18. Error Handling

The servlet contains error handling around the service/database operation.

If a database or service error occurs, the application displays a user-friendly error page instead of exposing the raw exception to the user.

The error page provides a link to return to the CareerGraph homepage.

---

## 19. User Interface

The CareerGraph homepage provides a modern responsive interface containing:

- CareerGraph branding
- Job recommendation introduction
- Skill input field
- Job search button
- Match percentage feature
- Skill-gap feature
- Graph-powered recommendation feature
- Responsive layout
- Search-again navigation

### Screenshots

Screenshots can be added to the repository under:

```text
docs/
├── homepage.png
├── recommendations.png
└── skill-gap.png
```

They can then be displayed in this README using:

```markdown
![CareerGraph Homepage](docs/homepage.png)

![Job Recommendations](docs/recommendations.png)
```

---

## 20. Graph Data Model Diagram

The main graph structure is:

```text
                    ┌─────────────────┐
                    │      Job        │
                    └────────┬────────┘
                             │
                    REQUIRES │
                             ▼
                    ┌─────────────────┐
                    │     Skill       │
                    └─────────────────┘

                    ┌─────────────────┐
                    │      Job        │
                    └────────┬────────┘
                             │
                   BELONGS_TO│
                             ▼
                    ┌─────────────────┐
                    │    Category     │
                    └─────────────────┘
```

Example:

```text
                 ┌─────────────────────────┐
                 │ Java Backend Developer  │
                 └────────────┬────────────┘
                              │
                    ┌─────────┼──────────┐
                    │         │          │
                 REQUIRES   REQUIRES   REQUIRES
                    │         │          │
                    ▼         ▼          ▼
                 Java      Spring       SQL
                             
                              │
                         BELONGS_TO
                              │
                              ▼
                 ┌──────────────────────┐
                 │ Backend Development  │
                 └──────────────────────┘
```

![CareerGraph Graph Data Model](docs/careergraph-graph-model.png)

---

## 21. Security Considerations

Database credentials are loaded from environment variables rather than being stored directly in source code.

Cypher queries use parameters such as:

```text
$skills
```

instead of directly concatenating user input into the query.

This helps prevent unsafe query construction.

---

## 22. Future Improvements

Possible improvements include:

- Increasing the number of jobs and skills
- Adding user profiles
- Adding skill proficiency levels
- Personalized career paths
- Job descriptions
- Salary information
- Location-based recommendations
- Learning-resource recommendations
- Authentication
- REST API
- Frontend framework integration
- More advanced graph-based recommendation algorithms

---

## 23. Project Status

The core CareerGraph application is implemented and tested.

The application currently supports:

- CognoDB connection
- Graph data creation
- Job and skill relationships
- Job category relationships
- Graph traversal
- User skill input
- Job recommendations
- Match percentage calculation
- Matched skill identification
- Skill-gap identification
- Duplicate skill removal
- Input validation
- Database error handling
- Responsive web UI
- Maven WAR packaging

---

## 24. Final Application Flow

```text
User
 |
 | Enters Skills
 v
CareerGraph UI
 |
 v
JobRecommendationServlet
 |
 v
CareerGraphService
 |
 v
CareerGraphDAO
 |
 v
CognoDB / Neo4j
 |
 | Graph Traversal
 v
Jobs + Required Skills + Categories
 |
 v
Match Calculation
 |
 +--------------------+
 |                    |
 v                    v
Matched Skills     Missing Skills
 |                    |
 +---------+----------+
           |
           v
    Ranked Job Results
           |
           v
        User
```

---

## 25. Conclusion

CareerGraph demonstrates how a graph database can be used to build a relationship-oriented job recommendation system.

Instead of simply searching for matching text, the application models relationships between jobs, skills, and career categories and uses those relationships to calculate job suitability and identify skill gaps.

The project combines Java Servlets, Maven, Apache Tomcat, the Neo4j Java Driver, CognoDB, Cypher, HTML, and CSS into a complete graph-based career recommendation application.