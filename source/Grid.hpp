//
//  Grid.hpp
//  RagdollDemo (Mac)
//
//  Created by 김지원 on 3/8/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef Grid_hpp
#define Grid_hpp

#include <stdio.h>
#include <cugl/cugl.h>
#include <vector>

/** Width of the game world in Box2d units */
#define DEFAULT_WIDTH   32.0f
/** Height of the game world in Box2d units */
#define DEFAULT_HEIGHT  18.0f
#define GRID_WIDTH      3
#define GRID_HEIGHT     2
#define UP_LEFT_CORNER_X    3
#define UP_LEFT_CORNER_Y    9
#define OFFSET_X         0
#define OFFSET_Y         0


#pragma mark -
#pragma mark Ragdoll
/**
 * A ragdoll whose body parts are boxes connected by joints
 *
 * Note that this module handles its own scene graph management.  Since a
 * ComplexObstacle owns all of its child obstacles, it is natural for it to
 * own the corresponding as well scene graph.
 *
 * For the construction, see the ragdoll diagram above, with the position offsets.
 */
class Grid {
private:
    /** This macro disables the copy constructor (not allowed on scene graphs) */
    CU_DISALLOW_COPY_AND_ASSIGN(Grid);
    
protected:
    float _drawscale;
    std::shared_ptr<cugl::Texture> _texture;
    int _x;
    int _y;
    
    
public:
#pragma mark -
#pragma mark Constructors
    /**
     * Creates a new Ragdoll at the origin.
     *
     * NEVER USE A CONSTRUCTOR WITH NEW. If you want to allocate a model on
     * the heap, use one of the static constructors instead.
     */
    Grid(void) { }
    
    /**
     * Destroys this Ragdoll, releasing all resources.
     */
    virtual ~Grid(void) { dispose(); }
    
    /**
     * Disposes all resources and assets of this Ragdoll
     *
     * Any assets owned by this object will be immediately released.  Once
     * disposed, a Ragdoll may not be used until it is initialized again.
     */
    void dispose();
    
    /**
     * Initializes a new Ragdoll with the given position and scale
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @param pos   Initial position in world coordinates
     * @param scale The drawing scale to convert world to screen coordinates
     *
     * @return  true if the obstacle is initialized properly, false otherwise.
     */
    bool init(float scale, std::shared_ptr<cugl::Texture> texture, int x, int y);
    
    
#pragma mark -
#pragma mark Static Constructors
    /**
     * Returns a newly allocated Ragdoll with the given position and scale
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @param pos   Initial position in world coordinates
     * @param scale The drawing scale to convert world to screen coordinates
     *
     * @return a newly allocated Ragdoll with the given position
     */
    static std::shared_ptr<Grid> alloc(float scale, std::shared_ptr<cugl::Texture> texture, int x, int y) {
        std::shared_ptr<Grid> result = std::make_shared<Grid>();
        return (result->init(scale, texture, x, y) ? result : nullptr);
    }
    
    cugl::Vec2 gridCoordToPosition(){
        return cugl::Vec2((UP_LEFT_CORNER_X + (GRID_WIDTH + OFFSET_X)*_x + GRID_WIDTH/2)*_drawscale, (-(GRID_HEIGHT + OFFSET_Y)*_y + UP_LEFT_CORNER_Y + GRID_HEIGHT/2)*_drawscale);
    }
    
    
#pragma mark -
#pragma mark Animation
    
    /**
     * Sets the scene graph node representing this Ragdoll.
     *
     * Note that this method also handles creating the nodes for the body parts
     * of this Ragdoll. Since the obstacles are decoupled from the scene graph,
     * initialization (which creates the obstacles) occurs prior to the call to
     * this method. Therefore, to be drawn to the screen, the nodes of the attached
     * bodies must be added here.
     *
     * The bubbler also uses the world node when adding bubbles to the scene, so
     * the input node must be added to the world BEFORE this method is called.
     *
     * By storing a reference to the scene graph node, the model can update
     * the node to be in sync with the physics info. It does this via the
     * {@link Obstacle#update(float)} method.
     *
     * @param node  The scene graph node representing this Ragdoll, which has been added to the world node already.
     */
    void setSceneNode(const std::shared_ptr<cugl::Node>& node);
    
    /**
     * Sets the ratio of the Ragdoll sprite to the physics body
     *
     * The Ragdoll needs this value to convert correctly between the physics
     * coordinates and the drawing screen coordinates.  Otherwise it will
     * interpret one Box2D unit as one pixel.
     *
     * All physics scaling must be uniform.  Rotation does weird things when
     * attempting to scale physics by a non-uniform factor.
     *
     * @param scale The ratio of the Ragdoll sprite to the physics body
     */
    void setDrawScale(float scale);
    


};

#endif
