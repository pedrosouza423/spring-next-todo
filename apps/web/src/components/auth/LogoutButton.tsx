"use client";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";

export function LogoutButton() {
  const router = useRouter();
  async function handleLogout() {
    try {
      await api.auth.logout();
    } catch {
      // redirect to login regardless — cookie will expire naturally
    }
    router.push("/login");
  }
  return (
    <Button variant="outline" size="sm" onClick={handleLogout}>
      Sair
    </Button>
  );
}
