#!/usr/bin/env python3
"""
Spaceship REST API Client for Domain & DNS Management.
Configures and pushes DNS records directly for matrixrain.app.
"""

import argparse
import json
import os
import re
import ssl
import sys
import urllib.error
import urllib.request
from typing import Any, Dict, List, Optional, Tuple

try:
    import certifi
    SSL_CONTEXT = ssl.create_default_context(cafile=certifi.where())
except Exception:
    SSL_CONTEXT = ssl._create_unverified_context()

BASE_URL = "https://spaceship.dev/api/v1"


class SpaceshipClient:
    """Client for Spaceship REST API."""

    def __init__(self, api_key: Optional[str] = None, api_secret: Optional[str] = None):
        self._api_key = api_key or os.environ.get("SPACESHIP_API_KEY")
        self._api_secret = api_secret or os.environ.get("SPACESHIP_API_SECRET")

        if not self._api_key or not self._api_secret:
            self._api_key, self._api_secret = self._read_zshenv_credentials()

    def _read_zshenv_credentials(self) -> Tuple[Optional[str], Optional[str]]:
        zshenv_path = os.path.expanduser("~/.zshenv")
        api_key, api_secret = None, None
        if os.path.exists(zshenv_path):
            try:
                with open(zshenv_path, "r", encoding="utf-8") as f:
                    content = f.read()
                    m_key = re.search(r'export\s+SPACESHIP_API_KEY=["\']?([^"\'\n]+)["\']?', content)
                    m_sec = re.search(r'export\s+SPACESHIP_API_SECRET=["\']?([^"\'\n]+)["\']?', content)
                    if m_key:
                        api_key = m_key.group(1).strip()
                    if m_sec:
                        api_secret = m_sec.group(1).strip()
            except Exception:
                pass
        return api_key, api_secret

    @property
    def has_credentials(self) -> bool:
        return bool(self._api_key and self._api_secret)

    def api_request(
        self,
        path: str,
        method: str = "GET",
        body: Optional[Dict[str, Any]] = None,
    ) -> Dict[str, Any]:
        """Executes HTTP request against Spaceship REST API."""
        if not self.has_credentials:
            raise RuntimeError("Spaceship credentials missing: SPACESHIP_API_KEY and SPACESHIP_API_SECRET must be set.")

        url = f"{BASE_URL}{path}"
        headers = {
            "X-API-Key": self._api_key,
            "X-API-Secret": self._api_secret,
            "Accept": "application/json",
            "User-Agent": "MatrixRain-Deployer/1.0",
        }

        data = None
        if body is not None:
            headers["Content-Type"] = "application/json"
            data = json.dumps(body).encode("utf-8")

        req = urllib.request.Request(url, data=data, headers=headers, method=method)

        try:
            with urllib.request.urlopen(req, timeout=15, context=SSL_CONTEXT) as resp:
                status = resp.status
                content = resp.read().decode("utf-8")
                if not content and status == 204:
                    return {"status": "success", "code": 204}
                return json.loads(content)
        except urllib.error.HTTPError as e:
            err_msg = e.read().decode("utf-8", errors="ignore")
            raise RuntimeError(f"Spaceship API HTTP Error {e.code} ({e.reason}): {err_msg}") from e
        except Exception as e:
            raise RuntimeError(f"Spaceship API request failed: {e}") from e

    def get_dns_records(self, domain: str, take: int = 100, skip: int = 0) -> List[Dict[str, Any]]:
        """Fetches DNS resource records for a domain."""
        data = self.api_request(f"/dns/records/{domain}?take={take}&skip={skip}")
        return data.get("items", [])

    def set_dns_records(self, domain: str, items: List[Dict[str, Any]], force: bool = True) -> Dict[str, Any]:
        """Sets DNS resource records for a domain via PUT."""
        payload = {"force": force, "items": items}
        return self.api_request(f"/dns/records/{domain}", method="PUT", body=payload)

    def configure_vercel(self, domain: str, ttl: int = 1200) -> Dict[str, Any]:
        """Configures apex A record to Vercel and www CNAME."""
        items = [
            {"name": "@", "type": "A", "address": "76.76.21.21", "ttl": ttl},
            {"name": "www", "type": "CNAME", "cname": "cname.vercel-dns.com", "ttl": ttl},
        ]
        return self.set_dns_records(domain, items, force=True)

    def configure_custom_ip(self, domain: str, ip: str, ttl: int = 1200) -> Dict[str, Any]:
        """Configures apex A record pointing to a custom server IP."""
        items = [{"name": "@", "type": "A", "address": ip, "ttl": ttl}]
        return self.set_dns_records(domain, items, force=True)


def main():
    parser = argparse.ArgumentParser(description="Spaceship DNS Configurator for matrixrain.app")
    parser.add_argument("command", choices=["get", "set-vercel", "set-ip"], nargs="?", default="get")
    parser.add_argument("--domain", default="matrixrain.app", help="Domain to configure (default: matrixrain.app)")
    parser.add_argument("--ip", help="IP address for set-ip command")
    args = parser.parse_args()

    client = SpaceshipClient()
    if not client.has_credentials:
        print("❌ Error: Spaceship credentials not found in environment or ~/.zshenv")
        sys.exit(1)

    print(f"🛰️ Spaceship API Connected (Domain: {args.domain})")

    if args.command == "get":
        records = client.get_dns_records(args.domain)
        print(f"\nActive DNS Records for {args.domain}:")
        if not records:
            print("  (No records configured)")
        for r in records:
            val = r.get("address") or r.get("cname") or r.get("value")
            print(f"  • {r.get('type')} {r.get('name')} -> {val} (TTL: {r.get('ttl')})")
    elif args.command == "set-vercel":
        res = client.configure_vercel(args.domain)
        print(f"✔ Successfully configured Vercel DNS for {args.domain}: {res}")
    elif args.command == "set-ip":
        if not args.ip:
            print("❌ Error: --ip argument required for set-ip")
            sys.exit(1)
        res = client.configure_custom_ip(args.domain, args.ip)
        print(f"✔ Successfully configured IP {args.ip} for {args.domain}: {res}")


if __name__ == "__main__":
    main()
