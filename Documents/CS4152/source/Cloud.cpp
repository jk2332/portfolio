//
//  Cloud.cpp
//  Weather Defender (Mac)
//
//  Created by 김지원 on 2/25/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "Cloud.hpp"

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
bool Cloud::init(Poly2 p, Vec2 pos) {
    PolygonObstacle::init(p);
    setPosition(pos);

    setName("cloud");
    setGravityScale(0);
    
    _contacting = false;
    _node = nullptr;
    _centroid  = nullptr;
//    _drawscale = scale;
    _unitNum = 1;
    _isRaining = false;
    _rainCoolDown = 50l;
    _world = nullptr;
    _size = 1.0f;
    _ob = nullptr;
    return true;
}




/**
 * Disposes all resources and assets of this Ragdoll
 *
 * Any assets owned by this object will be immediately released.  Once
 * disposed, a Ragdoll may not be used until it is initialized again.
 */
void Cloud::dispose() {
//    _node = nullptr;
//    _bodies.clear();
    //_bubbler = nullptr;
}


#pragma mark -
#pragma mark Part Initialization

/**
 * Sets the texture for the given body part.
 *
 * As some body parts are symmetrical, we may reuse textures.
 *
 * @param part      The part identifier
 * @param texture   The texture for the given body part
 */
void Cloud::setTexture(const std::shared_ptr<Texture>& texture) {
    _texture = texture;
}

void Cloud::markForRemoval() {
    CULog("cloud to be removed");
    markRemoved(true);
}

#pragma mark -
#pragma mark Physics
/**
 * Updates the object's physics state (NOT GAME LOGIC).
 *
 * This method is called AFTER the collision resolution state. Therefore, it
 * should not be used to process actions or any other gameplay information.
 * Its primary purpose is to adjust changes to the fixture, which have to
 * take place after collision.
 *
 * In other words, this is the method that updates the scene graph.  If you
 * forget to call it, it will not draw your changes.
 *
 * @param delta Timing values from parent loop
 */
void Cloud::update(float delta) {
    Obstacle::update(delta);
    if (_node != nullptr) {
        _node->setPosition(getPosition()*_scale);
        _node->setAngle(getAngle());
    }
    else if (_cloudNode != nullptr) {
        _cloudNode->setPosition(_scale*(getPosition() + Vec2(getWidth()/2.0, getHeight()/2.0)));
        _cloudNode->setAngle(getAngle());
        _cloudNode->ps.update(getPosition()*_scale, delta, 1);
        _shadowNode->setPosition(_shadowNode->getPositionX(), -_scale*_disp);
    }
}

void Cloud::setCloudScale(float s) {
    _scale = s;
}

std::shared_ptr<BoxObstacle> Cloud::getObstacle() {
    return _ob;
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
void Cloud::setSceneNodeParticles(const std::shared_ptr<cugl::CloudNode>& node, float displacement,
                                  std::shared_ptr<Texture> cloudFace, std::shared_ptr<Texture> shadow){
    _cloudNode = node;
    _texture = cloudFace;
    std::shared_ptr<PolygonNode> sprite = PolygonNode::allocWithTexture(cloudFace);
    sprite->setAnchor(Vec2::ANCHOR_CENTER);
    sprite->setContentSize(cloudFace->getSize()*_size);
    sprite->setPosition(_cloudNode->getSize()/2.0f);
    _cloudNode->addChildWithName(sprite, "cloudFace");
    _disp = displacement;
    _shadowNode = PolygonNode::allocWithTexture(shadow);
    _shadowNode->setAnchor(Vec2::ANCHOR_CENTER);
    _shadowNode->setContentSize(shadow->getSize()*_size);
    _shadowNode->setPosition(_cloudNode->getSize()/2.0f - Vec2(0, displacement));
    _cloudNode->addChildWithName(_shadowNode, "shadow");
}

void Cloud::setSceneNode(const std::shared_ptr<cugl::Node>& node){
    _node = node;
}

void Cloud::incSize(float f) {
//    CULog("increased size");
    _size += 0.35;
}

void Cloud::decSize() {
//    CULog("decreased size");
    if (_size > 0.35){
        _size -= 0.35;
    }
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
void Cloud::setDrawScale(float scale) {
    _drawscale = scale;
}
