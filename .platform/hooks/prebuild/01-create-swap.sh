#!/bin/bash

set -e

if [ "$ENABLE_SWAP" != "true" ]; then
    echo "[Swap] Skip swap creation."
    exit 0
fi

SWAP_FILE="/swapfile"
SWAP_SIZE="1G"

if swapon --show | grep -q "$SWAP_FILE"; then
    echo "[Swap] Swap is already active."
    exit 0
fi

if [ ! -f "$SWAP_FILE" ]; then
    echo "[Swap] Creating swap file..."

    fallocate -l "$SWAP_SIZE" "$SWAP_FILE"
    chmod 600 "$SWAP_FILE"
    mkswap "$SWAP_FILE"
fi

swapon "$SWAP_FILE"

if ! grep -q "$SWAP_FILE" /etc/fstab; then
    echo "$SWAP_FILE swap swap defaults 0 0" >> /etc/fstab
fi

echo "[Swap] Swap enabled."