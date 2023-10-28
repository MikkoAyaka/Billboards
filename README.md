# Billboards

Billboards gives you the possibility to setup rentable signs. You could for example place a wall of those signs at your protected spawn region and players will be able to rent those signs and edit their text, even though they have no build permission there. That gives them a great possibility to, for example, advertise their goods at a place where many players come along frequently.

## What's changed in fork version

* Use Gradle.
* Depend on ProtocolLib, no longer need to hold a sign to edit.
* Add format code (`&`->`§`) support with permissions.
* You can reload configurations and messages by command.
* Add sign edit GUI.
* Add sign click action. (set `items.action.command` in `config.yml` to empty to disable the function)

## Step by step walk-through

An admin places some signs, for example near the spawn.
He then targets each of those signs (looking at them) and makes them rentable via `/billboards [<price> <duration>]`.
A player comes along and wants to rent one of those signs to, for example, advertise his goods and special deals.
He clicks an available sign, and clicks it again to confirm to rent it for a configurable duration and for a configurable amount of money.
He can then edit his sign by temporarly placing another sign on front of it (sneak+right click on the sign with a sign in hand). This should bypass all protections of other plugins, so that he is not blocked from editing his sign.
He writes his text and when he is done the temporarly placed sign gets removed and the billboard sign behind it adapts the text.
The sign will automatically be reset and made available for others to rent after the setup time (in the config or via the command).
When a rentable sign is right-clicked with anything else but a sign, it shows information about the remaining time and the owner.
An admin can remove those signs again by crouching and breaking it.

## Commands

* `/billboard [<price> <duration>] [creator]` - let's you define/create billboard signs (rentable signs), optionally with custom price and duration in days, and a creator playername (for admins to create player signs for themself or other players)
* `/billboard reload` - reload configuration and messages

## Permissions

| Permission             | Detail                                                  | Default |
|------------------------|---------------------------------------------------------|---------|
| billboards.admin       | Can add new billboard signs, and can remove them again. | op      |
| billboards.sign.color  | Can use color code on signs.                            | false   |
| billboards.sign.format | Can use format code on signs.                           | false   |
| billboards.sign.magic  | Can use magic code (§k) on signs.                       | false   |
| billboards.rent        | Can rent billboard signs and edit those.                | true    |
| billboards.create      | Can create player billboard signs.　                     | false　  |

**`billboards.create` is experimental. Read about the current flaws below!**

## The player signs are **experimental**

The plugin has no sign protection built-in yet (signs can be broken, if they are not in some sort of protected region (this can also be abused by players selling signs in their land and breaking them afterwards)) and players (with the billboards.create permission) can currently transform ANY sign EVERYWHERE to a player-billboard-sign.
These disadvantages make the player signs currently impractical.

Things that have to be added in the future in order for the player signs to properly work:

* sign protection
* player sign creation only at places where the players has build permission

## Configuration

| Configuration               | Default Value | Detail                                                                                                                                                      |
|-----------------------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| default-price               | 10            | The default price a player has to pay for renting a sign.                                                                                                   |
| default-duration-in-days    | 7             | All signs are by default rented for a week.                                                                                                                 |
| max-billboards-per-player   | -1            | The max number of signs a player can rent at the same time (-1 for no limit).                                                                               |
| bypass-sign-change-blocking | true          | Whether we should bypass other plugins which attempt to prevent sign editing (eg. protection plugins, but this also applies to anti swearing plugins etc.). |

All messages are configurable and translatable.
This plugin requires [ProtocolLib](https://github.com/dmulloy2/ProtocolLib/), [Vault](https://github.com/MilkBowl/Vault) for the economy interaction.

## Languages

Please check [lang](/lang) folder.

If there are not your language, PRs from [messages.yml](/lang/messages.yml) welcome.

## Links

* BukkitDev (Original): https://dev.bukkit.org/projects/billboards/