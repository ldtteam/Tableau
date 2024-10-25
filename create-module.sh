#!/bin/bash

# Function to convert kebab-case to CamelCase
kebab_to_camel() {
  local input=$1
  local output=""

  # Split the input by hyphens and capitalize each part
  IFS='-' read -ra parts <<< "$input"
  for part in "${parts[@]}"; do
    output+=$(tr '[:lower:]' '[:upper:]' <<< ${part:0:1})${part:1}
  done

  echo "$output"
}

replace_hyphens_with_dots() {
  local input=$1
  local output=${input//-/.}
  echo "$output"
}

replace_hyphens_with_slashes() {
  local input=$1
  local output=$(echo "$input" | sed 's/-/\//g')
  echo "$output"
}

rename_files() {
  local dir=$1
  local old_name=$2
  local new_name=$3

  find "$dir" -type f -name "*$old_name*" | while read -r file; do
    new_file=$(echo "$file" | sed "s/$old_name/$new_name/g")
    mv "$file" "$new_file"
  done
}

rename_directories() {
  local dir=$1
  local old_name=$2
  local new_name=$3

  find "$dir" -type d -name "$old_name" | while read -r directory; do
    new_directory=$(echo "$directory" | sed "s|$old_name|$new_name|g")
    mkdir -p "$(dirname "$new_directory")"
    mv "$directory" "$new_directory"
  done
}

# Check if the module name is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <new-module-name>"
  exit 1
fi

NEW_MODULE_NAME=$1
NEW_MODULE_NAME_LOWERCASE=${NEW_MODULE_NAME,,}
NEW_MODULE_CLASS_NAME=$(kebab_to_camel "$NEW_MODULE_NAME_LOWERCASE")
NEW_MODULE_PACKAGE_TREE=$(replace_hyphens_with_dots "$NEW_MODULE_NAME_LOWERCASE")
NEW_MODULE_PACKAGE_PATH=$(replace_hyphens_with_slashes "$NEW_MODULE_NAME_LOWERCASE")

TEMPLATE_DIR="module-template"
NEW_MODULE_DIR="modules/$NEW_MODULE_NAME"

# Check if the template directory exists
if [ ! -d "$TEMPLATE_DIR" ]; then
  echo "Template directory '$TEMPLATE_DIR' does not exist."
  exit 1
fi

# Copy the template directory to the new module directory
cp -r "$TEMPLATE_DIR" "$NEW_MODULE_DIR"

# Rename files containing "ModuleTemplate"
rename_files "$NEW_MODULE_DIR" "ModuleTemplate" "$NEW_MODULE_CLASS_NAME"

# Rename directories containing "module-template-packagetree"
rename_directories "$NEW_MODULE_DIR" "module-template-packagetree" "$NEW_MODULE_PACKAGE_PATH"

# Interpolate the new module name in the copied files
find "$NEW_MODULE_DIR" -type f -exec sed -i "s/ModuleTemplate/$NEW_MODULE_CLASS_NAME/g" {} +
find "$NEW_MODULE_DIR" -type f -exec sed -i "s/module-template-lowercase/$NEW_MODULE_NAME_LOWERCASE/g" {} +
find "$NEW_MODULE_DIR" -type f -exec sed -i "s/module-template-packagetree/$NEW_MODULE_PACKAGE_TREE/g" {} +
find "$NEW_MODULE_DIR" -type f -exec sed -i "s/module-template/$NEW_MODULE_NAME/g" {} +

# Register the new module in settings.gradle
echo "include('$NEW_MODULE_NAME')" >> settings.gradle
echo "project(':$NEW_MODULE_NAME').projectDir = file('$NEW_MODULE_DIR')" >> settings.gradle

# Inject the new project as an api dependency in the root project's build.gradle
sed -i "/dependencies {/a \    api(project(':$NEW_MODULE_NAME'))" build.gradle

# Add the new module directory to Git
git add "$NEW_MODULE_DIR"

echo "New module '$NEW_MODULE_NAME' created successfully."