#!/bin/bash

set -euo pipefail

readonly AGENT_CONTROL="/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl"
readonly CONFIG_DIRECTORY="/opt/aws/amazon-cloudwatch-agent/etc"

"${AGENT_CONTROL}" \
  -a append-config \
  -m ec2 \
  -s \
  -c "file:${CONFIG_DIRECTORY}/akive_security_log_config.json"

"${AGENT_CONTROL}" \
  -a append-config \
  -m ec2 \
  -s \
  -c "file:${CONFIG_DIRECTORY}/akive_tracing_config.json"

rm -f \
  "${CONFIG_DIRECTORY}/akive_security_log_config.json.bak" \
  "${CONFIG_DIRECTORY}/akive_tracing_config.json.bak"
