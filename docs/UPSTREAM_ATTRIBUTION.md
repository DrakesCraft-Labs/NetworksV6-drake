# Upstream Attribution

## NetworksExpansion portability work

This repository ports selected gameplay behavior from
[NetworksExpansion](https://github.com/balugaq/NetworksExpansion), inspected at
commit `5cfd662` (2026-08-11).

The upstream project is a complete replacement fork of Networks and targets the
Gugu Slimefun API, JEG, and Gugu storage services. DrakesCraft must remain a
single `NetworksV6-Drake` addon built against the Drake Slimefun API, so the
upstream JAR is not bundled, loaded, or registered beside this plugin.

The native ports currently included are:

- Advanced auto-crafter behavior: stacks of identical crafting blueprints can
  be processed together. Drake's implementation additionally charges power per
  blueprint and refuses a batch whose result cannot fit in one inventory stack.
- Input-only and output-only monitors: directional storage links which make the
  intended one-way flow explicit without changing a player's existing monitors.
- Advanced import: a 54-slot buffer that transfers through Drake's existing
  synchronized, remainder-safe import path.
- Advanced export: eighteen templates and eighteen output slots; failed menu
  insertions flow back through the same protected network return path.
- Advanced purger: up to forty-eight explicit filters, with the standard
  purger's deliberate destruction semantics and synchronous world access.
- Advanced vacuum: eighteen buffered slots, persistent blacklist/whitelist
  filters and material or exact-item matching, implemented on Drake's legacy
  BlockStorage without Gugu storage dependencies.

The auto-crafter checks keep the operation atomic and prevent item loss or free
throughput. The monitor port updates the root's input/output caches separately,
so a one-way link cannot silently become a bidirectional storage endpoint.

Future ports must be made module by module. Any candidate that requires a Gugu
core namespace, JEG, or a second primary storage system needs a Drake-compatible
adapter and regression tests before it is registered.
