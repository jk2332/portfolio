//
//  Grid.cpp
//  RagdollDemo (Mac)
//
//  Created by 김지원 on 3/8/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "Grid.hpp"

#include <Box2D/Dynamics/Joints/b2RevoluteJoint.h>
#include <Box2D/Dynamics/Joints/b2WeldJoint.h>
#include <Box2D/Dynamics/b2World.h>

using namespace cugl;

#pragma mark -
#pragma mark Animation and Physics Constants

/** This is adjusted by screen aspect ratio to get the height */
#define GAME_WIDTH 1024

#pragma mark -
#pragma mark Constructors

/**
 * Initializes a new Ragdoll with the given position
 *
 * The Ragdoll is 1 unit by 1 unit in size. The Ragdoll is scaled so that
 * 1 pixel = 1 Box2d unit
 *
 * The scene graph is completely decoupled from the physics system.
 * The node does not have to be the same size as the physics body. We
 * only guarantee that the scene graph node is positioned correctly
 * according to the drawing scale.
 *
 * @param  pos        Initial position in world coordinates
 * @param  scale    The drawing scale to convert between world and screen coordinates
 *
 * @return  true if the obstacle is initialized properly, false otherwise.
 */
bool Grid::init(float scale, std::shared_ptr<Texture> texture, int x, int y) {
    _drawscale = scale;
    _texture = texture;
    _x = x;
    _y = y;
    
    return true;
}

/**
 * Disposes all resources and assets of this Ragdoll
 *
 * Any assets owned by this object will be immediately released.  Once
 * disposed, a Ragdoll may not be used until it is initialized again.
 */
void Grid::dispose() {
    _texture = nullptr;
}



#pragma mark -
#pragma mark Scene Graph Management

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
void Grid::setSceneNode(const std::shared_ptr<cugl::Node>& node){
    std::shared_ptr<PolygonNode> grid_node = PolygonNode::allocWithTexture(_texture);
    grid_node->setPosition((UP_LEFT_CORNER_X+(GRID_WIDTH+OFFSET_X)*_x)*_drawscale, ((GRID_HEIGHT+OFFSET_Y)*_y+UP_LEFT_CORNER_Y)*_drawscale);
    grid_node->setContentSize(GRID_WIDTH*_drawscale, GRID_HEIGHT*_drawscale);
    node->addChildWithName(grid_node, "grid"+std::to_string(_x)+std::to_string(_y));
}


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
void Grid::setDrawScale(float scale) {
    _drawscale = scale;
}


