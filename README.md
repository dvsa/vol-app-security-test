# vol-app-security-scan

# Description
 ZAP Security scan library. This library consists of methods that expose the ZAP JAVA API

# Prerequisites
- Maven
- OWASP ZAP

# Running Zap Scanner

The test will first start ZAP which listens to any incoming traffic on a given port number. Once ZAP starts 
the Selenium test will start on the selenium proxy and listen to any incoming traffic.

## Locally

* Download ZAP from https://www.zaproxy.org/download/
* Unpack folder into project directory
* Navigate to ZAP folder e.g. ``cd /zap/ZAP_X.X.X/``
* Use the following command to start ZAP 
 ``  
     sh zap.sh -config api.disablekey=true -daemon -port 8090
  ``
* Use the following command to start the test `mvn clean test -Denv=qa -Dbrowser=firefox-proxy`

## Pipeline
* Pass in the following arguments -Denv=qa and -Dbrowser=firefox-proxy

## Reports
 - passive and active scans report can be found in the following location Reports/
