from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import subprocess

class RequestHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers['Content-Lenght'])
        post_data = self.rfile.read(content_length)
        json_data = json.loads(post_data)

        kyklos_prod = json_data.get('KyklosProduction', False)
        if kyklos_prod:
            print("KyklosProduction is True")
            curl_command = 'curl -H "Content-Type: application/json" -X POST -d \'{"KyklosProduction": true}\' https://gft-kyklos.duckdns.org/endpoints/dsw'
            subprocess.run(curl_command, shell=True)
        else:
            print("KyklosProduction is False")
            curl_command = 'curl -H "Content-Type: application/json" -X POST -d \'{"KyklosProduction": false}\' https://gft-kyklos.duckdns.org/endpoints/dsw'
            subprocess.run(curl_command, shell=True)

        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        response = {'message': 'Request received and processed'}
        self.wfile.write(json.dumps(response).encodes('utf-8'))

server_address = ('', 8000)
httpd = HTTPServer(server_address, RequestHandler)
print('Server started on port 8000...')

try:
    httpd.serve_forever()
except KeyboardInterrupt:
    print('Server stopped')
    httpd.server_close()
