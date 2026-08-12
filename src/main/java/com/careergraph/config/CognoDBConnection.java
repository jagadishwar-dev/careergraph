package com.careergraph.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class CognoDBConnection {

    private static final String URI = System.getenv("COGNODB_URI");
    private static final String USERNAME = System.getenv("COGNODB_USERNAME");
    private static final String PASSWORD = System.getenv("COGNODB_PASSWORD");

    private static final Driver driver =
            GraphDatabase.driver(
                    URI,
                    AuthTokens.basic(USERNAME, PASSWORD)
            );

    public static Driver getDriver() {
        return driver;	
    }

    public static void close() {
        driver.close();
    }
}