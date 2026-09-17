import argparse
import csv
import json
import sys
from collections import defaultdict
from decimal import Decimal
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


DEFAULT_BASE_URL = "http://localhost:8080"
DEFAULT_EMAIL = "admin@payments.com"
DEFAULT_PASSWORD = "admin123"
DEFAULT_OUTPUT = "reports/payment_summary.csv"


def authenticate(base_url, email, password):
    url = f"{base_url}/auth/login"

    body = json.dumps({
        "email": email,
        "password": password
    }).encode("utf-8")

    request = Request(
        url,
        data=body,
        headers={"Content-Type": "application/json"},
        method="POST"
    )

    try:
        with urlopen(request, timeout=10) as response:
            data = json.load(response)

            token = data.get("token")

            if not token:
                raise RuntimeError(
                    "Authentication response did not contain a token."
                )

            return token

    except HTTPError as error:
        if error.code in (401, 403):
            raise RuntimeError(
                "Authentication failed: invalid credentials."
            ) from error

        raise RuntimeError(
            f"Authentication failed with HTTP status {error.code}."
        ) from error

    except URLError as error:
        raise RuntimeError(
            f"Connection failed: could not connect to {base_url}."
        ) from error


def get_payments(base_url, token):
    url = f"{base_url}/payments"

    request = Request(
        url,
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/json"
        },
        method="GET"
    )

    try:
        with urlopen(request, timeout=10) as response:
            return json.load(response)

    except HTTPError as error:
        if error.code in (401, 403):
            raise RuntimeError(
                "Authentication failed while retrieving payments."
            ) from error

        raise RuntimeError(
            f"Failed to retrieve payments: HTTP {error.code}."
        ) from error

    except URLError as error:
        raise RuntimeError(
            f"Connection failed: could not connect to {base_url}."
        ) from error


def summarize_payments(payments):
    summary = defaultdict(
        lambda: {
            "total": Decimal("0.00"),
            "count": 0
        }
    )

    for payment in payments:
        if payment.get("status") != "PROCESSED":
            continue

        customer_id = payment.get("customerId")
        amount = payment.get("amount")

        if customer_id is None or amount is None:
            continue

        amount = Decimal(str(amount))

        summary[customer_id]["total"] += amount
        summary[customer_id]["count"] += 1

    return summary


def write_csv(summary, output_file):
    output_path = Path(output_file)
    output_path.parent.mkdir(
        parents=True,
        exist_ok=True
    )

    with output_path.open(
        "w",
        newline="",
        encoding="utf-8"
    ) as file:
        writer = csv.writer(file)

        writer.writerow([
            "customer_id",
            "total_amount",
            "payment_count",
            "average_amount"
        ])

        for customer_id in sorted(summary):
            total = summary[customer_id]["total"]
            count = summary[customer_id]["count"]

            average = total / count

            writer.writerow([
                customer_id,
                f"{total:.2f}",
                count,
                f"{average:.2f}"
            ])


def parse_arguments():
    parser = argparse.ArgumentParser(
        description=(
            "Generate a CSV payment summary grouped by customer."
        )
    )

    parser.add_argument(
        "--base-url",
        default=DEFAULT_BASE_URL,
        help=f"Payment Processor API URL (default: {DEFAULT_BASE_URL})"
    )

    parser.add_argument(
        "--email",
        default=DEFAULT_EMAIL,
        help=f"Login email (default: {DEFAULT_EMAIL})"
    )

    parser.add_argument(
        "--password",
        default=DEFAULT_PASSWORD,
        help="Login password"
    )

    parser.add_argument(
        "--output",
        default=DEFAULT_OUTPUT,
        help=f"CSV output path (default: {DEFAULT_OUTPUT})"
    )

    return parser.parse_args()


def main():
    args = parse_arguments()

    try:
        token = authenticate(
            args.base_url,
            args.email,
            args.password
        )

        payments = get_payments(
            args.base_url,
            token
        )

        summary = summarize_payments(payments)

        write_csv(
            summary,
            args.output
        )

        print(
            f"Report generated successfully: {args.output}"
        )

    except RuntimeError as error:
        print(
            f"Error: {error}",
            file=sys.stderr
        )
        sys.exit(1)

    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(
            f"Error generating report: {error}",
            file=sys.stderr
        )
        sys.exit(1)


if __name__ == "__main__":
    main()