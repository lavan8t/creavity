# Issue 01: Instant Button Exclusivity and Connectivity Checks

Status: resolved
Blockers: none

## Context
1. In `PeerConnectScreen`, tapping Host or Join had asynchronous latency before `uiState.isAdvertising`/`isDiscovering` updated, allowing double-taps on both buttons.
2. Bluetooth and Wi-Fi checks must prompt the user when disabled so Nearby Connections and LAN discovery can function reliably.

## Acceptance Criteria
1. Tapping Host immediately sets local/optimistic active mode to Host, changes its label to "Cancel", and immediately greys out and disables the Join button with 0ms delay.
2. Tapping Join immediately sets local/optimistic active mode to Join, changes its label to "Cancel", and immediately greys out and disables the Host button with 0ms delay.
3. Tapping Cancel immediately resets both buttons to enabled and active.
4. On Android, if Bluetooth or Wi-Fi is disabled, prompt user with system settings or enable intent, and display a helpful status banner.
