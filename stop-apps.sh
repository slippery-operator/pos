#!/bin/bash

# Employee Spring Full - Stop Script
# This script stops both the POS app and Invoice app

echo "Stopping Employee Spring Full Applications..."
echo "=============================================="

# Function to stop application by PID
stop_app() {
    local pid_file=$1
    local app_name=$2
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if kill -0 $pid 2>/dev/null; then
            echo "Stopping $app_name (PID: $pid)..."
            kill $pid
            sleep 5
            if kill -0 $pid 2>/dev/null; then
                echo "Force killing $app_name..."
                kill -9 $pid
            fi
            echo "$app_name stopped successfully."
        else
            echo "$app_name is not running."
        fi
        rm -f "$pid_file"
    else
        echo "$app_name PID file not found."
    fi
}

# Stop Invoice App
stop_app ".invoice-pid" "Invoice App"

# Stop POS App
stop_app ".pos-pid" "POS App"

echo ""
echo "=============================================="
echo "All applications stopped."
echo "==============================================" 