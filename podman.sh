podman build -t dbpedia-extractor -f ./Dockerfile . --root /usr/proj/temp/podman/storage --runroot /usr/proj/temp/podman/runtime

podman run --root /usr/proj/temp/podman/storage --runroot /usr/proj/temp/podman/runtime -it dbpedia-extractor /bin/sh