# Level File Format

- use simple text format
    - one line is one thing (block, start/end position, entity)

## File Ending

`.hse`

Pronunciation: dot house

## Version

`v <version_number>`

## Comments

`# my comment here`

## Blocks

`b <x> <y> <z> <tMinX> <tMinY> <tMaxX> <tMaxY> <state>=<value>`

- position
- texture coordinates (min and max)
- block state
    - name of state
    - value of state

## Static Entities

`s <type> <x> <y> <z> <pitch> <yaw>`

## Start/End Position

TODO

## Entities

`e <id> <x> <y> <z>`

- entities that inhabit the level
    - properties per entity or different kinds of entities
