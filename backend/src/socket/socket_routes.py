from typing import Optional, Union, Annotated, List
from fastapi import FastAPI, Header, APIRouter, Depends
from fastapi import status
from fastapi.responses import JSONResponse
from fastapi.exceptions import HTTPException
import socket
import asyncio

from .service import mcu_service

socket_router = APIRouter()


@socket_router.get("/status")
async def get_status():
    return await mcu_service.get_status()


@socket_router.post("/send")
async def send_to_mcu(message: str):
    return await mcu_service.send_message(message)