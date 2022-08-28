#!/bin/bash

function getBinary() {
    echo $(whereis $1 | cut -d ':' -f 2 | tr -d '[:space:]')
}

intelij=$(getBinary intellij-idea-ultimate-edition)
[ -z "$intelij" ] && {
    intelij=$(getBinary intellij-idea-community-edition)
}
[ -z "$intelij" ] && {
    intelij=$(getBinary android-studio)
}

[ -z "$intelij" ] && {
    echo "Couldn't format files, IntelliJ binary is missing"
    exit 1
}

$intelij format -s ".idea/codeStyles/Project.xml" -m "${PLUGIN_FILE_PATTERN:-"*"}" -r .
