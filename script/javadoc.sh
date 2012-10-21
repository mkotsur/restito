#!/bin/bash

PAGES_BRANCH="gh-pages"
PAGES_JAVADOC_PATH="javadoc/current"
BUILD_JAVADOC_PATH="build/docs/javadoc"

gradle javadoc
if [[ ! -d "${BUILD_JAVADOC_PATH}" ]]; then echo "Could not find generated javadoc."; exit 30; fi


if [[ ! -d "./src" ]]; then echo "Should be started from the project root."; exit 10; fi
if [[ ! `git branch | grep "${PAGES_BRANCH}"` ]]; then echo "  --> Branch ${PAGES_BRANCH} does not exist."; exit 20; fi

initial_branch="`git rev-parse --symbolic-full-name --abbrev-ref HEAD`"
echo "  --> You are on branch: ${initial_branch}. Checking out ${PAGES_BRANCH}."
git co "${PAGES_BRANCH}"

if [[ $? ]]; then echo "  --> Previous command failed. Bye-Bye."; exit $?; fi

git rm -rf "${PAGES_JAVADOC_PATH}"
cp -r "${BUILD_JAVADOC_PATH}" "${PAGES_JAVADOC_PATH}"

if [[ $? ]]; then echo "  --> Previous command failed. Bye-Bye."; exit $?; fi

git add -A
git ci -am "New javadoc for git pages"
git push origin ${PAGES_BRANCH}

git co ${initial_branch}

echo "  [DONE]  :-)"



