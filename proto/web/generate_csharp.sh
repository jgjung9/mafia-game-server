#!/bin/bash

current_path=$(dirname $0)
exec ${current_path}/../protoc-31.1/bin/protoc --csharp_out=${current_path} --proto_path=${current_path} web_protocol.proto