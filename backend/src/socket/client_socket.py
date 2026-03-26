import asyncio
import json

SERVER_HOST = "127.0.0.1"
SERVER_PORT = 9999

# Simulate chatter using:
# curl -X POST "http://localhost:8000/mcu/send?message={MESSAGE_HERE}"
# API_MSG -> SERVER -> MSG -> CLIENT

# Only 1 device can connect
async def read_loop(reader: asyncio.StreamReader, queue: asyncio.Queue):
    """Permanently reads from server, never cancelled"""
    while True:
        data = await reader.read(1024)
        if not data:
            await queue.put(None)   # signal disconnect
            break
        await queue.put(("server", data.decode().strip()))

async def input_loop(queue: asyncio.Queue):
    """Permanently reads user input in a thread, never cancelled"""
    loop = asyncio.get_event_loop()
    while True:
        text = await loop.run_in_executor(None, input, ">> ")
        await queue.put(("user", text))

async def simulate_mcu():
    reader, writer = await asyncio.open_connection(SERVER_HOST, SERVER_PORT)
    print("Connected to server")

    # register
    registration = json.dumps({"type": "register", "device_id": "ID-002"})
    writer.write(registration.encode())
    await writer.drain()

    ack = await reader.read(1024)
    print(f"Server: {ack.decode()}\n")

    queue: asyncio.Queue = asyncio.Queue()

    # both run forever in background, neither gets cancelled
    asyncio.create_task(read_loop(reader, queue))
    asyncio.create_task(input_loop(queue))

    print("Type to send, empty line to quit\n")

    try:
        while True:
            item = await queue.get()

            if item is None:
                print("Server disconnected, closing conn")
                raise Exception("closing loop")
                break

            source, data = item

            if source == "server":
                print(f"\nServer sent: '{data}'\n")

            elif source == "user":
                if not data.strip():
                    print("Closing connection...")
                    break
                writer.write(data.encode())
                await writer.drain()
    finally:
        writer.close()
        await writer.wait_closed()
        print("Connection closed")
        return

asyncio.run(simulate_mcu())