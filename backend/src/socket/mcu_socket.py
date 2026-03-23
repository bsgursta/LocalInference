import asyncio
import json
import logging

logger = logging.getLogger(__name__)

HOST = "0.0.0.0"
PORT = 9999

# One unique MCU connection per instance
class MCUManager:
    def __init__(self):
        self.device_id: str | None = None
        self.reader: asyncio.StreamReader | None = None
        self.writer: asyncio.StreamWriter | None = None
        self.lock = asyncio.Lock()
        self.connected = False
        self._server = None

    async def start(self):
        self._server = await asyncio.start_server(self._handle_connection, HOST, PORT)
        logger.info(f"MCU socket server listening on {HOST}:{PORT}")
        async with self._server:
            await self._server.serve_forever()

    async def stop(self):
        if self.writer:
            self.writer.close()
            await self.writer.wait_closed()
        if self._server:
            self._server.close()

    async def _handle_connection(
        self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter
    ):
        addr = writer.get_extra_info("peername")

        # reject if an MCU is already connected
        if self.connected:
            logger.warning(f"Second MCU attempted connection from {addr}, rejected")
            writer.close()
            return

        logger.info(f"MCU attempting connection from {addr}")

        try:
            # first message must be a registration payload
            raw = await asyncio.wait_for(reader.read(1024), timeout=10.0)
            data = json.loads(raw.decode())

            if data.get("type") != "register":
                logger.warning("First message was not a registration, closing")
                writer.close()
                return

            self.device_id = data["device_id"]
            self.reader = reader
            self.writer = writer
            self.connected = True

            logger.info(f"MCU registered: device_id={self.device_id} addr={addr}")

            # send acknowledgement back to MCU
            writer.write(json.dumps({"type": "ack", "status": "registered"}).encode())
            await writer.drain()

            # keep connection alive — wait until MCU disconnects
            await self._watch_connection()

        except asyncio.TimeoutError:
            logger.warning("MCU registration timed out")
            writer.close()
        except Exception as e:
            logger.error(f"MCU connection error: {e}")
        finally:
            self._clear_connection()
            logger.info("MCU disconnected, ready for new connection")

    async def _watch_connection(self):
        """Detects disconnect without reading. send() owns the reader"""
        try:
        # wait until the writer is closed (MCU disconnected)
            await self.writer.wait_closed()
        except Exception as e:
            logger.warning(f"Connection watch error: {e}")

    def _clear_connection(self):
        self.connected = False
        self.device_id = None
        self.reader = None
        self.writer = None

    async def send(self, message: str) -> str:
        """Send a message to the MCU and wait for its response"""
        async with self.lock:
            if not self.connected or not self.writer:
                raise RuntimeError("No MCU connected")

            self.writer.write(message.encode())
            await self.writer.drain()
            
            # give MCU time to process
            response = await asyncio.wait_for(
                self.reader.read(1024), timeout=10.0  
            )
            print(f'response from client: {response.decode()}')
            return response.decode()


# single shared instance
mcu_manager = MCUManager()
