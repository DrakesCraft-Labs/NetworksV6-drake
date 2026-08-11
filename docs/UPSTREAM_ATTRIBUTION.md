# Upstream Attribution

## NetworksExpansion portability work

This repository ports selected gameplay behavior from
[NetworksExpansion](https://github.com/balugaq/NetworksExpansion), inspected at
commit `5cfd662` (2026-08-11).

The upstream project is a complete replacement fork of Networks and targets the
Gugu Slimefun API, JEG, and Gugu storage services. DrakesCraft must remain a
single `NetworksV6-Drake` addon built against the Drake Slimefun API, so the
upstream JAR is not bundled, loaded, or registered beside this plugin.

The first native port is the advanced auto-crafter behavior: stacks of identical
crafting blueprints can be processed together. Drake's implementation additionally
charges power per blueprint and refuses a batch whose result cannot fit in one
inventory stack. Those checks keep the operation atomic and prevent item loss or
free throughput.

Future ports must be made module by module. Any candidate that requires a Gugu
core namespace, JEG, or a second primary storage system needs a Drake-compatible
adapter and regression tests before it is registered.
