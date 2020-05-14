#!/usr/bin/env bash

[ -z "${NC_TOKEN}" ] && { echo "Nextcloud token missing."; exit 1; }

# Variables

nc_user="GitNexBot"
nc_password=$NC_TOKEN
nc_url="https://cloud.swatian.com/remote.php/dav/files/GitNexBot/GitNex-Builds/latest.apk"

upload_filename="signed.apk"
upload_timeout=300
upload_retries=3

# Main uploading logic

upload_repeated=0

upload_file() {

  curl --upload-file $upload_filename --user $nc_user:$nc_password $nc_url --progress-bar --max-time $upload_timeout

}

while (($? == 0)) && (($upload_repeated < $upload_retries)); do

  upload_repeated=$(($upload_repeated + 1))
  echo "Upload pass $upload_repeated"
  upload_file

done

if (($? != 0)); then

  echo "Upload has failed."
  exit 1

fi

exit 0