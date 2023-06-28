from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import subprocess

# Define the request handler class
class RequestHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        content_type = self.headers.get('Content-Type')
        if content_type != 'application/json':
            self.send_error(400, 'Bad Request: Invalid Content-Type')
            return

        content_length = int(self.headers.get('Content-Length', 0))
        post_data = self.rfile.read(content_length)
        json_data = json.loads(post_data)

        # Process the boolean input
        kyklos_production = json_data.get('KyklosProduction', False)
        if kyklos_production:
            # Perform actions when KyklosProduction is True
            print("KyklosProduction is True")
            # Add your logic here

            # Make the curl request
            curl_command = 'curl -H "Content-Type: application/json" -X POST -d \'{"KyklosProduction": true}\' https://gft-kyklos.duckdns.org/endpoints/dsw'
            subprocess.run(curl_command, shell=True)

        else:
            # Perform actions when KyklosProduction is False
            print("KyklosProduction is False")
            # Add your logic here

            # Make the curl request
            curl_command = 'curl -H "Content-Type: application/json" -X POST -d \'{"KyklosProduction": false}\' https://gft-kyklos.duckdns.org/endpoints/dsw'
            subprocess.run(curl_command, shell=True)

        # Send a response back to the client
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        response = {'message': 'Request received and processed'}
        self.wfile.write(json.dumps(response).encode('utf-8'))

# Create an HTTP server and specify the request handler
server_address = ('0.0.0.0', 8600)  # You can change the port number if needed
httpd = HTTPServer(server_address, RequestHandler)
httpd.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR,1)
httpd.socker.setsockopt(socket.SOL_SOCKET, socker.SO_BINDTODEVICE, b'spnet')
print('Server started on port 8000...')

try:
    # Start the server
    httpd.serve_forever()
except KeyboardInterrupt:
    # Handle keyboard interrupt to stop the server
    print('Server stopped')
    httpd.server_close()
