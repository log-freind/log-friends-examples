#!/usr/bin/env python3
import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


class IngestHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path != "/ingest":
            self.send_error(404)
            return

        length = int(self.headers.get("Content-Length", "0"))
        body = self.rfile.read(length).decode("utf-8")
        payload = json.loads(body)
        event_types = [event.get("type", "UNKNOWN") for event in payload.get("events", [])]

        print(
            "POST /ingest",
            f"workerId={payload.get('workerId')}",
            f"events={len(event_types)}",
            f"types={event_types}",
            flush=True,
        )
        print(json.dumps(payload, ensure_ascii=False, indent=2), flush=True)

        self.send_response(202)
        self.end_headers()

    def log_message(self, _format, *_args):
        return


if __name__ == "__main__":
    server = ThreadingHTTPServer(("127.0.0.1", 8089), IngestHandler)
    print("Mock Log Friends ingest listening on http://127.0.0.1:8089/ingest", flush=True)
    server.serve_forever()
