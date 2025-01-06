# Meeting Room Booking System (Vida)

## Project Overview

The Meeting Room Booking System is a comprehensive software solution designed to streamline the process of booking and managing meeting rooms within an organization. The backend of the system is built using **Spring Boot (Java)** to provide robust, scalable, and efficient services.

This project was developed with a focus on user management, room scheduling, and automated notifications to enhance productivity and ensure seamless meeting room management.

---

## Features

### 1. User Management
- Create, update, and delete user accounts.
- Import and export user lists via CSV or Excel.

### 2. Room Management
- Add, edit, and remove meeting rooms.
- Associate rooms with specific departments for better organization.

### 3. Department Management
- Manage organizational departments.
- Assign users and rooms to corresponding departments.

### 4. Room Booking
- Schedule meeting room bookings with time and date.
- Modify or cancel bookings as needed.

### 5. Notifications
- Automated email reminders for upcoming meetings.
- Alerts for canceled or rescheduled bookings.

### 6. Data Import/Export
- Import user lists to bulk-add users to the system.
- Export user lists for reporting or administrative purposes.

---

## Tech Stack

### Backend
- **Spring Boot**: Core framework for building RESTful APIs.
- **Spring Data JPA**: For database operations.
- **Spring Security (JWT Token)**: For authentication and authorization.
- **Java Mail API**: For sending email notifications.

### Database
- **MySQL**: Used to store all user, room, department, and booking data.

### Tools and Libraries
- **Maven**: Dependency management and build automation.
- **Lombok**: For reducing boilerplate code.
- **Swagger**: API documentation and testing.

---

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/minhlong1802/vida.git
   ```

2. Navigate to the project directory:
   ```bash
   cd vida
   ```

3. Configure the database:
   - Update `application.properties` with your database connection details:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/meeting_booking
     spring.datasource.username=your_db_username
     spring.datasource.password=your_db_password
     ```

4. Build the project:
   ```bash
   mvn clean install
   ```

5. Run the application:
   ```bash
   java -jar target/vida-0.0.1-SNAPSHOT.jar
   ```

6. Access the API:
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Base URL: `http://localhost:8080/api`

---

## API Endpoints

### User Management
- `POST /api/users` - Add a new user.
- `GET /api/users` - Retrieve all users.
- `PUT /api/users/{id}` - Update user details.
- `DELETE /api/users/{id}` - Remove a user.

### Room Management
- `POST /api/rooms` - Add a new room.
- `GET /api/rooms` - Retrieve all rooms.
- `PUT /api/rooms/{id}` - Update room details.
- `DELETE /api/rooms/{id}` - Remove a room.

### Booking Management
- `POST /api/bookings` - Create a new booking.
- `GET /api/bookings` - Retrieve all bookings.
- `PUT /api/bookings/{id}` - Modify an existing booking.
- `DELETE /api/bookings/{id}` - Cancel a booking.

---

## Contribution

If you would like to contribute to this project:
1. Fork the repository.
2. Create a new branch for your feature or bug fix.
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Commit your changes and push to your branch.
   ```bash
   git commit -m "Add your message here"
   git push origin feature/your-feature-name
   ```
4. Create a pull request.

---

## Contact

If you have any questions or need assistance, feel free to contact me:
- **Email**: minhlong1802@example.com
- **GitHub**: [minhlong1802](https://github.com/minhlong1802)

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.
