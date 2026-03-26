import asyncio
import logging

logger = logging.getLogger(__name__)

HOST = "0.0.0.0"
PORT = 9999

recent_peer = None
recent_peer_writer = None

async def handle_connection(reader: asyncio.StreamReader, writer: asyncio.StreamWriter):
    global recent_peer
    global recent_peer_writer
    addr = writer.get_extra_info("peername")
    recent_peer = addr
    recent_peer_writer = writer

    logger.info(f"Connection from {addr}")
    logger.info(recent_peer)

    try:
        while True:
            data = await reader.read(1024)
            if not data:
                logger.info(f"{addr} disconnected")
                writer.close()
                await writer.wait_closed()
                break

            msg = data.decode().strip()
            logger.info(f"Received from {addr}: {msg}")

            response = f"heard {msg} from {addr}\n"
            writer.write(response.encode())
            await writer.drain()

    except Exception as e:
        logger.error(f"Connection error: {e}")
    finally:
        writer.close()
        await writer.wait_closed()

async def start_socket_server():
    server = await asyncio.start_server(handle_connection, HOST, PORT)
    logger.info(f"Socket server listening on {HOST}:{PORT}")
    async with server:
        await server.serve_forever()