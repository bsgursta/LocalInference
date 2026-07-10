import socket

# Configuration
HOST = "192.168.1.241"  # Localhost
PORT = 5000        # Arbitrary non-privileged port

def start_echo_server():
    # AF_INET = IPv4, SOCK_STREAM = TCP
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        # Prevent "Address already in use" errors on immediate restart
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        
        # Bind and start listening
        server_socket.bind((HOST, PORT))
        server_socket.listen()
        print(f"[STARTING] Server is listening on {HOST}:{PORT}...")

        while True:
            # Block and wait for a client connection
            conn, addr = server_socket.accept()
            with conn:
                while True:
                    data = conn.recv(1024)
                    if not data:
                        break
                    conn.sendall(data) # Echo data back

if __name__ == "__main__":
    start_echo_server()

