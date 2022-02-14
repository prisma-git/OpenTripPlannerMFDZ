#! /bin/bash -e

GRAPH_DIR=/opt/opentripplanner/graph/
GRAPH_OUTPUT_FILE=${GRAPH_DIR}graph.obj
ZIP_OUTPUT_FILE=${GRAPH_DIR}graph.zip

if [[ ! -z "${GRAPH_ZIP_URL}" ]]; then
  if [[ ! -f "$GRAPH_OUTPUT_FILE" ]]; then
    curl --fail-with-body --create-dirs ${GRAPH_ZIP_URL} -o ${ZIP_OUTPUT_FILE}
    unzip ${ZIP_OUTPUT_FILE} -d ${GRAPH_DIR}
    set -x
    java $JAVA_OPTS -jar otp-shaded.jar --load --serve ${GRAPH_DIR}
  else
    echo "Graph file ${GRAPH_OUTPUT_FILE} already exists in container. Not downloading from ${GRAPH_ZIP_URL}"
    exit 1
  fi
else
  echo "Environment variable GRAPH_ZIP_URL not set. Starting in manual mode."
  set -x
  java $JAVA_OPTS -Djava.awt.headless=true -server --add-exports java.desktop/sun.font=ALL-UNNAMED -jar otp-shaded.jar $@
fi

