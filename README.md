## MFDZ's OpenTripPlanner fork

This repo contains the Java code of OTP as well as a Dockerfile that is automatically deployed.

This is a non-exhaustive list of features that this fork contains, but are not in upstream yet:

- bike parking and car parking unified into vehicle parking
- ParkAPI support

### Docker image

#### Automatic graph download
The image is [deployed to Dockerhub](https://hub.docker.com/r/mfdz/opentripplanner).

If you want the container to download a zip file containing the graph (`graph.obj`) and 
the runtime configuration files (`otp-config.json`, `router-config.json`), use the following command:

```
docker pull mfdz/opentripplanner:latest
docker run --rm -it \ 
    -e GRAPH_ZIP_URL=https://example.com/graph.zip \
    -p 8080:8080 \
    mfdz/opentripplanner:latest
```

Then visit https://localhost:8080 to view the debug UI.

#### Manual mode

If you want to use a volume to place the graph and config files, you should use the following command:

```
docker pull mfdz/opentripplanner:latest
docker run --rm -it \ 
    -v /place/on/host/graph/:/opt/opentripplanner/graph/
    -p 8080:8080 \
    mfdz/opentripplanner:latest --load --serve graph
```

#### Logback configuration

OTP comes with a default `logback.xml` configuration file which is bundled inside the Jar. In
order to change this, you need to build a new Jar and Docker image, which is cumbersome.

However, it's also possible to define a different `logback.xml` file by using [standard Logback
functionality](http://logback.qos.ch/manual/configuration.html#configFileProperty).

In order to do this - combined with the automatic download of the graph - follow these steps:

1. Create a file `logback.xml` in the directory where you're running Docker and fill it with the values that you wish.
   A good idea for debugging would be to use a [configuration with colourful log output](https://stackoverflow.com/a/27899234/99022) to ensure that it's working.

2. Run the following command to place the file into the container via a volume and configure the Logback to use it:
```
docker pull mfdz/opentripplanner:latest
docker run --rm -it \
    -v ./logback.xml:/opt/opentripplanner/logback.xml \
    -e JAVA_OPTS="-Dlogback.configurationFile=/opt/opentripplanner/logback.xml" \
    -e GRAPH_ZIP_URL=https://example.com/graph.zip \
    -p 8080:8080 \
    mfdz/opentripplanner:latest
```
3. See that the log output is now using colours.