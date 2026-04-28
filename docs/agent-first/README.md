# Agent-First Harness Notes

This repository uses an agent-first harness pattern:

- `AGENTS.md` is a concise map.
- Detailed context lives under `docs/`.
- Execution plans are versioned in `docs/exec-plans/`.
- CI verifies doc structure with `scripts/check_agent_docs.sh`.

This keeps context discoverable for both humans and agents while reducing prompt bloat.
