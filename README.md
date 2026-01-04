# ObsidianBreakerModern

A modern obsidian breaker plugin for Minecraft 1.21+ Paper/Spigot servers. Perfect for Factions servers!

## 🚀 Quick Start - Get Compiled JAR

### Option 1: GitHub Auto-Build (Easiest!)
1. Create a new GitHub repository
2. Upload all these files to it
3. Go to "Actions" tab
4. Click "Build Plugin" → "Run workflow"
5. Download the compiled jar from "Artifacts"!

### Option 2: Build Locally
```bash
# Requires Java 17+ and Maven
mvn clean package
# JAR will be in target/
```

## Features

- ✅ **Break any hard block with explosions** - Obsidian, Ancient Debris, Netherite, and more!
- ✅ **Check Tool** - Right-click blocks with a stick to see their durability!
- ✅ **All 1.21 blocks** - Copper, Deepslate, Tuff, Sculk, Blackstone, Reinforced Deepslate
- ✅ **Damage persistence** - Block damage saves across server restarts
- ✅ **Visual feedback** - Particle effects when blocks take damage
- ✅ **Configurable** - Set durability for any block

## Commands

| Command | Description |
|---------|-------------|
| `/ob tool` | Toggle check tool mode (right-click to check blocks) |
| `/ob check` | Check block you're looking at |
| `/ob settool <item>` | Change check tool item (default: STICK) |
| `/ob list` | List all configured blocks |
| `/ob stats` | Show plugin statistics |
| `/ob reload` | Reload configuration |
| `/ob clear` | Clear all damage data |

## Check Tool Usage

1. Run `/ob tool` to enable check tool mode
2. Hold a **stick** (or your configured item)
3. **Right-click** any block to see its durability!

You'll see:
```
══════ Block Info ══════
Block: OBSIDIAN
Health: ████████████████████ 100%
Damage: 0/5
Hits Left: 5
════════════════════════
```

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `obsidianbreaker.check` | Use check tool and /ob check | true |
| `obsidianbreaker.reload` | Reload config | op |
| `obsidianbreaker.settool` | Change check tool item | op |
| `obsidianbreaker.clear` | Clear damage data | op |
| `obsidianbreaker.checktool.always` | Always have check tool on | op |

## Default Block Durabilities

| Block | Hits to Break |
|-------|---------------|
| Obsidian | 5 |
| Crying Obsidian | 5 |
| Ancient Debris | 8 |
| Netherite Block | 10 |
| Reinforced Deepslate | 15 |
| Ender Chest | 3 |
| Enchanting Table | 3 |
| All Copper variants | 2 |
| All Deepslate variants | 2 |

## Configuration

```yaml
settings:
  explosion-radius: 3
  show-cracks: true
  persist-damage: true
  underwater-explosions: true
  check-tool:
    enabled: true
    item: STICK  # Change with /ob settool

durability:
  OBSIDIAN: 5
  CRYING_OBSIDIAN: 5
  ANCIENT_DEBRIS: 8
  NETHERITE_BLOCK: 10
  # ... many more blocks configured!
```

## Installation

1. Download `ObsidianBreakerModern.jar`
2. Put in your `plugins` folder
3. Restart server
4. Configure in `plugins/ObsidianBreakerModern/config.yml`

## Support

- Paper 1.21+
- Spigot 1.21+

## License

MIT License
