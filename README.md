### A simple layer seven application load balancer, which routes HTTP requests from clients to a pool of HTTP servers based on round robin strategy.

To setup development environment clone the repository and fire up [Docker Desktop](https://www.docker.com/products/docker-desktop/) and use the following commands:

```bash
cd load_balancer
make docker-build
make docker-run
```

After finishing the development exit Docker and use the following command:

```bash
make docker-clean
```

To build the project using [Ant](https://ant.apache.org/) use the following command:
```bash
ant
```

You will find the build class files in build directory and two jar files server.jar and loadbalancer.jar created in the dist directory.

The scripts and the server_urls.txt are setup in such a way that we have three servers at 127.0.0.1:8081, 127.0.0.1:8082, 127.0.0.1:8083 and the loadbalancer is at 127.0.0.1:8080. These can be modified to suit the requirements if needed.

To see the project in action open three terminals:

In terminal 1:
```bash
chmod +x run_servers.sh
./run_servers.sh
```

In terminal 2:
```bash
chmod +x run_loadbalancer.sh
./run_loadbalancer.sh
```

In terminal 3:
```bash
curl -vL http://127.0.0.1:8080
curl -vL http://127.0.0.1:8080
curl -vL http://127.0.0.1:8083/togglehealth
curl -vL http://127.0.0.1:8080
curl -vL http://127.0.0.1:8080
curl -vL http://127.0.0.1:8083/togglehealth
curl -vL http://127.0.0.1:8080
curl -vL http://127.0.0.1:8080
curl -vL http://127.0.0.1:8080
curl -vL http://127.0.0.1:8080
```

The output logs can be monitored.

## Demo

[Demo Video](demo/demo.mp4)

Kill the run_loadbalancer.sh script first and then the run_server.sh next using Ctrl/Command + C.

Make use of the following command to remove dist and build directories:
```bash
ant clean
```