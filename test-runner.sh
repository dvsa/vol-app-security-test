#!/bin/bash

set -e

echo "=== VOL Security Test Runner ==="
echo "Platform Environment: ${PLATFORM_ENV:-unknown}"
echo "Test Suite: ${TEST_SUITE:-all}"
echo "Target URL: ${TARGET_URL:-unknown}"
echo "Build ID: ${BUILD_ID:-unknown}"

# Validate required parameters
if [ -z "${TARGET_URL}" ]; then
    echo "ERROR: TARGET_URL is required"
    exit 1
fi

# Set Maven test properties
MAVEN_OPTS="-DtargetUrl=${TARGET_URL}"

# Add test suite filter if specified
if [ -n "${TEST_SUITE}" ] && [ "${TEST_SUITE}" != "all" ]; then
    MAVEN_OPTS="${MAVEN_OPTS} -Dtest=${TEST_SUITE}"
fi

# Add Maven options if provided
if [ -n "${MAVEN_OPTIONS}" ]; then
    MAVEN_OPTS="${MAVEN_OPTS} ${MAVEN_OPTIONS}"
fi

echo "Maven Options: ${MAVEN_OPTS}"

# Run the security tests
echo "Starting security tests..."
mvn test \
    -P github \
    ${MAVEN_OPTS}

# Check if test completed successfully
if [ $? -eq 0 ]; then
    echo "Security tests completed successfully"
else
    echo "Security tests failed"
    exit 1
fi

# Prepare results directory
mkdir -p security-results
if [ -d "target/surefire-reports" ]; then
    echo "Copying test results..."
    cp -r target/surefire-reports/* security-results/ || echo "Warning: Could not copy all test results"
fi

# Copy ZAP reports if they exist
if [ -d "target/zap-reports" ]; then
    echo "Copying ZAP security reports..."
    cp -r target/zap-reports/* security-results/ || echo "Warning: Could not copy ZAP reports"
fi

# Upload results to S3 if configured
if [ -n "${RESULTS_TARGET_BUCKET}" ] && [ -n "${BUILD_ID}" ]; then
    echo "Uploading results to S3..."

    # Create results archive
    if [ -d "security-results" ] && [ "$(ls -A security-results)" ]; then
        zip -r "security_results_${RESULTS_BUILD_NUMBER:-1}.zip" security-results/

        # Upload to S3
        aws s3 cp "security_results_${RESULTS_BUILD_NUMBER:-1}.zip" \
            "s3://${RESULTS_TARGET_BUCKET}/${RESULTS_TARGET_BUCKET_PATH}/${BUILD_ID}/security_results_${RESULTS_BUILD_NUMBER:-1}.zip"

        echo "Results uploaded successfully"
    else
        echo "Warning: No results found to upload"
    fi
else
    echo "No S3 configuration found, skipping upload"
fi

echo "=== Security Test Completed ==="