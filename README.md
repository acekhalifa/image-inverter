# Image Processing System

An image converting api with Play Framework, featuring high-performance image processing using Akka actors.

##  Features

### Core Functionality

- ** Image Processing**: Parallel color inversion using Akka actors (R→G, G→B, B→R)
- ** RESTful API**: Fully documented with Swagger/OpenAPI

### Technical Highlights
- **Actor-Based Concurrency**: Leverages Akka actors for parallel image processing
- **Modular Architecture**: Clean separation between API, implementation, and gateway layers



## ⚡ Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/your-org/image-inverter.git
cd image-inverter

# 3. Build the project
mvn clean install

# 4. Run the application
cd service-gateway
sbt run

# 5. Access Swagger UI
# Open: http://localhost:9000/docs
```

##  Prerequisites

### Required
- **Java**: JDK 11 or higher
- **Maven**: 3.6+ for building modules
- **SBT**: 1.4.3+ for Play Framework

## 🔧 Installation


### Configure Application

Edit `service-gateway/conf/application.conf`:


# Image Storage Path
image.storage.path = "uploads/inverted"

# Akka Configuration (auto-configured based on CPU cores)



##  Configuration


### Akka Actor Configuration

```hocon
# Actor pool size (defaults to CPU cores)
akka.actor.deployment {
  /imageCoordinator/workerRouter {
    router = round-robin-pool
    nr-of-instances = 8  # Override if needed
  }
}
```



##  Running the Application

```powershell
cd service-gateway
mvn play2:run

**Application starts at**: `http://localhost:9000`

```



##  API Documentation

### Swagger UI

Access interactive API documentation:
```
http://localhost:9000/docs
```

### API Endpoints Overview

#### Image Processing

**Upload and Invert Image**:
```http
POST /api/image/upload
Content-Type: multipart/form-data

image: [Binary file data]
```

**Get Inverted Image**:
```http
GET /api/image/{filename}
```


###  Image Processing Workflow

```bash
# Upload and invert image
curl -X POST http://localhost:9000/api/image/upload \
  -F "image=@/path/to/photo.jpg"

# Response:
# {
#   "message": "Image inverted successfully using Akka actors",
#   "filename": "uuid-generated.png",
#   "url": "/api/image/uuid-generated.png"
# }

# Download inverted image
curl http://localhost:9000/api/image/uuid-generated.png \
  --output inverted-photo.png

# View in browser
open http://localhost:9000/api/image/generated.png
```






**Key Principles**:
- **Actor Model**: Message-driven concurrency
- **Supervision**: Fault-tolerant actor hierarchies
- **Parallel Processing**: Image divided into chunks
- **Non-blocking**: Asynchronous message passing
