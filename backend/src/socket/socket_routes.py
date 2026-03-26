from typing import Optional, Union, Annotated, List
from fastapi import FastAPI, Header, APIRouter, Depends
from fastapi import status
from fastapi.responses import JSONResponse
from fastapi.exceptions import HTTPException
import socket
import asyncio
import src.socket.mcu_socket as sock
import src.socket.service as sock_serv

socket_router = APIRouter()


@socket_router.get("/status")
async def get_status():
    if not sock.recent_peer:
        return "No peer found"
    
    return f'last connected to: {sock.recent_peer}'


@socket_router.post("/send")
async def send_to_mcu(message: str):
    if not sock.recent_peer:
        return "No peer found"
    
    return await sock_serv.send_message(message)