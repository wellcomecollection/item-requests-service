# Wiremock

We use [WireMock](http://wiremock.org/) to record external API interactions and replay them in tests.

To re-record:

- Sierra API: `record_sierra.sh`

The Sierra API requires an API key & secret (contact the Collection Platform team for access).

**Important**: Remove any recorded secrets or generated tokens before adding recordings to tests which get committed!