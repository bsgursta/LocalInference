package com.proto.localinference.socket;

public enum McuCommand {
  /* Server -> MCU: updates the model weights used */
  UPDATE,

  /* MCU -> Server: pings the server about a an *incident* that was triggered */
  NOTIFY,

  /* MCU -> Server: MCU opens a socket connection with server for the first time
  NOTE: REQUIRES MCU UUID TO ALREADY BE REGISTERED AHEAD OF TIME                    */
  REGISTER,

  /* MCU -> Server: MCU reopens a new socket connection due to previously closed connection */
  RECONNECT,

  /* MCU ->  Server: Checks for valid socket-is-alive connection */
  HEALTHCHECK,

  /* Server -> MCU -> Server: Server checks for location of MCU, MCU responsds with an incoming GPS position */
  GPS,

  /* MCU -> Server: Streams audio of positive occurence */
  AUDIO_STREAM,

  /* IGNORE: FOR INTERNAL USE ONLY. Serves no purpose except to act as a blanket FAIL option */
  NIL,
}
