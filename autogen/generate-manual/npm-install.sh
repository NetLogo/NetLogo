#!/bin/bash

set -e

if ! command -v npm 2>&1 >/dev/null; then
  echo "You must have npm installed to generate the manual PDF."
  exit 1
fi

npm install
