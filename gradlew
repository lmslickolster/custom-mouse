#!/bin/sh
GRADLE_VERSION="8.14.3"
GRADLE_HOME="$HOME/.gradle/wrapper/dists/gradle-$GRADLE_VERSION"
GRADLE_ZIP="/tmp/gradle-$GRADLE_VERSION-bin.zip"
GRADLE_URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  echo "Downloading Gradle $GRADLE_VERSION..."
  mkdir -p "$GRADLE_HOME"
  curl -L "$GRADLE_URL" -o "$GRADLE_ZIP" || exit 1
  unzip -q "$GRADLE_ZIP" -d /tmp/gradle-extract || exit 1
  cp -R "/tmp/gradle-extract/gradle-$GRADLE_VERSION/"* "$GRADLE_HOME/"
  rm -rf /tmp/gradle-extract "$GRADLE_ZIP"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
