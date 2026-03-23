from .mcu_socket import mcu_manager


class MCUService:
    async def send_message(self, message: str) -> dict:
        if not mcu_manager.connected:
            return {"error": "No MCU connected"}

        try:
            response = await mcu_manager.send(message)
            return {
                "device_id": mcu_manager.device_id,
                "sent": message,
                "response": response,
            }
        except RuntimeError as e:
            return {"error": str(e)}
        except TimeoutError:
            return {"error": "MCU did not respond in time"}

    async def get_status(self) -> dict:
        return {"connected": mcu_manager.connected, "device_id": mcu_manager.device_id}


mcu_service = MCUService()
