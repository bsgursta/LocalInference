# This is LocalInference

## NOTE:
Must have docker installed first.

Edit `.env` file to the correct profile as it dictates docker's run-time behavior: 
- dev
- test
- prod 

Currently only dev & prod are supported.

### Setting up a whitelisted UUID

For prod
```sh
curl -X POST "https://website.com/mcumanager/api/mcu/register" \
        -H "Content-Type: application/json" \
        -d '{
      "uuid": "dd1d2e0c-504e-445c-8e0e-136e392ef4f3"
    }'

```

For dev or use swagger ui `http://127.0.0.1:8080/swagger-ui/index.html#/`
```sh
curl -X POST "http://127.0.0.1/mcu/register" \
        -H "Content-Type: application/json" \
        -d '{
      "uuid": "dd1d2e0c-504e-445c-8e0e-136e392ef4f3"
    }'

```

## Set up for development:
Ensure that in `.env` PROFILE=`dev` then run
`docker compose up` 
    `-d` (if you want to run detached)

Visit http://127.0.0.1:8080/swagger-ui/index.html#/ and make a call to /mcu/registration and add the UUID to whitelist

## Set up for production:
Ensure that in `.env` PROFILE=`prod` then run

`docker compose up` 
    `-d` (if you want to run detached)



### Socket demo
See [this java file](backend/SocketClient.java) to test out 

Implements an API to connect to MCU, opens socket for communication and provides a sample client file (requires manual execution of client).

The backend will automatically spin up a 24/7 socket, accepting limited number of MCU devices during the server's lifetime. 


# Todo



MCU makes a connection to server (socket con, 24/7 no-drop/timeout)
    -> on first registration, MCU stores provided `SECRET_KEY` used to reconnect if something were to change
        -> on reregistration, use stored `SECRET_KEY` to get a new key and rewhitelist self

    -> connect using baked MAC address on first connection 
        On any connection, server returns either:
            1 == OK, followed by extra stream of data afterwards `\n`
            OR
            0 == CONNECTION PREVENTED (nothing else)

            

MCU awaits for instructions from server
    -> while loop to parse instructions




The key idea is:

exactly one component reads the socket
exactly one component writes the socket
everybody else communicates through queues/events

That eliminates almost all race conditions and write conflicts while still allowing either side to initiate communication whenever needed.


One reader
One writer
Many producers
Many consumers
Thread-safe queues


Outbound
+------------------+
| Application      |
+------------------+
          |
          v
+------------------+
| Outbound Queue   |
+------------------+
          |
          v
+------------------+
| Socket Writer    |
+------------------+


===========================
Inbound

+------------------+
| Socket Reader    |
+------------------+
          |
          v
+------------------+
| Dispatcher       |
+------------------+
- Read message
- Validate
- Route
- Continue reading

For inbound traffic, you usually do the opposite: one reader, many handlers.
                    TCP Socket
                         |
                    Reader Thread
                         |
                  Message Decoder
                         |
                    Dispatcher
                  /     |      \
                 /      |       \
                v       v        v
          Cmd Queue  Config   Telemetry
                     Queue     Queue



Socket Reader
     |
     v
 Message Bus
  /   |   \
 A    B    C
  \   |   /
     v
Outbound Queue
     |
Socket Writer