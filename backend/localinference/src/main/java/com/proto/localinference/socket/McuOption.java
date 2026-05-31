package com.proto.localinference.socket;

public enum McuOption {
  /* Server -> MCU: updates the model weights used */
  UPDATE,

  /* MCU -> Server: pings the server about a an *incident* that was triggered */
  NOTIFY,

  /* MCU -> Server: MCU opens a socket connection with server for the first time
  NOTE: REQUIRES MCU UUID TO ALREADY BE REGISTERED AHEAD OF TIME                    */
  REGISTER,

  /* MCU => Server: MCU reopens a new socket connection due to previously closed connection */
  REREGISTER,

  /* MCU <--->  Server: Checks for valid socket-is-alive connection */
  PING,

  /* IGNORE: FOR INTERNAL USE ONLY */
  NIL,
}
