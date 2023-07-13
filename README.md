# CCS308 Book Management System - Backend
This is an updated version of our [previous backend server](https://github.com/smgestupa/bms-mobile), which is part of [our project](https://github.com/smgestupa/ccs308-bms-mobile-application). This is a Spring Boot application, which utilizes MySQL as our main database management system (DBMS).

## Cloning the Repository
> Make sure to clone the [required Python files](https://github.com/laazyCmd/ccs308-bms-utils).

1. Choose a preferred directory
2. Open a terminal and clone the repository: `git clone git@github.com:laazyCmd/bms-backend.git`
3. Import the directory with your preferred IDE
4. Wait for Gradle to install and load the dependencies
5. Change the default properties in `application.properties` if necessary:
- change `spring.datasource.url` according to your database address; 
- if you are not using MySQL, change to a correct driver in `spring.datasource.driver-class-name`
6. Run the Spring Boot application by navigating to `src/main/kotlin/MainApplication.kt` and press `SHIFT + 10`
7. Open a browser to see if the server is successfully running in [`http://localhost:8080`](http://localhost:8080)
