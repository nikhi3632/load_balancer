IMAGE_NAME := loadbalancer
IMAGE_TAG := latest
PROJECT_DIR := $(CURDIR)
DOCKERFILE := Dockerfile
CONTAINER_APP:= home

docker-build:
	docker build --no-cache -t $(IMAGE_NAME):$(IMAGE_TAG) -f $(DOCKERFILE) .

docker-run:
	docker run -it -v $(PROJECT_DIR):/$(CONTAINER_APP) -w /$(CONTAINER_APP) $(IMAGE_NAME):$(IMAGE_TAG) /bin/bash

docker-clean:
	@echo "Remove the Docker container...if it's running"
	docker ps -aq --filter "ancestor=$(IMAGE_NAME):$(IMAGE_TAG)" | xargs -r docker rm -f

	@echo "Delete the Docker image locally...without affecting remote repositories"
	docker rmi -f $(IMAGE_NAME):$(IMAGE_TAG)