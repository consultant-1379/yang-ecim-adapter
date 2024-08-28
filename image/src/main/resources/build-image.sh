#!/bin/bash

echo "Building yang-ecim-adapter OpenStack qcow2 image."

IMAGE_ARTIFACT=ubuntu-16.04-server-cloudimg-amd64-root
IMAGE_URL="${nexus.url}/service/local/artifact/maven/redirect?r=3pptools&g=com.ubuntu.cloud-images&a=${IMAGE_ARTIFACT}&p=tgz&v=RELEASE"
IMAGE_CACHE=$HOME/.cache/image-create
export BASE_IMAGE_FILE=${IMAGE_ARTIFACT}.tar.gz

image_url_redirect=$(curl -s ${IMAGE_URL} | cut -d: -f2-)
sha_remote=$(curl -s -L ${image_url_redirect}.sha1)

function download_image {
    echo "Downloading ${IMAGE_URL}/${BASE_IMAGE_FILE}"
    curl -L ${IMAGE_URL} -o ${BASE_IMAGE_FILE}
    mkdir -p ${IMAGE_CACHE}
    mv ${BASE_IMAGE_FILE} ${IMAGE_CACHE}
}

if [ -f ${IMAGE_CACHE}/${BASE_IMAGE_FILE} ]; then
    sha_local=$(sha1sum ${IMAGE_CACHE}/${BASE_IMAGE_FILE} | cut -d' ' -f1)

    if [ "${sha_remote}" = "${sha_local}" ]; then
        echo "Using existing cached file ${IMAGE_CACHE}/${BASE_IMAGE_FILE}"
    else
        download_image
    fi
else
    download_image
fi

export DIB_CLOUD_INIT_DATASOURCES=OpenStack
export DIB_OFFLINE=1
export ELEMENTS_PATH=$PWD

echo "BASE_IMAGE_FILE: ${BASE_IMAGE_FILE}"
echo "DIB_CLOUD_INIT_DATASOURCES: ${DIB_CLOUD_INIT_DATASOURCES}"
echo "ELEMENTS_PATH: ${ELEMENTS_PATH}"

set -x

disk-image-create --no-tmpfs -a amd64 -o yang-ecim-adapter-image-${project.version} -p openjdk-8-jdk vm ubuntu yea-element dhcp-all-interfaces --logfile disk-image-create.log

