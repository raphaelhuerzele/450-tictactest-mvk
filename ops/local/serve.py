"""Local static/version servers. Runtime configuration stays outside release archives."""
import argparse
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer


class Handler(SimpleHTTPRequestHandler):
    version = None

    def do_GET(self):
        if self.version is not None:
            if self.path.split('?')[0] != '/version':
                self.send_error(404)
                return
            content = (self.version + '\n').encode()
            self.send_response(200)
            self.send_header('Content-Type', 'text/plain; charset=utf-8')
            self.send_header('Cache-Control', 'no-store')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Content-Length', str(len(content)))
            self.end_headers()
            self.wfile.write(content)
        else:
            super().do_GET()


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--port', type=int, required=True)
    parser.add_argument('--directory', default='.')
    parser.add_argument('--version')
    args = parser.parse_args()
    Handler.version = args.version
    ThreadingHTTPServer(('127.0.0.1', args.port), partial(Handler, directory=args.directory)).serve_forever()
