import asyncio
import json

SERVER_HOST = "127.0.0.1"
SERVER_PORT = 9999

# Simulate chatter using:
# curl -X POST "http://localhost:8000/mcu/send?message={MESSAGE_HERE}"
# API_MSG -> SERVER -> MSG -> CLIENT

# Only 1 device can connect
async def simulate_mcu():
    reader, writer = await asyncio.open_connection(SERVER_HOST, SERVER_PORT)
    print(f"Connected to server")

    # register device to server
    registration = json.dumps({"type": "register", "device_id": "ID-002"})
    writer.write(registration.encode())
    await writer.drain()

    # wait for ack
    ack = await reader.read(1024)
    print(f"Server: {ack.decode()}")

    # Listen for incoming messages from server && reply
    print("Waiting for messages...")
    while True:
        message = await reader.read(1024)
        if not message:
            print("Server closed connection")
            break

        print(f"Server sent: {message.decode()}")

        # simulate a response by repeating what the server sent
        response = json.dumps({"status": "ok", "echo": message.decode()})
        writer.write(response.encode())
        await writer.drain()

    writer.close()
    await writer.wait_closed()

asyncio.run(simulate_mcu())