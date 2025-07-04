# Employee Spring Full - POS System with Invoice Generation

This project consists of two separate Spring Maven applications:

1. **POS App** (Port 9000) - Main Point of Sale application
2. **Invoice App** (Port 9001) - Stateless invoice generation service

## Project Structure

```
employee-spring-full/
├── pos/                    # Main POS application
│   ├── src/main/java/com/increff/pos/
│   │   ├── controller/     # REST controllers
│   │   ├── dto/           # Business logic orchestration
│   │   ├── api/           # Single entity operations
│   │   ├── dao/           # Data Access Objects
│   │   ├── entity/        # JPA entities
│   │   ├── model/         # Forms and responses
│   │   ├── spring/        # Spring configuration
│   │   └── util/          # Utility classes
│   └── pom.xml
├── invoice-app/           # Invoice generation service (Stateless)
│   ├── src/main/java/com/increff/invoice/
│   │   ├── controller/    # REST controllers
│   │   ├── dto/          # Business logic and validation
│   │   ├── model/        # Forms and responses
│   │   ├── spring/       # Spring configuration
│   │   ├── util/         # Utility classes (PDF generation)
│   │   └── exception/    # Exception handling
│   └── pom.xml
└── README.md
```

## Features

### POS App Features
- **Order Management**: Create and manage orders
- **Product Management**: Manage products and inventory
- **Invoice Generation**: Generate invoices for orders
- **Reporting**: Sales reports and day-on-day sales analysis
- **File Storage**: Local PDF storage for invoices

### Invoice App Features
- **PDF Generation**: Generate professional invoices using Apache FOP
- **Template Processing**: Use Velocity templates for invoice layout
- **Base64 Encoding**: Return PDF as base64 string for easy transfer

## Setup Instructions

### Prerequisites
- Java 8 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher

### Database Setup
1. Create MySQL database:
```sql
CREATE DATABASE pos_db;
CREATE DATABASE invoice_db;
```

2. Update database configuration in:
   - `pos/employee.properties`
   - `invoice-app/src/main/resources/com/increff/invoice/invoice.properties`

### Running the Applications

#### 1. Start Invoice App (Port 9001)
```bash
cd invoice-app
mvn clean install
mvn jetty:run
```

#### 2. Start POS App (Port 9000)
```bash
cd pos
mvn clean install
mvn jetty:run
```

## API Endpoints

### POS App Endpoints

#### Invoice Management
- `GET /invoice/generate-invoice/{id}` - Generate invoice for order
- `GET /invoice/get-invoice/{id}` - Download invoice PDF

#### Reporting
- `POST /reports/sales-report` - Generate sales report with filters
- `GET /reports/day-sales` - Get day-on-day sales report
- `POST /reports/calculate-day-sales` - Manually calculate daily sales

#### Existing Endpoints
- `GET /orders` - Search orders
- `POST /orders` - Create order
- `GET /orders/{id}/order-items` - Get order details
- `GET /products` - Get products
- `POST /products` - Create product

### Invoice App Endpoints
- `POST /invoice/generate` - Generate invoice PDF
- `GET /invoice/health` - Health check

## Invoice Generation Flow

1. **User requests invoice**: `GET /invoice/generate-invoice/{orderId}`
2. **POS app fetches order**: Gets order details and items
3. **POS app calls invoice-app**: Sends order data to invoice service
4. **Invoice app generates PDF**: Uses Apache FOP + Velocity template
5. **Invoice app returns base64**: Returns encoded PDF string
6. **POS app saves PDF**: Decodes and saves PDF locally
7. **POS app creates record**: Stores invoice metadata in database
8. **User downloads invoice**: `GET /invoice/get-invoice/{orderId}`

## Reporting Features

### Sales Report
- **Filters**: Date range, brand, category
- **Output**: Aggregated quantities and revenue by category
- **Endpoint**: `POST /reports/sales-report`

### Day-on-Day Sales Report
- **Scheduler**: Automatically calculates daily totals at 1:00 AM
- **Manual calculation**: `POST /reports/calculate-day-sales`
- **Date range filtering**: `GET /reports/day-sales?start-date=2024-01-01&end-date=2024-01-31`
- **Data**: Invoiced orders count, items count, total revenue

## Database Schema

### POS App Tables
- `orders` - Order information
- `order_items` - Order line items
- `products` - Product catalog
- `inventory` - Stock levels
- `invoice` - Invoice metadata
- `pos_day_sales` - Daily sales aggregates

### Invoice App Tables
- No database required (stateless service)
- No DAO or API layers needed (stateless architecture)

## Configuration

### POS App Configuration
- Database: `pos/employee.properties`
- Invoice storage: `invoices/` folder (configurable)
- Invoice app URL: `http://localhost:9001` (configurable)

### Invoice App Configuration
- No database configuration required (stateless service)
- Templates: `invoice-app/src/main/resources/templates/`

## Technologies Used

### POS App
- Spring MVC 4.3.6
- Hibernate 5.4.0
- MySQL 8.0
- Apache Commons
- ModelMapper
- Lombok

### Invoice App
- Spring MVC 4.3.6
- Apache FOP 2.7 (PDF generation)
- Apache Velocity 2.3 (template processing)
- Jackson (JSON processing)

## Development Notes

### Layering Convention
- **Controller**: REST endpoints and request/response handling
- **DTO**: Business logic orchestration, validation, and external service calls
- **API**: Single entity operations and data validation
- **DAO**: Database operations
- **Entity**: JPA entities and data models

#### DTO Layer Responsibilities (POS App)
- Orchestrates complex business operations that involve multiple APIs
- Handles external service calls (like invoice-app)
- Coordinates data transformations and aggregations
- Manages cross-cutting concerns like file operations
- Input validation and sanitization
- Error handling and user-friendly error messages

#### DTO Layer Responsibilities (Invoice App - Stateless)
- Handles both external validation and business logic
- PDF generation and invoice number creation
- Input validation and business rule enforcement
- Response formatting and error handling

#### API Layer Responsibilities
- Single entity operations and validation
- Business rules enforcement
- Transaction management
- Data integrity checks

### Error Handling
- Custom `ApiException` with error types
- Proper HTTP status codes
- Detailed error messages

### Security
- Spring Security integration
- Input validation
- File access controls

## Troubleshooting

### Common Issues
1. **Port conflicts**: Ensure ports 9000 and 9001 are available
2. **Database connection**: Check MySQL credentials and database existence
3. **PDF generation**: Verify Apache FOP dependencies
4. **File permissions**: Ensure write access to invoice storage directory

### Logs
- POS app: `pos/logs/`
- Invoice app: `invoice-app/logs/`
- Jetty logs: `jetty.log`

## Future Enhancements
- Email invoice delivery
- Invoice templates customization
- Advanced reporting features
- Multi-tenant support
- API rate limiting
- Caching layer 