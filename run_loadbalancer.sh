#!/bin/bash

echo_message() {
    echo "[RUN_LOADBALANCER] $1"
}

LB_IP="127.0.0.1"
LB_PORT="8080"

declare -a urls=($(cat server_urls.txt))

start_loadbalancer() {
    local log_level="$1"
    java -Dlog.level="$log_level" -cp "dist/loadbalancer.jar:lib/*" com.loadbalancer.Main "$LB_IP" "$LB_PORT" "server_urls.txt" &
}

# Function to gracefully shutdown the load balancer
shutdown_loadbalancer() {
    local pid=$(lsof -ti :"$LB_PORT")
    if [ -n "$pid" ]; then
        echo_message "Load balancer on port $LB_PORT shutting down..."
        kill "$pid"
    else
        echo_message "No load balancer found running on port $LB_PORT."
    fi
    wait_for_shutdown
    print_exit_message
}

# Function to wait for load balancer to fully shutdown
wait_for_shutdown() {
    while true; do
        local running_lb=$(pgrep -f "java -Dlog.level=INFO -cp dist/loadbalancer.jar:lib/* com.loadbalancer.Main" | wc -l)
        if [ "$running_lb" -eq 0 ]; then
            break
        else
            echo_message "Waiting for load balancer to shutdown..."
            sleep 1
        fi
    done
}

print_exit_message() {
    echo_message "Exiting upon the shutdown of the load balancer."
    exit 0
}

# Trap Ctrl+C signal and call shutdown_loadbalancer function
trap 'shutdown_loadbalancer' SIGINT

# Start the load balancer
start_loadbalancer "INFO"

# Loop indefinitely to keep the script running
while true; do
    sleep 1
done
