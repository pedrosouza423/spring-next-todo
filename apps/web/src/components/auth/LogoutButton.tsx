"use client";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";

export function LogoutButton() {
  const router = useRouter();
  async function handleLogout() {
    await api.auth.logout();
    router.push("/login");
  }
  return (
    <Button variant="outline" size="sm" onClick={handleLogout}>
      Sair
    </Button>
  );
}
