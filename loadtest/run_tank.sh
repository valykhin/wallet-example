docker run \
    -v $(pwd):/var/loadtest \
    -v $SSH_AUTH_SOCK:/ssh-agent -e SSH_AUTH_SOCK=/ssh-agent \
    --net host \
    -m 1g \
    -it \
    --entrypoint /bin/bash \
    yandex/yandex-tank