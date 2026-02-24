# Automated Deployment of Java Web Application

## Overview
Team project delivering a fully automated 
CI/CD pipeline for a Java based web application 
using Jenkins, Maven, and AWS infrastructure.

## Team
Group of 4 DevOps Engineers

## Application
NumberGuessGame — Java Servlet web application
built with Maven and deployed on Tomcat

## Tech Stack
- Jenkins — CI/CD Pipeline from SCM
- Java/Maven — Build and test automation
- Tomcat — Application server
- Git/GitHub — Source control and SCM trigger
- AWS EC2 — Infrastructure

## Pipeline Flow
1. Developer pushes code to GitHub
2. Jenkins triggered automatically via webhook
3. Maven builds and tests the application
4. Artifact packaged and deployed to Tomcat
5. Application accessible publicly

## Key Features
- Pipeline as Code — Jenkinsfile in SCM
- Event driven triggers via GitHub webhooks
- Automated testing in pipeline
- Error handling and debugging measures
- Application monitoring post deployment

## Project Structure
\`\`\`
NumberGuessGame/
├── src/
│   └── main/
│       ├── java/com/studentapp/
│       │   └── NumberGuessServlet.java
│       └── webapp/
│           ├── WEB-INF/web.xml
│           └── index.jsp
├── pom.xml
├── Jenkinsfile
└── README.md
\`\`\`

## Lessons Learned
- Importance of pipeline as code for maintainability
- Team collaboration in DevOps workflows
- Event driven CI/CD reduces manual intervention
- Monitoring is critical for production reliability
