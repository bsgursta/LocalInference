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

Then have to individually execute `docker compose up li-pgsql-db` to run the database first then `docker compose up li-server-py` to initialize database with placeholder data, and `docker compose up li-frontend-tsx` whenever.

When you are done with the docker containers, you can run `docker compose down` to turn the containers off.

After doing the above, the next time you run the setup, you can just quickly run `docker compose up` and all should be well.


### Socket demo
Implements an API to connect to MCU, opens socket for communication and provides a sample client file (requires manual execution of client).

The backend will automatically spin up a 24/7 socket, accepting only 1 MCU device during the current server's lifetime. (restarting server will enable a new connection, working on a more robust solution)

A simple API to test future implementation of sending custom data to a client (MCU)

`backend/src/sample_socket`

a simple socket demo (as simple as it gets) though it is a one-way from client->server rather than other way around. Client sends a custom text message and server replies back w/ `heard {msg}`