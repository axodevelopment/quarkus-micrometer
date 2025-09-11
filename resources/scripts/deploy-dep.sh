#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

ERRORS=0
WARNINGS=0

echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}  Deploy Dependencies${NC}"
echo -e "${BLUE}===========================================${NC}"
echo ""

print_status() {
    local status=$1
    local message=$2

    case $status in
        "OK")
            echo -e "[${GREEN}✓${NC}] ${message}"
            ;;
        "ERROR")
            echo -e "[${RED}✗${NC}] ${message}"
            ERRORS=$((ERRORS + 1))
            ;;
        "WARNING")
            echo -e "[${YELLOW}⚠${NC}] ${message}"
            WARNINGS=$((WARNINGS + 1))
            ;;
        "INFO")
            echo -e "[${BLUE}ℹ${NC}] ${message}"
            ;;
    esac
}


# Check requirements

# 1. Check environment variables
echo -e "${YELLOW}1. Checking Environment Variables...${NC}"

if [[ -n "${WS_USERID:-}" ]]; then
    print_status "OK" "WS_USERID is set to: ${WS_USERID}"
else
    print_status "ERROR" "WS_USERID environment variable is not set"
fi

if [[ -n "${WS_PROJECT:-}" ]]; then
    print_status "OK" "WS_PROJECT is set to: ${WS_PROJECT}"
else
    print_status "ERROR" "WS_PROJECT environment variable is not set"
fi

CURRENT_PROJECT=$(oc project -q 2>/dev/null || echo "unknown")
        
print_status "INFO" "Current project: ${CURRENT_PROJECT}"

if [[ -n "${WS_PROJECT:-}" ]]; then
    if [[ "${CURRENT_PROJECT}" == "${WS_PROJECT}" ]]; then
        print_status "OK" "Current project matches WS_PROJECT"

# 1. Create project if it doesn't exist (maybe do it later)

# 2. Deploy OTEL
        oc apply -f ../resources/otel/OpenTelemetryCollector.yaml
        print_status "INFO" "Applied OpenTelemetry Collector configuration"

        echo ""

# 3. Deploy Service / Service Monitor
        
        oc apply -f ../resources/otel/servicemonitor.yaml
        print_status "INFO" "Applied Service configuration"

        echo ""

    else
        print_status "WARNING" "Current project (${CURRENT_PROJECT}) does not match WS_PROJECT (${WS_PROJECT})"
        print_status "INFO" "You can switch with: oc project ${WS_PROJECT}"
    fi
fi

echo ""
