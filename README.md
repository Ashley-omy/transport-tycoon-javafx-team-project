# 🚚🚗Transport Tycoon JavaFX Team Project 🛣🌳💶

A team-based Java game project inspired by Transport Tycoon, developed as part of a university software development course.

## Overview

This project is a transport simulation game where players build and manage transportation infrastructure. The application is designed based on the MVC architecture to separate game logic, user interaction, and visual rendering.

This game has been built using GitLab.

## How to Play

# Important Rules

・You are a boss of transport company. you are given initial budget. You can construct routes of transport and purchase vehicles with it.
・You make money when vehicles successfully unload materials, products or passanger in destinations.
・Maintenance fee occur and it increases as vehicles age. If a vehicle is overaged, you can sell it out.
・When no budget (coins) left, your company goes bankrupt and gameover.

# 1. Download the game

To download the game, please go to "Release" and click a jar file that corresponds a OS of your computer.

# 2. Make a route of transport

Make sure to construct roads so that they connect factories, mines and cities properly.
Factories consume materials from mines and then cities buy products from the factories. If you connect a city and a city and make a bus run on the road, passangers make profit.

Place pieces of road first, subsequentely place stops, where vehicles load and unload stuff or people. After that, place a garage, in which you are able to buy vehicles later. To make a route, click "Place Route" button on the side bar, subsequentely click stops that belong to the route one by one. Click "Place Route" once again after that.

# 3. Purchase vehicles

Click garage and choose a vehicles. There are two types: bus and truck. Buses carry passanger, while trucks carry materials and products. Price, speed, capacity and maintenance fee vary depending on vehicles so choose carefully considering them.

# 4. Restart and Save

## Technologies

- Java
- JavaFX
- Maven
- JUnit
- GitLab
- CI/CD pipelines

## My Contributions

- Designed UML diagrams to clarify the system architecture
- Contributed to the JavaFX-based UI implementation
- Implemented vehicle movement logic
- Worked in a team development environment using GitLab

## Architecture

The application follows the MVC pattern:

- Model: game world, entities, transport logic, and simulation state
- View: JavaFX UI and map rendering
- Controller: user input handling and coordination between Model and View

## Screenshots and Design Documents

### 1. UML Class Diagrams

These UML diagrams were designed to clearly visualize class relationships and method responsibilities at a glance. Following the MVC architecture, they were used to support smoother team collaboration and system design.

![Package Diagram](images/package_UML.png)

![Model Diagram](images/model_UML.png)

![View Diagram](images/view_UML.png)

![Controller Diagram](images/controller_UML.png)

### 2. CI/CD Pipeline

![CI Pipeline](images/pipeline.png)

Used GitLab CI pipelines to support automated testing and collaborative development.

### 3. GitLab Team Development

![GitLab IssueBoard](images/issue_board.png)
![GitLab RepositoryGraph](images/repository_graph.png)

These screenshots demonstrate the collaborative development workflow using GitLab, including branch management, issue tracking, task organization.
