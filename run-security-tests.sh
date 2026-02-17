#!/bin/bash

# VOL Security Test - Boilerplate Runner
# This script provides an easy way to run ZAP security scans locally or in CI/CD

set -e

# Default values
ENV="qa"
BROWSER="firefox-proxy"
ZAP_PORT="8090"
ZAP_PATH="./zap"
CLEAN_BUILD="false"
HELP="false"

# Function to display help
show_help() {
    echo "VOL Security Test Runner"
    echo "======================="
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -e, --env ENV          Target environment (default: qa)"
    echo "  -b, --browser BROWSER  Browser configuration (default: firefox-proxy)"
    echo "  -p, --port PORT        ZAP proxy port (default: 8090)"
    echo "  -z, --zap-path PATH    Path to ZAP installation (default: ./zap)"
    echo "  -c, --clean            Run mvn clean before test"
    echo "  -h, --help             Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                     # Run with default settings"
    echo "  $0 -e qa -b chrome     # Run against QA with Chrome"
    echo "  $0 -c                  # Clean build and run tests"
    echo ""
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--env)
            ENV="$2"
            shift 2
            ;;
        -b|--browser)
            BROWSER="$2"
            shift 2
            ;;
        -p|--port)
            ZAP_PORT="$2"
            shift 2
            ;;
        -z|--zap-path)
            ZAP_PATH="$2"
            shift 2
            ;;
        -c|--clean)
            CLEAN_BUILD="true"
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

echo "=== VOL Security Test Configuration ==="
echo "Environment: ${ENV}"
echo "Browser: ${BROWSER}"
echo "ZAP Port: ${ZAP_PORT}"
echo "ZAP Path: ${ZAP_PATH}"
echo "Clean Build: ${CLEAN_BUILD}"
echo "=================================="

# Function to check if ZAP is running
check_zap_running() {
    if curl -s "http://localhost:${ZAP_PORT}" > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# Function to start ZAP daemon
start_zap() {
    echo "Starting ZAP daemon on port ${ZAP_PORT}..."
    
    # Check if ZAP directory exists
    if [ ! -d "${ZAP_PATH}" ]; then
        echo "ERROR: ZAP installation not found at ${ZAP_PATH}"
        echo "Please download ZAP from https://www.zaproxy.org/download/"
        echo "and unpack it to the project directory"
        exit 1
    fi

    # Find ZAP executable
    ZAP_SCRIPT=$(find "${ZAP_PATH}" -name "zap.sh" -type f | head -1)
    
    if [ -z "${ZAP_SCRIPT}" ]; then
        echo "ERROR: zap.sh not found in ${ZAP_PATH}"
        exit 1
    fi

    # Start ZAP daemon
    cd "$(dirname "${ZAP_SCRIPT}")"
    nohup ./zap.sh -config api.disablekey=true -daemon -port "${ZAP_PORT}" > zap.log 2>&1 &
    ZAP_PID=$!
    cd - > /dev/null

    # Wait for ZAP to start
    echo "Waiting for ZAP to start..."
    for i in {1..30}; do
        if check_zap_running; then
            echo "ZAP is running (PID: ${ZAP_PID})"
            return 0
        fi
        sleep 2
    done

    echo "ERROR: ZAP failed to start within 60 seconds"
    exit 1
}

# Function to stop ZAP
stop_zap() {
    echo "Stopping ZAP..."
    if [ -n "${ZAP_PID}" ]; then
        kill "${ZAP_PID}" 2>/dev/null || true
        wait "${ZAP_PID}" 2>/dev/null || true
    fi
    
    # Also try to kill any remaining ZAP processes
    pkill -f "zap.sh" 2>/dev/null || true
}

# Function to run tests
run_tests() {
    echo "Running security tests..."
    
    MAVEN_CMD="mvn test -Denv=${ENV} -Dbrowser=${BROWSER}"
    
    if [ "${CLEAN_BUILD}" = "true" ]; then
        MAVEN_CMD="mvn clean test -Denv=${ENV} -Dbrowser=${BROWSER}"
    fi
    
    echo "Executing: ${MAVEN_CMD}"
    ${MAVEN_CMD}
}

# Cleanup function
cleanup() {
    echo "Cleaning up..."
    stop_zap
}

# Set up trap to ensure cleanup on exit
trap cleanup EXIT INT TERM

# Main execution
main() {
    # Check if ZAP is already running
    if check_zap_running; then
        echo "ZAP is already running on port ${ZAP_PORT}"
    else
        start_zap
    fi
    
    # Run the security tests
    run_tests
    
    echo "=== Security tests completed successfully ==="
    echo "Reports can be found in: Reports/"
}

# Execute main function
main