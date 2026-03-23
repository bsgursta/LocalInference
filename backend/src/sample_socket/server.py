import socket

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(('0.0.0.0', 9999)) 


server.listen(1)
client, addr = server.accept()
while True:
    msg = client.recv(1024).decode()
    print(msg)
    client.send(f'Heard {msg}'.encode())