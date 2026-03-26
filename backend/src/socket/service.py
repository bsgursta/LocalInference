import asyncio
import json
import src.socket.mcu_socket as sock
SERVER_HOST = "127.0.0.1"
SERVER_PORT = 9999

async def send_message(message: str) -> str:
    try:
        if not sock.recent_peer_writer:
            return "failed to send msg"
            
        sock.recent_peer_writer.write(message.encode())
        await sock.recent_peer_writer.drain()
        return "success"

        
    except RuntimeError as e:
        return {"error": str(e)}
    except TimeoutError:
        return {"error": "MCU did not respond in time"}

async def get_status(self):
    return {"connected": mcu_manager.connected, "device_id": mcu_manager.device_id}


