import logging
logging.basicConfig(
    level=logging.INFO,
    format="%(levelname)s:     %(name)s - %(message)s"
)
import asyncio
from typing import Optional, Union, Annotated
from fastapi import FastAPI, Header
from fastapi.middleware.cors import CORSMiddleware
from src.auth.auth_routes import auth_router
from src.config import Settings
from src.db.main import init_db

from src.root_routes import root_router
from src.socket.socket_routes import socket_router
from contextlib import asynccontextmanager
from src.socket.mcu_socket import mcu_manager

@asynccontextmanager
async def life_span(app: FastAPI):
    print("Server is starting...")
    asyncio.create_task(mcu_manager.start())

    yield
    print("Server has been stopped...")
    await mcu_manager.stop()

app = FastAPI(
    title="Portfolio",
    description="My portfolio & community for friends",
    lifespan=life_span,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
app.include_router(router=auth_router, prefix="/auth")
app.include_router(router=root_router, prefix="")
app.include_router(router=socket_router, prefix="/mcu")

