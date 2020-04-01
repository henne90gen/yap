# Yet Another Portal

## TODOs

- game does not terminate correctly after pressing ESC

- complete physics system
- level file format
- level generator (Max)
- level editor (Henne)
- top down camera (try both)
    - direct control
    - move only when player gets too close to edge
- path finding
    - along the voxel grid

## Story

- house at night
- try to get to the fridge to get food
- don't get caught by your parents
- otherwise you are grounded for two weeks
- scenarios
    - normal house
    - orphanage
    - hospital?
    - prison?
    - forest?
    - parking lot? (fast cars that can run you over)

## Entity Component System

- Entity
    - a collection of components that represents a thing in the game
- Component
    - a piece of data
- System
    - logic that operates on a certain set of entities
- Capability
    - collection of components
    - systems require capabilities from entities
    - entities inherently have capabilities, because they consist of components

Operations that should be possible

- iterate over all entities
- iterate over all entities that have a certain capability
- remove an entity and associated entry from the lists in entityMap
    - remove from entityList
    - find out which capabilities in capabilityMap match the components of the entity and delete the entity from those lists
- add entity
    - add components
    - iterate all capabilities in capabilityMap and add it to the ones that have matching components
- add component to entity
    - check that component does not exist already
    - just add component
    - check capabilityMap for new matching capabilities and add the entity to those lists
- remove component from entity
    - similar to removing an entity
