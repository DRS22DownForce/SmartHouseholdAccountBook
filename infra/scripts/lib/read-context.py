#!/usr/bin/env python3
"""cdk.json の context と cdk.local.json をマージして値を取得する。"""
import json
import sys
from pathlib import Path


def load_merged_context(infra_dir: Path) -> dict:
    ctx: dict = {}
    cdk_json = infra_dir / "cdk.json"
    if cdk_json.is_file():
        data = json.loads(cdk_json.read_text(encoding="utf-8"))
        ctx.update(data.get("context") or {})

    local_json = infra_dir / "cdk.local.json"
    if local_json.is_file():
        ctx.update(json.loads(local_json.read_text(encoding="utf-8")))

    return ctx


def main() -> None:
    if len(sys.argv) < 2:
        print("usage: read-context.py <infra-dir> [key]", file=sys.stderr)
        sys.exit(1)

    infra_dir = Path(sys.argv[1])
    ctx = load_merged_context(infra_dir)

    if len(sys.argv) >= 3:
        key = sys.argv[2]
        value = ctx.get(key, "")
        if value is None:
            print("")
        else:
            print(str(value).strip())
    else:
        print(json.dumps(ctx, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
