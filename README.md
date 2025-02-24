# book-management
book-management: this is a simple project to discuss on the interview for the test automation engineer position.

### Development Environment
- IntelliJ IDEA (or any other IDE that supports Java and Maven)
- JDK 21
- Maven 3.8.0 or higher (recommended version 3.9.x for best compatibility with JDK 21)

### Preparation
1. Clone the repository
2. Build the project using `mvn clean install -DskipTests`
3. Run the services according to the instructions below
4. Run end-to-end tests according to the instructions below

### Task
Cover the main flow with an end-to-end test in `OrderTest` class.

# Book Management System 
The Book Management System is a simple system that consists of two services:
1. **Book Service:** This service is responsible for managing books. It provides APIs to create, update, delete, and retrieve books.
2. **Order Service:** This service is responsible for managing orders. It provides APIs to create, update, delete, and retrieve orders.

More details see in the [architecture.md](docs/architecture.md).

# Run Services

How to run services see in the [howToRunServices.md](docs/howToRunServices.md).

# End-to-end Tests
The end-to-end tests are located in the `e2e-tests` module.
```
e2e-tests/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── io/
│   │           └── github/
│   │               └── alexshamrai/
│   │                   ├── client/     
│   │                   └── dto/       
│   └── test/
│       ├── java/
│       │   └── io/
│       │       └── github/
│       │           └── alexshamrai/
│       │               └── e2e/        
│       └── resources/                 
└── pom.xml                          
```
### Testing Framework
- JUnit 5
- REST Assured
- AssertJ

### Data Processing
- Jackson
- Lombok

### Build
- Maven
- Maven Surefire Plugin

### Run Tests
```bash
mvn clean test
```
Note: before running tests, make sure that both services are up and running.
