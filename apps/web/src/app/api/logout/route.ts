import { NextResponse } from "next/server";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export async function POST(request: Request) {
  const cookieHeader = request.headers.get("cookie") ?? "";
  try {
    await fetch(`${API_URL}/auth/logout`, {
      method: "POST",
      headers: { cookie: cookieHeader },
    });
  } catch {
    // backend may be unreachable — proceed to clear cookie anyway
  }

  const res = NextResponse.json({ ok: true });
  res.cookies.set("auth_token", "", { maxAge: 0, path: "/" });
  return res;
}
