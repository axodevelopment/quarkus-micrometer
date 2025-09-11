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
echo -e "${BLUE}  Test Deploy Module Application${NC}"
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

        # 2. Build app
        #quarkus build --no-tests -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true


        # 3. Deploy app
        #quarkus deploy openshift

        # if you need to clear out old is, bc from another s2i
        #oc delete deployment micrometer-module 2>/dev/null || true
        #oc delete service micrometer-module 2>/dev/null || true
        #oc delete route micrometer-module 2>/dev/null || true
        #oc delete bc micrometer-module-build-user1 2>/dev/null || true
        #oc delete is micrometer-module 2>/dev/null || true

        # 2. Try mvn commands....
        # Check appsettings for quarkus container annotations and select the right mvn package
        #mvn clean package -DskipTests -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true -Dquarkus.kubernetes.deploy=true -Dquarkus.openshift.annotations.\"sidecar.opentelemetry.io/inject\"=sidecar

        # should work on clean environment
        WS_USERID=${WS_USERID} mvn clean package -DskipTests

    else
        print_status "WARNING" "Current project (${CURRENT_PROJECT}) does not match WS_PROJECT (${WS_PROJECT})"
        print_status "INFO" "You can switch with: oc project ${WS_PROJECT}"
    fi
fi

echo ""


