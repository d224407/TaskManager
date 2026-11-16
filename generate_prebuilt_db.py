import json
import sqlite3
import os
import sys

# Paths relative to this script
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
json_path = os.path.join(SCRIPT_DIR, "uad_lists.json")
db_path = os.path.join(SCRIPT_DIR, "app/src/main/assets/databases/apps.db")

print(f"JSON file: {json_path}")
print(f"DB file: {db_path}")

os.makedirs(os.path.dirname(db_path), exist_ok=True)

# Delete old DB if it exists
if os.path.exists(db_path):
    os.remove(db_path)

conn = None
try:
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    cursor.execute("""
        CREATE TABLE apps (
            id TEXT NOT NULL PRIMARY KEY,
            description TEXT
        )
    """)

    with open(json_path, "r", encoding="utf-8") as f:
        apps = json.load(f)

    cursor.executemany(
        "INSERT OR IGNORE INTO apps (id, description) VALUES (?, ?)",
        [(app["id"], app["description"]) for app in apps]
    )

    # Must match @Database(version = 1) in Data.kt
    cursor.execute("PRAGMA user_version = 1")

    conn.commit()
    print(f"[OK] Inserted {len(apps)} apps into database")

except Exception as e:
    print(f"[ERROR] Failed to generate prebuilt database: {e}")
    sys.exit(1)

finally:
    if conn is not None:
        conn.close()
