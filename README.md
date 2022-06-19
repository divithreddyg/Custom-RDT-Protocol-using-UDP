Build: docker build -t javaapptest .

Create Network: docker network --subnet=172.18.0.0/16 nodenet

Rover Parameters: 
    Rover_ID
    Lander_IP
    Image_File

Start Rover: docker run -it -p 8080:8080 --cap-add=NET_ADMIN --net nodenet --ip 172.18.0.21 javaapptest 1 172.18.0.22 test.png

Lander Parameters:
    Lander_ID
    Lander_IP

Start Lander: docker run -it -p 8080:8080 --cap-add=NET_ADMIN --net nodenet --ip 172.18.0.22 javaapptest 2 172.18.0.22