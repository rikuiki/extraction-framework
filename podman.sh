podman build -t dbpedia-extractor-image -f ./Dockerfile . --root /usr/proj/temp/podman/storage \
  --runroot /usr/proj/temp/podman/runtime


--rootfs /usr/proj/data/podman/mount/dbpedia-extractor

podman run --root /usr/proj/temp/podman/storage --runroot /usr/proj/temp/podman/runtime  -it \
  -v /usr/proj/data/podman/mount/dbpedia-extractor:/ dbpedia-extractor /bin/bash