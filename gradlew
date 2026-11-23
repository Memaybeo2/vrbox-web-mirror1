#!/bin/sh
set -e

APP_HOME=$(cd "$(dirname "$0")" && pwd)
GRADLE_VERSION="8.9"
DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/gradle-${GRADLE_VERSION}-bin"

mkdir -p "$DIST_DIR"
ZIP_FILE="$DIST_DIR/gradle-${GRADLE_VERSION}-bin.zip"
UNPACK_DIR="$DIST_DIR/unpacked"

if [ ! -d "$UNPACK_DIR/gradle-${GRADLE_VERSION}" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    curl -fsSL "$DIST_URL" -o "$ZIP_FILE"
    mkdir -p "$UNPACK_DIR"
    unzip -q "$ZIP_FILE" -d "$UNPACK_DIR"
fi

exec "$UNPACK_DIR/gradle-${GRADLE_VERSION}/bin/gradle" "$@"
