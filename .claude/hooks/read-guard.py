#!/usr/bin/env python3
"""PreToolUse hook for Read: stops whole-file reads of long text files.

A file over MAX_LINES read without `offset` or `limit` costs thousands of tokens that stay in the context for the
rest of the session, usually to find a few lines. The hook denies such a read and tells Claude to search with Grep
or read the lines it needs. A read with `offset` or `limit` always passes, so reading a whole file stays possible
when it is really needed. Images, PDFs and notebooks always pass. Any error lets the read through.
"""

import json
import os
import sys

MAX_LINES = 400
ALWAYS_ALLOWED = (".png", ".jpg", ".jpeg", ".gif", ".webp", ".pdf", ".ipynb")


def main() -> None:
    try:
        tool_input = json.load(sys.stdin).get("tool_input") or {}
        path = tool_input.get("file_path") or ""
        if tool_input.get("offset") is not None or tool_input.get("limit") is not None:
            return
        if not path or path.lower().endswith(ALWAYS_ALLOWED) or not os.path.isfile(path):
            return
        with open(path, "rb") as file:
            lines = sum(1 for _ in file)
    except Exception:
        return
    if lines <= MAX_LINES:
        return
    reason = (
        f"{os.path.basename(path)} has {lines} lines (over {MAX_LINES}). Find what you need with Grep "
        f"(with -n and -C for context), then Read only that range with offset and limit. If you really need "
        f"the whole file, Read it again with limit={lines}."
    )
    json.dump(
        {
            "hookSpecificOutput": {
                "hookEventName": "PreToolUse",
                "permissionDecision": "deny",
                "permissionDecisionReason": reason,
            },
        },
        sys.stdout,
    )


if __name__ == "__main__":
    main()
