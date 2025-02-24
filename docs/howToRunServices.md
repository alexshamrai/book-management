# How to Run Services

## Run Services via script

Make the script executable
```bash
chmod +x ./scripts/run-applications.sh
```
Run the script
```bash
./scripts/run-applications.sh
```

## Running Services in Different Terminals
1. **Terminal #1:**
    - Navigate to `book-service`:
      ```bash
      cd book-service
      ```
    - Start the service:
      ```bash
      mvn spring-boot:run
      ```
2. **Terminal #2:**
    - Navigate to `order-service`:
      ```bash
      cd order-service
      ```
    - Start the service:
      ```bash
      mvn spring-boot:run
      ```
---