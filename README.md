# This is LocalInference

## Set up:

In `backend/`, create a `.env` file with the following parameters:
```
DB_URL=postgresql+asyncpg://postgres:party@li-pgsql-db:5432/postgres
JWT_SECRET=f6c00a8672387df665c7dfb8de17ecd7
JWT_ALGORITHM=HS256
```

Next, in `frontend/`, create a `.env.dev` file with the following parameters:
```
VITE_API_BASE_URL=http://127.0.0.1:8000
```

## NOTE:
Must have docker installed first.

# Building production Docker image 
in .env, set:
`PROFILE=prod`
then run:
`docker compose -f compose.yaml -f compose.prod.yaml up -d`

# Running dev container
<!-- TODO! -->
Ensure that in `.env` PROFILE=`dev` then run
`docker compose up` 
    `-d` (if you want to run detached)

### Socket demo
Implements an API to connect to MCU, opens socket for communication and provides a sample client file (requires manual execution of client).

The backend will automatically spin up a 24/7 socket, accepting limited number of MCU devices during the server's lifetime. (restarting server will enable a new connection, working on a more robust solution)


# Todo

Manually register MCU 
    -> user registers MAC address (UUID) [as whitelist]

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