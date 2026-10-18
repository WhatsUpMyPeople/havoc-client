#!/usr/bin/env python3
"""
Havoc Client — Discord server setup.

Builds the whole server structure in one shot: roles, categories, channels,
permission overwrites, pinned messages, invite link.

USAGE
  pip install -U "discord.py>=2.4"
  python havoc-discord-setup.py <BOT_TOKEN> <GUILD_ID>

GUILD_ID: enable Developer Mode in Discord settings → right-click your server
icon → Copy Server ID.

The script is IDEMPOTENT by name: re-running it won't duplicate roles or
channels that already exist with the same name — it reuses them. Edit the
STRUCTURE constants below to tweak and re-run.
"""

import asyncio
import sys
from dataclasses import dataclass, field
from typing import Optional

import discord


# ---------------------------------------------------------------------------
# BRAND
# ---------------------------------------------------------------------------
BRAND              = "Havoc"
WEBSITE_URL        = "https://claude.ai/artifact/YSZKHZsWybSWqe7kDBqCH8"  # edit after you set a vanity domain
SELLER_DISCORD     = "0v68"
PAYPAL_EMAIL       = "monteirogavin20@gmail.com"
PAYPAL_RECIPIENT   = "charles monteiro"
GITHUB_REPO        = "https://github.com/WhatsUpMyPeople/havoc-client"

ACCENT_RED   = discord.Color.from_rgb(255, 42, 77)
PURPLE       = discord.Color.from_rgb(176, 92, 255)
GOLD         = discord.Color.from_rgb(255, 191, 71)
SILVER       = discord.Color.from_rgb(190, 198, 210)
GREEN        = discord.Color.from_rgb(74, 222, 128)
BLUE         = discord.Color.from_rgb(100, 170, 255)


# ---------------------------------------------------------------------------
# STRUCTURE
# ---------------------------------------------------------------------------
# Roles — hoisted (shown separately in member list) when `hoist=True`.
# Order matters: top of this list sits highest in the role hierarchy.
ROLES = [
    # (name, color, hoist, mentionable)
    ("Owner",     ACCENT_RED, True,  True),
    ("Staff",     BLUE,       True,  True),
    ("Lifetime",  PURPLE,     True,  True),
    ("Monthly",   GOLD,       True,  True),
    ("Daily",     SILVER,     True,  True),
    ("Verified",  GREEN,      False, False),
    ("Bot",       discord.Color.dark_grey(), False, False),
]


@dataclass
class ChannelSpec:
    name: str
    kind: str = "text"           # "text" or "voice"
    topic: Optional[str] = None
    # which roles can VIEW this channel (empty = @everyone can view)
    view_roles: list = field(default_factory=list)
    # which roles can SEND messages (empty = same as view)
    send_roles: list = field(default_factory=list)
    # pin these messages in order after the channel is created
    pins: list = field(default_factory=list)


@dataclass
class CategorySpec:
    name: str
    channels: list
    # if set, overrides the view-perms of every channel in this category
    default_view_roles: list = field(default_factory=list)


STRUCTURE: list = [
    CategorySpec(
        "— INFORMATION —",
        [
            ChannelSpec(
                "welcome",
                topic="Start here. Rules + how the server works.",
                send_roles=["Staff", "Owner"],
                pins=[
                    f"""**Welcome to {BRAND}.**

This is the official server for the **{BRAND} Client** for Minecraft 26.2.

**Rules**
1. No racism, slurs, harassment, or NSFW content.
2. No sharing of leaked builds, cracked jars, or other sellers' material.
3. One account per person. Don't ban-evade.
4. Keep drama in DMs. Public spam gets you timed out.
5. Staff decisions are final; appeal in a DM to <@!ownerid-replace-after> only.

**How this server is laid out**
- `INFORMATION` — rules, pricing, FAQ
- `COMMUNITY` — hang out, share clips, suggest features
- `PURCHASE` — how to buy and how to open a ticket
- `SUPPORT` — install help, configs, bug reports (customers only)
- `CUSTOMER AREA` — downloads and changelog (role-gated)

**Website**
{WEBSITE_URL}
"""
                ],
            ),
            ChannelSpec(
                "announcements",
                topic="Updates on new builds, new features, server news.",
                send_roles=["Staff", "Owner"],
            ),
            ChannelSpec(
                "pricing",
                topic="Current pricing and the full buy flow.",
                send_roles=["Staff", "Owner"],
                pins=[
                    f"""**{BRAND} Client — pricing**

```
Daily      $3.99   /  24 hours
Monthly    $10     /  30 days
Lifetime   $15     /  one-time, every future 26.2 build
```

**How to buy**
1. Pick a plan above.
2. Open PayPal and send the amount to:
   `{PAYPAL_EMAIL}` ({PAYPAL_RECIPIENT})
   Use **Friends & Family** when possible to skip the fee.
3. Screenshot the PayPal confirmation.
4. Open a ticket in #open-ticket **or** DM **{SELLER_DISCORD}** with:
   • the screenshot
   • your Minecraft username

Jar + install instructions come back the same day, usually within an hour.

Full feature list and website: {WEBSITE_URL}
"""
                ],
            ),
            ChannelSpec(
                "faq",
                topic="Common questions before you buy.",
                send_roles=["Staff", "Owner"],
                pins=[
                    """**FAQ**

**How long until I get the jar?**
Usually under an hour. Faster when your first DM already has the screenshot + your Minecraft username in it.

**Will it get flagged by anticheat?**
Depends on the server. We ship bypasses for common public anticheats and name them in each module's description. No client can promise permanence — try Daily on a new server first.

**Refunds?**
No refunds on digital access. Daily exists so you can test cheaply before committing to Monthly or Lifetime.

**Why PayPal, not a site checkout?**
Keeps the handoff human and the fees low.

**Can I share my jar?**
No. Each build is keyed to the buyer's Discord. Sharing it burns both accounts.
"""
                ],
            ),
        ],
    ),

    CategorySpec(
        "— COMMUNITY —",
        default_view_roles=["Verified", "Daily", "Monthly", "Lifetime", "Staff", "Owner"],
        channels=[
            ChannelSpec("general",      topic="Main chat."),
            ChannelSpec("pvp-clips",    topic="Post your clips. One clip per message."),
            ChannelSpec("showcase",     topic="Show off configs, HUD setups, themes."),
            ChannelSpec("suggestions",  topic="Suggest new modules or features. Keep it one suggestion per message."),
            ChannelSpec("memes",        topic="Memes."),
            ChannelSpec("voice-chat",   kind="voice"),
        ],
    ),

    CategorySpec(
        "— PURCHASE —",
        channels=[
            ChannelSpec(
                "purchase-info",
                topic="Buy flow and payment info — read before opening a ticket.",
                send_roles=["Staff", "Owner"],
                pins=[
                    f"""**Buying {BRAND} — the short version**

1. **Pay** — PayPal to `{PAYPAL_EMAIL}` ({PAYPAL_RECIPIENT}). Amount from #pricing. F&F if possible.
2. **Screenshot** the PayPal receipt.
3. **Open a ticket** in #open-ticket, or DM **{SELLER_DISCORD}** directly with:
   • the screenshot
   • your Minecraft username

That's it. Jar comes back the same day."""
                ],
            ),
            ChannelSpec(
                "open-ticket",
                topic=f"DM {SELLER_DISCORD} with your PayPal receipt + Minecraft username to receive your build.",
                send_roles=["Staff", "Owner"],
                pins=[
                    f"""**Opening a ticket**

The simplest "ticket" here is a direct DM to **{SELLER_DISCORD}**. Include:

• Your **PayPal receipt** (screenshot).
• Your **Minecraft username**.
• Which plan you paid for (Daily / Monthly / Lifetime).

You'll get the jar + install instructions in that DM thread. If you don't hear back within a few hours, ping an active @Staff role in #general.
"""
                ],
            ),
        ],
    ),

    CategorySpec(
        "— SUPPORT —",
        default_view_roles=["Daily", "Monthly", "Lifetime", "Staff", "Owner"],
        channels=[
            ChannelSpec("install-help",  topic="Can't get the jar to load? Post here."),
            ChannelSpec("configs",       topic="Share and request .cbcfg config profiles."),
            ChannelSpec("bug-reports",   topic="One bug per message. Include: module, server, repro steps."),
        ],
    ),

    CategorySpec(
        "— CUSTOMER AREA —",
        default_view_roles=["Daily", "Monthly", "Lifetime", "Staff", "Owner"],
        channels=[
            ChannelSpec(
                "downloads",
                topic="Current build + install instructions. Role-gated.",
                send_roles=["Staff", "Owner"],
            ),
            ChannelSpec(
                "changelog",
                topic="What changed in each build.",
                send_roles=["Staff", "Owner"],
            ),
            ChannelSpec("vip-chat",      topic="Members-only chat."),
        ],
    ),

    CategorySpec(
        "— STAFF —",
        default_view_roles=["Staff", "Owner"],
        channels=[
            ChannelSpec("staff-chat",     topic="Internal staff chat."),
            ChannelSpec("purchase-log",   topic="Record every incoming payment here (one line per sale)."),
            ChannelSpec("mod-log",        topic="Bans, warnings, ticket closures."),
        ],
    ),
]


# ---------------------------------------------------------------------------
# BOT
# ---------------------------------------------------------------------------
# This script only creates roles, categories, channels, and pins — no member
# or message events are consumed, so default intents are enough and no
# privileged-intent toggle in the Developer Portal is required.
intents = discord.Intents.default()
client = discord.Client(intents=intents)


def perms_view_only(enabled: bool) -> discord.PermissionOverwrite:
    return discord.PermissionOverwrite(view_channel=enabled)


def perms_view_send(view: bool, send: bool) -> discord.PermissionOverwrite:
    return discord.PermissionOverwrite(
        view_channel=view,
        send_messages=send,
        create_public_threads=send,
        send_messages_in_threads=send,
        add_reactions=send,
        attach_files=send,
        embed_links=send,
    )


async def ensure_role(guild: discord.Guild, name: str, color: discord.Color,
                      hoist: bool, mentionable: bool) -> discord.Role:
    existing = discord.utils.get(guild.roles, name=name)
    if existing:
        print(f"  role already exists: {name}")
        return existing
    role = await guild.create_role(
        name=name, color=color, hoist=hoist, mentionable=mentionable,
        reason=f"{BRAND} setup",
    )
    print(f"  + role: {name}")
    return role


async def ensure_category(guild: discord.Guild, name: str,
                          overwrites) -> discord.CategoryChannel:
    existing = discord.utils.get(guild.categories, name=name)
    if existing:
        await existing.edit(overwrites=overwrites, reason=f"{BRAND} setup")
        print(f"  cat already exists: {name}")
        return existing
    cat = await guild.create_category(
        name=name, overwrites=overwrites, reason=f"{BRAND} setup"
    )
    print(f"  + cat: {name}")
    return cat


async def ensure_channel(guild: discord.Guild, cat: discord.CategoryChannel,
                         spec: ChannelSpec, overwrites):
    existing = discord.utils.get(guild.channels, name=spec.name)
    if existing:
        await existing.edit(
            category=cat, topic=spec.topic if spec.kind == "text" else None,
            overwrites=overwrites, reason=f"{BRAND} setup",
        )
        print(f"    ch already exists: {spec.name}")
        return existing
    if spec.kind == "voice":
        ch = await guild.create_voice_channel(
            name=spec.name, category=cat, overwrites=overwrites, reason=f"{BRAND} setup",
        )
    else:
        ch = await guild.create_text_channel(
            name=spec.name, category=cat, topic=spec.topic, overwrites=overwrites,
            reason=f"{BRAND} setup",
        )
    print(f"    + ch: {spec.name}")
    return ch


async def pin_messages(channel: discord.TextChannel, pins: list):
    # Avoid duplicate pinning: only post+pin if no bot pin already matches.
    try:
        existing_pins = await channel.pins()
    except discord.HTTPException:
        existing_pins = []
    existing_bot_pins = {m.content[:80] for m in existing_pins if m.author == client.user}
    for body in pins:
        if body[:80] in existing_bot_pins:
            continue
        msg = await channel.send(body)
        try:
            await msg.pin(reason=f"{BRAND} setup")
        except discord.HTTPException as e:
            print(f"    (pin failed in #{channel.name}: {e})")


@client.event
async def on_ready():
    print(f"logged in as {client.user} ({client.user.id})")
    guild_id = int(sys.argv[2])
    guild = client.get_guild(guild_id) or await client.fetch_guild(guild_id)
    print(f"configuring guild: {guild.name}  ({guild.id})")

    # --- 1. roles ---
    print("roles:")
    role_map: dict = {}
    for name, color, hoist, mentionable in ROLES:
        role_map[name] = await ensure_role(guild, name, color, hoist, mentionable)

    everyone = guild.default_role

    # --- 2. categories + channels ---
    for cat_spec in STRUCTURE:
        # Build category-level overwrites first.
        cat_overwrites = {}
        if cat_spec.default_view_roles:
            cat_overwrites[everyone] = perms_view_only(False)
            for rn in cat_spec.default_view_roles:
                if rn in role_map:
                    cat_overwrites[role_map[rn]] = perms_view_send(True, True)
        else:
            cat_overwrites[everyone] = perms_view_only(True)

        print(f"category: {cat_spec.name}")
        cat = await ensure_category(guild, cat_spec.name, cat_overwrites)

        for ch_spec in cat_spec.channels:
            # Start from category overwrites, then layer channel-specific ones.
            ov = dict(cat_overwrites)

            if ch_spec.view_roles:
                ov[everyone] = perms_view_only(False)
                for rn in ch_spec.view_roles:
                    if rn in role_map:
                        ov[role_map[rn]] = perms_view_send(True, True)

            if ch_spec.send_roles:
                # Determine who can view this channel.
                viewers_source = (
                    ch_spec.view_roles
                    or cat_spec.default_view_roles
                    or None  # None → @everyone can view
                )
                senders = set(ch_spec.send_roles)
                if viewers_source is None:
                    # Everyone views; only listed roles send.
                    ov[everyone] = perms_view_send(True, False)
                    for rn in senders:
                        if rn in role_map:
                            ov[role_map[rn]] = perms_view_send(True, True)
                else:
                    for rn in viewers_source:
                        if rn in role_map:
                            ov[role_map[rn]] = perms_view_send(True, rn in senders)

            ch = await ensure_channel(guild, cat, ch_spec, ov)

            if ch_spec.kind == "text" and ch_spec.pins:
                await pin_messages(ch, ch_spec.pins)

    # --- 3. permanent invite link in the first text channel ---
    print("invite:")
    first_text = next((c for c in guild.text_channels if c.name == "welcome"), None)
    if first_text:
        try:
            inv = await first_text.create_invite(
                max_age=0, max_uses=0, unique=False, reason=f"{BRAND} setup",
            )
            print(f"  permanent invite: {inv.url}")
        except discord.HTTPException as e:
            print(f"  (invite failed: {e})")

    print("done. you can revoke the bot token now.")
    await client.close()


def main():
    if len(sys.argv) != 3:
        print(__doc__)
        sys.exit(2)
    token = sys.argv[1]
    client.run(token)


if __name__ == "__main__":
    main()
