#!/bin/bash

probe_intelij_binarys=(
    "intellij-idea-ultimate-edition"
    "intellij-idea-community-edition"
    "android-studio"
    "intellij"
    "intelli"
)

function getBinary() {
    echo $(whereis $1 | cut -d ':' -f 2 | tr -d '[:space:]')
}

intelij=
for bin in $probe_intelij_binarys; do
    intelij=$(getBinary $bin)
    [ -n "$intelij" ] && break
done

[ -z "$intelij" ] && {
    echo "Couldn't format files, IntelliJ binary is missing"
    exit 1
}

$intelij format -s ".idea/codeStyles/Project.xml" -m "${PLUGIN_FILE_PATTERN:-"*"}" -r .
