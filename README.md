# 🎉 Split Buddy

**Split Buddy** is a collaborative expense management application that simplifies tracking and splitting expenses among friends, families, or teams. With robust authentication, real-time email notifications, and reliable data validation, Split Buddy ensures smooth and secure group financial management.

---

## 🚀 Key Features

### 💸 Expense Management
- Create, view, and manage group-based shared expenses
- Supports multiple splitting strategies (e.g., **equal split**)
- Built-in validation to ensure fair and consistent expense entries

### 🔍 Validation Logic
- Validates individual shares for fairness
- Ensures the total split matches the original expense amount
- Throws descriptive errors for mismatched or invalid data

### 📧 Asynchronous Email Notifications (Kafka)
- Uses **Apache Kafka** for event-driven email delivery
- Non-blocking and decoupled: messages are processed by a dedicated consumer
- Email templates ensure consistent, informative notifications

---

## 🔐 Authentication & Authorization

### ✅ JWT-Based Authentication
- Stateless session management using **JSON Web Tokens**
- Tokens validated for every secured API request

### 🟡 Google OAuth2 Integration
- Sign in securely with Google accounts
- Maps Google profile to internal user model

### 📲 OTP-Based Email Verification
- OTP sent to email during registration for verification
- OTP has **5-minute expiry** and **resend** capability
- Enhances account security and email validity

### 🔒 Spring Security Endpoint Protection
- Fine-grained route protection 
    -  `/api/v1/auth/**` - Authentication routes (for registration and login)
    - `/api/v1/user/**` - General authenticated user access
    - `/api/v1/split/group/**` - Group and expense actions for authenticated users
- JWT filter integrated into Spring Security chain for stateless authentication

---

## 🗄️ Redis Integration

Split Buddy uses **Redis** as an in-memory store for:

- 🔄 **Refresh Tokens**
    - Used for securely refreshing JWT tokens
    - Stored with expiry time

- 🔐 **OTP Codes**
    - Temporary OTP values mapped to user emails
    - Auto-expire after 5 minutes

---

## ⚙️ Tech Stack

| Component       | Description                                       |
|-----------------|---------------------------------------------------|
| Java 17+        | Core language                                     |
| Spring Boot     | Application framework                             |
| Spring Security | Authentication and authorization                  |
| JWT             | Token-based stateless session management          |
| Google OAuth2   | Third-party login via Google                      |
| Redis           | In-memory cache for OTP and refresh token storage |
| Apache Kafka    | Asynchronous event messaging                      |
| Lombok          | Reduces boilerplate code                          |
| SLF4J + Logback | Logging                                           |
| Maven           | Build and dependency management                   |

---

## 🛠️ Kafka Setup

Split Buddy uses **Apache Kafka** to send asynchronous email notifications.

### ▶️ Steps to Run Kafka Locally (Binary Version)

1. **Start Zookeeper**
   ```bash
   bin/zookeeper-server-start.sh config/zookeeper.properties
   ```
   
2. **Start Kafka Broker**
   ```bash
   bin/kafka-server-start.sh config/server.properties
   ```
   
3. **Create Topic**
    ```bash
   bin/kafka-topics.sh --create \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 1 \
    --topic notifications
   ```


## 🌐 Swagger API Documentation
### Explore and test all available APIs via Swagger UI.

#### 🔗Access:

    http://localhost:8090/swagger-ui/index.html

#### 🛡️ Authorizing Secured Endpoints:
Click on the Authorize button and enter:

    Bearer <your-jwt-token>
---

## 🧪 Running Locally

- **Clone the Repository**

    ```bash
        git clone https://github.com/ahmad-wasim-7109/split-buddy.git

    ```
   
- **Set Up Dependencies**
  Make sure the following are installed and running:

    - Java 17
    - Apache Kafka & Zookeeper
    - Redis Server

  - **Build the Project**
      ```bash
      mvn clean install
      ```

- **Run the Application**
    ```bash
    mvn spring-boot:run
    ```