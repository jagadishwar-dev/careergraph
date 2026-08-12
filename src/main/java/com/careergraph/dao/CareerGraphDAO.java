package com.careergraph.dao;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import java.util.List;
import java.util.Map;

import com.careergraph.config.CognoDBConnection;

public class CareerGraphDAO {

    private final Driver driver;

    public CareerGraphDAO() {
        this.driver = CognoDBConnection.getDriver();
    }

    public void seedData() {

        try (Session session = driver.session()) {

            // Clear existing data
            session.run("MATCH (n) DETACH DELETE n");

            // Create Skills
            session.run("""
                CREATE
                (:Skill {name: 'Java'}),
                (:Skill {name: 'Spring'}),
                (:Skill {name: 'Spring Boot'}),
                (:Skill {name: 'SQL'}),
                (:Skill {name: 'Hibernate'}),
                (:Skill {name: 'HTML'}),
                (:Skill {name: 'CSS'}),
                (:Skill {name: 'JavaScript'}),
                (:Skill {name: 'Python'}),
                (:Skill {name: 'Machine Learning'})
            """);

            // Create Jobs
            session.run("""
                CREATE
                (:Job {title: 'Java Backend Developer'}),
                (:Job {title: 'Full Stack Developer'}),
                (:Job {title: 'Software Engineer'}),
                (:Job {title: 'Machine Learning Engineer'}),
                (:Job {title: 'Database Developer'})
            """);

            // Create Categories
            session.run("""
                CREATE
                (:Category {name: 'Backend Development'}),
                (:Category {name: 'Full Stack Development'}),
                (:Category {name: 'Software Engineering'}),
                (:Category {name: 'Data Science'})
            """);

            // Connect Jobs with required Skills
            session.run("""
                MATCH
                    (java:Skill {name: 'Java'}),
                    (spring:Skill {name: 'Spring'}),
                    (boot:Skill {name: 'Spring Boot'}),
                    (sql:Skill {name: 'SQL'}),
                    (hibernate:Skill {name: 'Hibernate'}),
                    (html:Skill {name: 'HTML'}),
                    (css:Skill {name: 'CSS'}),
                    (js:Skill {name: 'JavaScript'}),
                    (python:Skill {name: 'Python'}),
                    (ml:Skill {name: 'Machine Learning'}),

                    (backend:Job {title: 'Java Backend Developer'}),
                    (fullstack:Job {title: 'Full Stack Developer'}),
                    (software:Job {title: 'Software Engineer'}),
                    (machine:Job {title: 'Machine Learning Engineer'}),
                    (database:Job {title: 'Database Developer'})

                CREATE
                    (backend)-[:REQUIRES]->(java),
                    (backend)-[:REQUIRES]->(spring),
                    (backend)-[:REQUIRES]->(boot),
                    (backend)-[:REQUIRES]->(sql),
                    (backend)-[:REQUIRES]->(hibernate),

                    (fullstack)-[:REQUIRES]->(java),
                    (fullstack)-[:REQUIRES]->(spring),
                    (fullstack)-[:REQUIRES]->(html),
                    (fullstack)-[:REQUIRES]->(css),
                    (fullstack)-[:REQUIRES]->(js),

                    (software)-[:REQUIRES]->(java),
                    (software)-[:REQUIRES]->(sql),

                    (machine)-[:REQUIRES]->(python),
                    (machine)-[:REQUIRES]->(ml),
                    (machine)-[:REQUIRES]->(sql),

                    (database)-[:REQUIRES]->(sql),
                    (database)-[:REQUIRES]->(java)
            """);

            // Connect Jobs to Categories
            session.run("""
                MATCH
                    (backend:Job {title: 'Java Backend Developer'}),
                    (fullstack:Job {title: 'Full Stack Developer'}),
                    (software:Job {title: 'Software Engineer'}),
                    (machine:Job {title: 'Machine Learning Engineer'}),
                    (database:Job {title: 'Database Developer'}),

                    (backendCat:Category {name: 'Backend Development'}),
                    (fullstackCat:Category {name: 'Full Stack Development'}),
                    (softwareCat:Category {name: 'Software Engineering'}),
                    (dataCat:Category {name: 'Data Science'})

                CREATE
                    (backend)-[:BELONGS_TO]->(backendCat),
                    (fullstack)-[:BELONGS_TO]->(fullstackCat),
                    (software)-[:BELONGS_TO]->(softwareCat),
                    (machine)-[:BELONGS_TO]->(dataCat),
                    (database)-[:BELONGS_TO]->(backendCat)
            """);

            System.out.println("CareerGraph seed data inserted successfully.");
        }
        
        
    }
    
    public List<String> findJobsBySkills(List<String> skills) {

        try (Session session = driver.session()) {

            String cypher = """
                MATCH (s:Skill)<-[:REQUIRES]-(j:Job)-[:BELONGS_TO]->(c:Category)

                WITH j,
                     c,
                     collect(s.name) AS requiredSkills

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
                       matchedSkills,
                       missingSkills,
                       size(requiredSkills) AS totalSkills,
                       size(matchedSkills) AS matchedCount,
                       (size(matchedSkills) * 100.0 /
                        size(requiredSkills)) AS matchPercentage

                ORDER BY matchPercentage DESC
            """;

            return session.run(
                    cypher,
                    Map.of("skills", skills)
            ).list(record -> {

                String title =
                        record.get("title").asString();

                String category =
                        record.get("category").asString();

                double percentage =
                        record.get("matchPercentage").asDouble();

                List<String> matched =
                        record.get("matchedSkills").asList(
                                value -> value.asString()
                        );

                List<String> missing =
                        record.get("missingSkills").asList(
                                value -> value.asString()
                        );

                return title
                        + " | Category: " + category
                        + " | Match: "
                        + String.format("%.0f", percentage) + "%"
                        + " | Matched: "
                        + String.join(", ", matched)
                        + " | Missing: "
                        + String.join(", ", missing);
            });
        }
    }
}