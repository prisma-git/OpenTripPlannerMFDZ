#! /bin/bash -e

GRAPH_DIR=/opt/opentripplanner/graph/
GRAPH_OUTPUT_FILE=${GRAPH_DIR}graph.obj
ZIP_OUTPUT_FILE=${GRAPH_DIR}graph.zip

if [[ ! -z "${GRAPH_ZIP_URL}" ]]; then
  if [[ ! -f "$GRAPH_OUTPUT_FILE" ]]; then
    curl --fail-with-body --create-dirs ${GRAPH_ZIP_URL} -o ${ZIP_OUTPUT_FILE}
    unzip ${ZIP_OUTPUT_FILE} -d ${GRAPH_DIR}
  else
    echo "Graph file ${GRAPH_OUTPUT_FILE} already exists in container. Not downloading from ${GRAPH_ZIP_URL}"
    exit 1
  fi
fi

java $JAVA_OPTS -jar otp-shaded.jar $@
