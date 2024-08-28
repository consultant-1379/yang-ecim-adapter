#!/bin/bash

echo "Deploying qcow2 image to ATH OpenStack test environment"

pre_name=yang-ecim-adapter
stack_name=${pre_name}-stack
image_name=${pre_name}-image-${project.version}
vm_name=${pre_name}-${project.version}-vm
floating_ip=131.160.203.149

# Remove any existing stacks
openstack stack list --short
existing_stack=$(openstack stack list --short | grep ${stack_name})

if [ $? -eq 0 ]; then
    stack_id=$(echo ${existing_stack} | cut -d'|' -f2)
    echo Removing existing stack ID ${stack_id}
    openstack stack delete -y --wait ${stack_name}
else
    echo No existing stack named ${stack_name} detected.
fi

# Remove existing image
openstack image list
existing_image=$(openstack image list | grep ${image_name})

if [ $? -eq 0 ]; then
    image_id=$(echo ${existing_image} | cut -d'|' -f2)
    echo Removing existing image ID ${image_id}
    openstack image delete ${image_name}
    sleep 10
else
    echo No existing image named ${image_name} detected.
fi

echo "Creating image ${image_name} in openstack"
openstack image create --public \
    --file ${project.build.directory}/${image_name}.qcow2 \
    ${image_name}

sleep 10

echo "Creating stack ${stack_name} in openstack"
openstack stack create --wait --timeout 1 ${stack_name} \
    -t ${project.build.directory}/openstack/adapter_heat.yml \
    -e ${project.build.directory}/openstack/adapter_params.env

sleep 3

echo "Assigning floating ip ${floating_ip} to server ${vm_name}"
openstack server add floating ip ${vm_name} ${floating_ip}

echo Complete!

