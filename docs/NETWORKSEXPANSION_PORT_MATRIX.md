# NetworksExpansion Port Matrix

This is the compatibility boundary for the Drake edition. It is deliberately
feature based: DrakesCraft does not load the Gugu fork, JEG, or a second
storage engine beside its existing production data.

## Native Drake ports

| Upstream capability | Drake item or behavior | Data impact |
| --- | --- | --- |
| Advanced auto-crafter | `NTW_ADVANCED_AUTO_CRAFTER` and withholding variant | New item IDs only |
| One-way monitors | Input-only and output-only monitor nodes | New item IDs only |
| Advanced import/export | `NTW_ADVANCED_IMPORT`, `NTW_ADVANCED_EXPORT` | New item IDs only |
| Advanced purger | `NTW_ADVANCED_PURGER` | New item ID only |
| Advanced vacuum | `NTW_ADVANCED_VACUUM` | New BlockStorage keys only |
| Advanced greedy block | `NTW_ADVANCED_GREEDY_BLOCK` | New item ID only |
| Smart grabber/pusher | `NTW_ADVANCED_GRABBER`, `NTW_ADVANCED_PUSHER` | New item IDs only |

Existing Network, Quantum Storage and Slimefun BlockStorage records are never
converted by these ports. SQL mirroring remains optional and disabled by
default; it is not part of this feature set.

## Not imported as upstream code

| Upstream group | Reason | Drake direction |
| --- | --- | --- |
| New-style grids and hanging grids | Depend on Gugu/JEG GUI and storage APIs | Extend the existing Drake Grid after a dedicated UI regression suite |
| Advanced wireless transmitter | Upstream transfer path has conflicting return semantics | Build a Drake version only after target selection and cross-root accounting are specified |
| Switching monitor | Depends on upstream hanging-block persistence | Treat as a separate Paper 1.21.11 feature, not a storage fork |
| Offsetter and Due Machine | Generic cargo utilities, not a network-storage requirement | Consider as independent Drake automation modules with protection tests |
| SuperTrash | Replaces menus through reflection | Intentionally excluded: reflection-based menu replacement is unsafe for a long-lived server |

## Deployment gate

Before this branch is deployed, run the in-game smoke test on the production
addon set: old greedy block, advanced greedy block, a protected external
inventory, a full output inventory, restart persistence, and a failed wireless
insert. Do not run `/sf migrate`; it is unrelated and irreversible.
