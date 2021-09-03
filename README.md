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
    -e GRAPH_ZIP_URL=http://example.com/graph.zip \
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
