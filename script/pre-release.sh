#!/usr/bin/env bash

[ -z "$1" ] && echo 'Error: pass the new version' && exit 1

CURRENT_VERSION=$(< build.gradle grep "Last released version" | grep -o -e '\d\.\d\.\d')


sed -i '' -e "s/$CURRENT_VERSION/$1/g" README.md
sed -i '' -e "s/$CURRENT_VERSION/$1/g" examples/popular-page/build.gradle
sed -i '' -e "s/$CURRENT_VERSION/$1/g" examples/popular-page/pom.xml
sed -i '' -e "s/$CURRENT_VERSION/$1/g" examples/standalone-server/build.gradle

echo "Don't forget to create release notes. This is what has been done up till now"

git log --pretty=oneline --abbrev-commit "${CURRENT_VERSION}"...HEAD