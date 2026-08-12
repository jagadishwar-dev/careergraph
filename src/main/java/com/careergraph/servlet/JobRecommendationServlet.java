package com.careergraph.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.careergraph.service.CareerGraphService;

@WebServlet("/recommend")
public class JobRecommendationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private CareerGraphService service;

    @Override
    public void init() throws ServletException {

        service = new CareerGraphService();

        System.out.println(
            "CareerGraphService created successfully"
        );
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        response.getWriter().println("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>CareerGraph</title>
            </head>

            <body>

                <h1>CareerGraph Job Recommendation</h1>

                <p>Servlet is working successfully.</p>

                <a href="index.html">Go to CareerGraph</a>

            </body>
            </html>
            """);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String skillsInput = request.getParameter("skills");

        // Validate input
        if (skillsInput == null || skillsInput.isBlank()) {

            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Please provide skills."
            );

            return;
        }

        // Convert input into skill list
        List<String> skills = Arrays.stream(
                    skillsInput.split(",")
                )
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .distinct()
                .toList();

        List<String> jobs;

        // Database error handling
        try {

            jobs = service.findJobsBySkills(skills);

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println("""
                <!DOCTYPE html>
                <html>

                <head>
                    <title>CareerGraph - Error</title>

                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #f4f6f8;
                            margin: 0;
                            padding: 40px;
                        }

                        .error {
                            max-width: 600px;
                            margin: auto;
                            background: white;
                            padding: 30px;
                            border-radius: 10px;
                            text-align: center;
                            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
                        }

                        a {
                            color: #2563eb;
                            text-decoration: none;
                        }
                    </style>
                </head>

                <body>

                    <div class="error">

                        <h1>Something went wrong</h1>

                        <p>
                            We could not connect to the
                            CareerGraph database.
                        </p>

                        <p>
                            Please try again later.
                        </p>

                        <a href="index.html">
                            ← Back to CareerGraph
                        </a>

                    </div>

                </body>
                </html>
                """);

            return;
        }

        // Successful response
        response.setContentType("text/html;charset=UTF-8");

        response.getWriter().println("""
            <!DOCTYPE html>
            <html>

            <head>

                <meta charset="UTF-8">

                <title>
                    CareerGraph - Job Recommendations
                </title>

                <style>

                    body {
                        font-family: Arial, sans-serif;
                        background: #f4f6f8;
                        margin: 0;
                        padding: 30px;
                    }

                    .container {
                        max-width: 900px;
                        margin: auto;
                    }

                    h1 {
                        text-align: center;
                        margin-bottom: 30px;
                    }

                    .job {
                        background: white;
                        margin: 20px 0;
                        padding: 20px;
                        border-radius: 10px;
                        box-shadow: 0 3px 10px rgba(0,0,0,0.1);
                    }

                    .job-title {
                        font-size: 21px;
                        font-weight: bold;
                    }

                    .category {
                        margin-top: 8px;
                        color: #555;
                    }

                    .match {
                        margin-top: 10px;
                        font-size: 18px;
                        font-weight: bold;
                    }

                    .matched {
                        margin-top: 12px;
                        color: green;
                    }

                    .missing {
                        margin-top: 10px;
                        color: #d97706;
                    }

                    .back {
                        text-align: center;
                        margin-top: 30px;
                    }

                    a {
                        color: #2563eb;
                        text-decoration: none;
                    }

                </style>

            </head>

            <body>

            <div class="container">

            <h1>CareerGraph Job Recommendations</h1>
            """);

        // No jobs found
        if (jobs.isEmpty()) {

            response.getWriter().println("""
                <div class="job">

                    <h2>No matching jobs found.</h2>

                    <p>
                        Try entering different skills.
                    </p>

                </div>
                """);

        } else {

            // Display jobs
            for (String job : jobs) {

                String[] parts = job.split("\\|");

                String title =
                    parts.length > 0 ? parts[0].trim() : "";

                String category =
                    parts.length > 1 ? parts[1].trim() : "";

                String match =
                    parts.length > 2 ? parts[2].trim() : "";

                String matched =
                    parts.length > 3 ? parts[3].trim() : "";

                String missing =
                    parts.length > 4 ? parts[4].trim() : "";

                response.getWriter().println(
                    "<div class='job'>"
                );

                response.getWriter().println(
                    "<div class='job-title'>"
                    + title +
                    "</div>"
                );

                response.getWriter().println(
                    "<div class='category'>"
                    + category +
                    "</div>"
                );

                response.getWriter().println(
                    "<div class='match'>"
                    + match +
                    "</div>"
                );

                if (!matched.isEmpty()) {

                    response.getWriter().println(
                        "<div class='matched'>"
                        + "✓ Matched Skills: "
                        + matched +
                        "</div>"
                    );
                }

                if (!missing.isEmpty()) {

                    response.getWriter().println(
                        "<div class='missing'>"
                        + "⚠ Skill Gap: "
                        + missing +
                        "</div>"
                    );
                }

                response.getWriter().println(
                    "</div>"
                );
            }
        }

        response.getWriter().println("""
            <div class="back">

                <a href="index.html">
                    ← Search Again
                </a>

            </div>

            </div>

            </body>
            </html>
            """);
    }
}