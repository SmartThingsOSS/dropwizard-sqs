# dropwizard-sqs

A [Dropwizard](https://github.com/SmartThingsOSS/dropwizard-common) module for working with Amazon's Simple Queue Service and Simple Notification Service

[![codecov](https://codecov.io/gh/SmartThingsOSS/dropwizard-sqs/branch/master/graph/badge.svg)](https://codecov.io/gh/SmartThingsOSS/dropwizard-sqs)
[![CircleCI](https://circleci.com/gh/SmartThingsOSS/dropwizard-sqs/tree/master.svg?style=svg)](https://circleci.com/gh/SmartThingsOSS/dropwizard-sqs/tree/master)

## Development

### Running Tests

To execute tests, an instance [GOAWS](https://github.com/p4tin/goaws) is required to be running. This repo bundles 
a Docker compose recipe for that purpose; within the repo root execute:

```
docker-compose up -d
./gradlew check
```
