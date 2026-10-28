PetPlace
### Where Every Pet Feels at Home

PetPlace is a pet e-commerce website designed to help users explore dog breeds, browse pet products, read pet-related news, and interact with a pet-loving community. The website includes user registration, login, shopping cart, customer reviews, newsletter subscriptions, contact messages, and order management.

##  Project Overview

PetPlace was developed to provide a user-friendly online platform for pet owners and people interested in pets. The project combines a web frontend, a Java Spring Boot backend, and a MySQL database.

##  Features

- **Home Page:** Introduces PetPlace and its services.
- **Dog Breeds:** Browse information about different dog breeds.
- **Pet Shop:** View available pet products.
- **User Authentication:** Register an account and sign in.
- **Shopping Cart:** Add and manage selected products.
- **Orders:** Capture customer and delivery information.
- **Customer Reviews:** Share feedback about dogs, products, and the website.
- **Newsletter:** Subscribe to receive updates.
- **Contact Page:** Submit enquiries and messages.
- **Pet News:** Browse pet-related news and information.

##  Technologies Used

| Technology | Purpose |
|---|---|
| HTML | Website structure |
| CSS | Website styling and layout |
| JavaScript | Frontend functionality and API requests |
| Java | Backend programming |
| Spring Boot | REST API development |
| MySQL | Database management |
| phpMyAdmin | Managing the MySQL database |
| Git and GitHub | Source code management |
| Netlify | Frontend hosting |

##  System Architecture

PetPlace uses a three-part architecture:

1. **Frontend:** The HTML, CSS, and JavaScript website that users interact with.
2. **Backend:** The Java Spring Boot application that processes requests and communicates with the database.
3. **Database:** MySQL stores application information, including user accounts, reviews, contact messages, newsletter subscriptions, and orders.

The frontend communicates with the backend through REST API endpoints. The backend uses database queries to store and retrieve information from MySQL.

## Project Structure


Petsplace/
├── frontend/
│   ├── index.html
│   ├── css/
│   ├── js/
│   ├── images/
│   └── data/
└── backend/
    ├── pom.xml
    └── src/
        └── main/
            ├── java/
            │   └── com/
            │       └── petsplace/
            │           ├── PetsPlaceApplication.java
            │           └── ApiController.java
            └── resources/
                ├── application.properties
                └── schema.sql


*Note: The folder structure above is illustrative. Update it if your actual repository uses different filenames or folders.*

## Requirements

Before running PetPlace locally, install:

- Java Development Kit (JDK 17 or a compatible version for your project).
- Apache Maven.
- MySQL Server through XAMPP or another MySQL installation.
- phpMyAdmin.
- A modern web browser.
- Visual Studio Code or another suitable code editor.

## Installation and Setup

### 1. Clone the repository


git clone https://github.com/PalesaCindi/Petsplace.git
cd Petsplace


### 2. Set up the MySQL database

1. Start Apache and MySQL in the XAMPP Control Panel.
2. Open phpMyAdmin at `http://localhost/phpmyadmin`.
3. Create a database named `petsplace`.
4. Create or import the required database tables and their relationships.

Ensure that the database name matches the configuration in the backend.

### 3. Configure the backend

Open the backend's `application.properties` file and configure the database connection.

Example for a local MySQL database:

properties
spring.application.name=petsplace-backend
server.port=8080

spring.datasource.url=jdbc:mysql://127.0.0.1:3306/petsplace?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver


Replace `YOUR_MYSQL_PASSWORD` with your local MySQL password. Keep credentials private and do not commit real passwords to GitHub.

### 4. Run the Spring Boot backend

Open a terminal in the backend directory, where `pom.xml` is located.


mvn spring-boot:run


The backend should start at:

`http://localhost:8080`

If Maven is not available on your system, use your installed Maven executable or configure Maven in your system's PATH.

### 5. Run the frontend

Open the frontend's `index.html` using a local development server, such as the Live Server extension in Visual Studio Code.

Make sure the frontend API configuration points to the running backend during local development:

javascript
const API_BASE = "http://localhost:8080";


The frontend and backend must both be running for database-backed features to work.

##  Deployment

The intended deployment setup is:

- **Frontend:** Netlify.
- **Backend:** A Java-compatible hosting service and Myphpadmin.
- **Database:** A remotely accessible MySQL database.

For a public deployment, configure the frontend to use the deployed backend URL rather than `localhost`. Configure the backend with the remote database connection details using environment variables.

**Important:** A local XAMPP/phpMyAdmin database is not automatically available to a publicly hosted backend. The backend and database must be deployed or hosted where they can communicate securely.

##  Security Considerations

- Keep database passwords and other secrets out of source control.
- Use secure password handling and authentication.
- Use HTTPS for the deployed website and API.
- Restrict database access to authorized connections.
- Validate and sanitize user input on the backend.
- Configure CORS to allow only the required frontend origins in production.

##  Future Enhancements

- Integrate a secure online payment gateway.
- Add order tracking and order history.
- Improve product search and filtering.
- Add an administration dashboard for products and orders.
- Introduce password reset functionality.
- Improve mobile responsiveness and accessibility.
- Add email notifications for orders and newsletter subscriptions.

## Author

**Developed by:** Palesa Cindi(ST10104134) & Ontlametse Moogapi(ST10244738)

**GitHub Repository:** [PalesaCindi/Petsplace1](https://github.com/PalesaCindi/PetsPlace1.git)

##  License

No license has been specified yet. Add a `LICENSE` file if you intend to publish the project under an open-source license.
