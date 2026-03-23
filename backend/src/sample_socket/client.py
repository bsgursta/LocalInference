import socket

HOST = '127.0.0.1'
PORT = 9999


client = socket.socket(family=socket.AF_INET, type=socket.SOCK_STREAM)
client.connect((HOST, 9999))

while True:
    msg = input("Enter a message to send to server: ")
    client.send(msg.encode())
    print(client.recv(1024).decode())