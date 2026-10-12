import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

/**
 * Deletes only the authenticated Supabase Auth user.
 * Configure SUPABASE_URL, SUPABASE_ANON_KEY and SUPABASE_SERVICE_ROLE_KEY
 * as Edge Function secrets. Never ship the service-role key in the Android app.
 */
Deno.serve(async (request: Request) => {
  if (request.method !== "POST") {
    return new Response(JSON.stringify({ error: "method_not_allowed" }), {
      status: 405,
      headers: { "Content-Type": "application/json", "Allow": "POST" },
    });
  }

  const authorization = request.headers.get("Authorization");
  if (!authorization?.startsWith("Bearer ")) {
    return new Response(JSON.stringify({ error: "missing_bearer_token" }), {
      status: 401,
      headers: { "Content-Type": "application/json" },
    });
  }

  const url = Deno.env.get("SUPABASE_URL");
  const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !anonKey || !serviceRoleKey) {
    return new Response(JSON.stringify({ error: "server_not_configured" }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }

  const token = authorization.slice("Bearer ".length).trim();
  const userClient = createClient(url, anonKey, {
    global: { headers: { Authorization: `Bearer ${token}` } },
    auth: { persistSession: false, autoRefreshToken: false },
  });
  const { data: { user }, error: userError } = await userClient.auth.getUser(token);
  if (userError || !user) {
    return new Response(JSON.stringify({ error: "invalid_session" }), {
      status: 401,
      headers: { "Content-Type": "application/json" },
    });
  }

  const adminClient = createClient(url, serviceRoleKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });

  // Delete only rows owned by the verified user, child tables first.
  // These tables use the same user_id ownership key as the Android sync layer.
  const ownedTables = [
    "individual_entry_items",
    "individual_entries",
    "entries",
    "days",
    "pieces",
    "months",
    "workers",
    "shops",
    "user_profiles",
  ];
  for (const table of ownedTables) {
    const { error } = await adminClient.from(table).delete().eq("user_id", user.id);
    if (error && error.code !== "42P01") {
      // Abort before deleting the auth identity if a known table cannot be cleaned.
      return new Response(JSON.stringify({ error: "account_data_delete_failed", table }), {
        status: 500,
        headers: { "Content-Type": "application/json" },
      });
    }
  }

  const { error: deleteError } = await adminClient.auth.admin.deleteUser(user.id);
  if (deleteError) {
    return new Response(JSON.stringify({ error: "delete_failed" }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }

  return new Response(JSON.stringify({ deleted: true }), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
});
