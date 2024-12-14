#!/usr/bin/env bash

## This script installs the website modification feature into the dev container
## We need to install Node.JS 20 or above

# Install Node.JS
# Installs nvm (Node Version Manager)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash

# Download and install Node.js (you may need to restart the terminal)
nvm install 22

# Verifies the right Node.js version is in the environment
node -v # should print `v22.12.0`

# Verifies the right npm version is in the environment
npm -v # should print `10.9.0`

# Then install yarn:
npm install -g yarn
