#!/bin/bash

echo_message() {
    echo "[RUN_SERVERS] $1"
}

# Read server URLs from urls.txt
declare -a urls=($(cat server_urls.txt))

start_server() {
    local url="$1"
    local log_level="$2"
    local ip_address=$(echo "$url" | awk -F[/:] '{print $4}')
    local port=$(echo "$url" | awk -F[/:] '{print $5}')
    java -Dlog.level="$log_level" -cp "build/server:lib/*" com.server.Main "$ip_address" "$port" &
}

# Function to gracefully shutdown the server
shutdown_server() {
    local port="$1"
    local pid=$(lsof -ti :"$port")
    if [ -n "$pid" ]; then
        echo_message "Server on port $port shutting down..."
        kill "$pid"
    else
        echo_message "No server found running on port $port."
    fi
}

# Trap Ctrl+C signal and call shutdown_server function
trap 'shutdown_servers' SIGINT

# Function to shutdown all servers
shutdown_servers() {
    for url in "${urls[@]}"; do
        local port=$(echo "$url" | awk -F[/:] '{print $5}')
        shutdown_server "$port"
    done
    wait_for_shutdown
}

# Function to wait for servers to fully shutdown
wait_for_shutdown() {
    while true; do
        local running_servers=$(pgrep -f "java -Dlog.level=INFO -cp build/server:lib/* com.server.Main" | wc -l)
        if [ "$running_servers" -eq 0 ]; then
            print_exit_message
            break
        else
            echo_message "Waiting for servers to shutdown..."
            sleep 1
        fi
    done
}

# Function to print exit message
print_exit_message() {
    echo_message "Exiting upon the shutdown of all servers."
    exit 0
}

# Start servers
for url in "${urls[@]}"; do
    start_server "$url" "INFO"
done

# Loop indefinitely to keep the script running
while true; do
    sleep 1
done
