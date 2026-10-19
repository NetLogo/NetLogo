#!/usr/bin/env python

import os
import socket
import sys
import numbers
import json

if sys.version_info[0] > 2 and sys.version_info[1] > 3:
    from collections.abc import Mapping
else:
    from collections import Mapping

import traceback

# In
QUIT_MSG = -1
STMT_MSG = 0
EXPR_MSG = 1
ASSN_MSG = 2
EXPR_STRINGIFIED_MSG = 3
HEARTBEAT_REQUEST = 4

# Out
SUCC_MSG = 0
ERR_MSG = 1
HEARTBEAT_RESPONSE = 4

def start_server():
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        conn = make_connection(sock)
        try:
            encoder = FlexibleEncoder()
            env_globals = {}
            for line in conn.makefile():
                try:
                    decoded = json.loads(line)
                    type = decoded["type"]

                    if type == QUIT_MSG:
                        conn.sendall(json.dumps({"type": SUCC_MSG}).encode('utf-8') + b"\n")
                        break
                    elif type == STMT_MSG:
                        handle_statement(conn, decoded["body"], env_globals, encoder)
                    elif type == EXPR_MSG:
                        handle_expression(conn, decoded["body"], env_globals, encoder)
                    elif type == ASSN_MSG:
                        handle_assignment(conn, decoded["body"], env_globals, encoder)
                    elif type == EXPR_STRINGIFIED_MSG:
                        handle_expression_stringified(conn, decoded["body"], env_globals, encoder)
                    elif type == HEARTBEAT_REQUEST:
                        handle_heartbeat(conn)
                except Exception as e:
                    handle_exception(conn, e, encoder)
                finally:
                    flush()
        finally:
            conn.close()
    finally:
        sock.close()

def make_connection(sock):
    sock.bind(('localhost', 0))
    sock.listen(0)
    _, port = sock.getsockname()
    sys.stdout.write("{}\n".format(port))
    sys.stdout.flush()
    conn, addr = sock.accept()
    return conn

def handle_heartbeat(conn):
    conn.sendall(json.dumps({"type": HEARTBEAT_RESPONSE}).encode('utf-8') + b"\n")

def handle_statement(conn, body, env_globals, encoder):
    exec(body, env_globals)
    conn.sendall(json.dumps({"type": SUCC_MSG, "body": ""}).encode('utf-8') + b"\n")

def handle_expression(conn, body, env_globals, encoder):
    evaluated = eval(body, env_globals)
    encoded = encoder.encode({"type": SUCC_MSG, "body": evaluated})
    conn.sendall(encoded.encode('utf-8') + b"\n")


def handle_assignment(conn, body, env_globals, encoder):
    varName = body["varName"]
    value = body["value"]
    env_globals[varName] = value
    conn.sendall(json.dumps({"type": SUCC_MSG, "body": ""}).encode('utf-8') + b"\n")


def handle_expression_stringified(conn, body, env_globals, encoder):
    representation = ""
    if len(body.strip()) > 0:
        ## Ask python if the given string can be evaluated as an expression. If so, evaluate it and return it, if not,
        ## Then try running it as code that doesn't evaluate to anything and return nothing.
        try:
            compiled = compile(body, "<string>", 'eval')
            evaluated = eval(compiled, env_globals)
            if evaluated is not None:
                if isinstance(evaluated, str):
                    representation = evaluated
                else:
                    representation = repr(evaluated)

        except SyntaxError as e:
            exec(body, env_globals)

    encoded = encoder.encode({"type": SUCC_MSG, "body": representation})
    conn.sendall(encoded.encode('utf-8') + b"\n")


def handle_exception(conn, e, encoder):
    err_data = {"type": ERR_MSG, "body": {"message": str(e), "longMessage": traceback.format_exc()}}
    conn.sendall(encoder.encode(err_data).encode('utf-8') + b"\n")


def flush():
    sys.stdout.flush()
    sys.stderr.flush()


class FlexibleEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, numbers.Integral):
            return int(o)
        if isinstance(o, numbers.Number):
            return float(o)
        if isinstance(o, Mapping):
            return dict(o)
        elif hasattr(o, '__len__') and hasattr(o, '__iter__'):
            return list(o)
        else:
            return json.JSONEncoder.default(self, o)  # let it error


if __name__ == '__main__':
    sys.path.insert(0, os.getcwd())
    start_server()
