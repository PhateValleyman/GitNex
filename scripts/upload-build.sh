#!/usr/bin/env bash

# Script used to upload builds via WebDAV
# @author opyale

[ -z "${NC_TOKEN}" ] && { echo "Nextcloud token missing."; exit 1; }

# Customizable variables

nc_user="GitNexBot"
nc_password=$NC_TOKEN
nc_url="https://cloud.swatian.com/remote.php/dav/files/GitNexBot/GitNex-Builds/latest.apk"

upload_filename="signed.apk"
upload_timeout=300
upload_retries=3

proxy_enabled=false
proxy_url="socks5://{ip_adress}:{port}"

# Uploading logic

upload_repeated=0

upload_file() {

  if [ $proxy_enabled == true ]; then
    curl --proxy $proxy_url --upload-file $upload_filename --user $nc_user:$nc_password $nc_url --progress-bar --max-time $upload_timeout
  else
    curl --upload-file $upload_filename --user $nc_user:$nc_password $nc_url --progress-bar --max-time $upload_timeout
  fi

}

while [[ ($? == 0) && ($upload_repeated < $upload_retries) ]]; do

  upload_repeated=$(($upload_repeated + 1))
  echo "Upload pass $upload_repeated"
  upload_file

done

if [ $? != 0 ]; then

  echo "Upload has failed."
  exit 1

fi

exit 0