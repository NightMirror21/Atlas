# Atlas
A totally unique plugin for minecraft's servers for creating territories and markers.
You will be surprised how convenient it is.

## TODO
- [ ] Soft-depend with dynmap
- [ ] Types like 'building zone', 'event place'
- [ ] Checking the occupancy of the territory in real time with output to the action bar
- [ ] More information about the intersection
- [ ] API
- [ ] Placeholders

## Commands
There is a convenient alias and tab completer for almost every command.

Permissions ending with *.player get access only for player functions, but
*.admin get all rights to player like deleting
another marker or edit them.

`/atlas reload` - reload plugin - *atlas.admin*\
`/atlas stats` - statistics - *atlas.admin*\
`/marker create` - create a marker - *marker.player or marker.admin*\
`/marker list own/all/[nickname]` - list of markers - *marker.player or marker.admin*\
`/marker info [id]` - info of marker - *marker.player or marker.admin*\
`/marker remove [id]` - remove marker - *marker.player or marker.admin*\
`/marker edit [id] name/desc` - edit name or description of marker - *marker.player or marker.admin*\
`/territory create` - create a territory - *territory.player or territory.admin*\
`/territory list own/all/[nickname]` - list of territories - *territory.player or territory.admin*\
`/territory info [id]` - info of territory - *territory.player or territory.admin*\
`/territory remove [id]` - remove territory - *territory.player or territory.admin*\
`/territory edit [id] name/desc` - edit name or description of territory - *territory.player or territory.admin*


## Configs
By default there are 3 configs and all are filled in English:
- [config.yml](src/main/resources/config.yml)
- [markers.yml](src/main/resources/markers.yml)
- [territories.yml](src/main/resources/territories.yml)

If you want to install the Russian language, then copy the configs from here:
- [config.yml](src/main/resources/ru/config.yml)
- [markers.yml](src/main/resources/ru/markers.yml)
- [territories.yml](src/main/resources/ru/territories.yml)