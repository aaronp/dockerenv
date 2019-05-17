#Development

A useful way to add a new service is to copy an existing one, and then just test it out on the command-line.

e.g.

```bash
cd src/main/resources/scripts/orientdb

#
# First, confirm it's NOT running
#
# this should produce no output and return w/ a non-zero exit code
./isOrientDbDockerRunning.sh 

echo $?
1

#
# Now try our start script and confirm it succeeds
#
./startOrientDbDocker.sh

echo $?
0
 
 #
 # isOrientDbDockerRunning should now return the image name
 #
./isOrientDbDockerRunning.sh 
61a2cf3cede0        orientdb:2.2.37     "server.sh"         11 seconds ago      Up 10 seconds       0.0.0.0:2424->2424/tcp, 0.0.0.0:2480->2480/tcp   dockerenv-orientdb
docker image dockerenv-orientdb is running

echo $?
0

```


