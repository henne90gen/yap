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

`b <id> <x> <y> <z> <state>=<value>`
`b 7 11 12 13 someProperty=123`

- block type id
- position
- block state
    - name of state
    - value of state

## Static Entities

`s <type> <x> <y> <z> <pitch> <yaw>`
`s 7 11 12 13 rotation=90`

## Start/End Position

`sp <x> <y> <z>`
`ep <x> <y> <z>`

- start position
- end position

## Entities

`e <id> <x> <y> <z>`

- entities that inhabit the level
    - properties per entity or different kinds of entities
