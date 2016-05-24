#!/bin/bash  
#strip parent directory from upload zip file 

 zip="$UPLOAD_PATH/$PHP_APP_ZIP"
 dest="$WEB_ROOT"
 temp=$(mktemp -d) && unzip -d "$temp" "$zip" && mkdir -p "$dest" && shopt -s dotglob && f=("$temp"/*) &&
    if (( ${#f[@]} == 1 )) && [[ -d "${f[0]}" ]] ; then
        mv "$temp"/*/* "$dest"
    else
        mv "$temp"/* "$dest"
    fi && rm -rf "$temp"

rm -rf "$UPLOAD_PATH/$PHP_APP_ZIP"

apachectl -DFOREGROUND
