#!/bin/bash

# Employee Spring Full - Startup Script
# This script starts both the POS app and Invoice app

echo "Starting Employee Spring Full Applications..."
echo "=============================================="

# Function to check if port is available
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
        echo "Port $1 is already in use. Please stop the application using port $1 first."
        return 1
    fi
    return 0
}

# Check if ports are available
echo "Checking port availability..."
if ! check_port 9001; then
    exit 1
fi
if ! check_port 9000; then
    exit 1
fi

echo "Ports 9000 and 9001 are available."

# Start Invoice App in background
echo "Starting Invoice App on port 9001..."
cd invoice-app
mvn clean install -q
mvn jetty:run > ../invoice-app.log 2>&1 &
INVOICE_PID=$!
cd ..

# Wait for invoice app to start
echo "Waiting for Invoice App to start..."
sleep 5

# Check if invoice app is running
echo "Checking if Invoice App is responding..."
for i in {1..30}; do
    if curl -s http://localhost:9001/invoice/health > /dev/null 2>&1; then
        echo "Invoice App started successfully on port 9001"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "Failed to start Invoice App. Check invoice-app.log for details."
        kill $INVOICE_PID 2>/dev/null
        exit 1
    fi
    echo "Waiting for Invoice App to start... (attempt $i/30)"
    sleep 2
done

# Start POS App in background
echo "Starting POS App on port 9000..."
cd pos
mvn clean install -q
mvn jetty:run > ../pos-app.log 2>&1 &
POS_PID=$!
cd ..

# Wait for POS app to start
echo "Waiting for POS App to start..."
sleep 10

# Check if POS app is running
echo "Checking if POS App is responding..."
for i in {1..30}; do
    if curl -s http://localhost:9000/orders > /dev/null 2>&1; then
        echo "POS App started successfully on port 9000"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "Failed to start POS App. Check pos-app.log for details."
        kill $POS_PID 2>/dev/null
        kill $INVOICE_PID 2>/dev/null
        exit 1
    fi
    echo "Waiting for POS App to start... (attempt $i/30)"
    sleep 2
done

echo ""
echo "=============================================="
echo "Both applications started successfully!"
echo ""
echo "POS App: http://localhost:9000"
echo "Invoice App: http://localhost:9001"
echo ""
echo "API Documentation:"
echo "- POS App Swagger: http://localhost:9000/swagger-ui.html"
echo "- Invoice App Health: http://localhost:9001/invoice/health"
echo ""
echo "Logs:"
echo "- POS App: pos-app.log"
echo "- Invoice App: invoice-app.log"
echo ""
echo "To stop applications, run: ./stop-apps.sh"
echo "=============================================="

# Save PIDs for stopping later
echo $INVOICE_PID > .invoice-pid
echo $POS_PID > .pos-pid 