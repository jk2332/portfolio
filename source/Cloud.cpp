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
bool Cloud::init(const Vec2& pos, float scale) {
    Obstacle::init(pos);
    
    setName("cloud");
    setGravityScale(0);

    _contacting = false;
    _node = nullptr;
    _centroid  = nullptr;
    _drawscale = scale;
    _unitNum = 1;
    _isRaining = false;
    _rainCoolDown = 50l;
    _world = nullptr;
    _size = .8f;
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
    _node = nullptr;
//    _bodies.clear();
    //_bubbler = nullptr;
}


#pragma mark -
#pragma mark Part Initialization
/**
 * Creates the individual body parts for this ragdoll
 *
 * The size of the body parts is determined by the scale together with
 * the assets (as part of the asset manager).  This will fail if any
 * body part assets are missing.
 *
 * @param assets The program asset manager
 *
 * @return true if the body parts were successfully created
 */
bool Cloud::initialBuild(const std::shared_ptr<AssetManager>& assets) {
//    CUAssertLog(_bodies.empty(), "Bodies are already initialized");
    
    // Get the images from the asset manager
    bool success = true;
    for(int ii = 0; ii < _unitNum; ii++) {
        //std::string name = getPartName(ii);
        std::shared_ptr<Texture> image = assets->get<Texture>("cloud");
        if (image == nullptr) {
            success = false;
        } else {
            _texture = image;
        }
    }
    if (!success) {
        return false;
    }
    
    // Now make everything
//    std::shared_ptr<BoxObstacle> part;
    
    // TORSO
    Vec2 pos = getPosition();
//    part = makeUnit(BODY, PART_NONE, pos);
//    part->setFixedRotation(true);
    
    Size size = _texture->getSize();
    size.width /= (_drawscale*1.5);
    size.height /= (_drawscale*1.5);
    
    std::shared_ptr<BoxObstacle> body = BoxObstacle::alloc(pos, size);
    body->setDensity(DEFAULT_DENSITY);
    CULog("created");
    
    //    _bodies.push_back(body);
    _ob = body;
    
    return true;
}

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
    CULog("remove is? %i", isRemoved());
}


/**
 * Returns a single body part
 *
 * While it looks like this method "connects" the pieces, it does not really.
 * It puts them in position to be connected by joints, but they will fall apart
 * unless you make the joints.
 *
 * @param  part     Part to create
 * @param  connect  Part to connect it to
 * @param  pos      Position RELATIVE to connecting part
 *
 * @return the created body part
 */
std::shared_ptr<BoxObstacle> Cloud::makeUnit(int part, int connect, const Vec2& pos) {
    std::shared_ptr<Texture> image = _texture;
    Size size = image->getSize();
    size.width /= (_drawscale*1.5);
    size.height /= (_drawscale*1.5);
    
    Vec2 pos2 = pos;
    
    std::shared_ptr<BoxObstacle> body = BoxObstacle::alloc(pos2, size);
    body->setDensity(DEFAULT_DENSITY);
    
//    _bodies.push_back(body);
    _ob = body;
    return body;
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
        std::vector<std::shared_ptr<Node>> children = _node->getChildren();
        // Update the nodes of the attached bodies
        for (auto it = children.begin(); it != children.end(); ++it) {
//            CULog("iter");
//            CULog("%f, %f pos", _ob->getPosition().x, _ob->getPosition().y);
            (*it)->setPosition(_ob->getPosition()*_drawscale);
            (*it)->setContentSize(_texture->getSize() * _size);
            
            // Propagate the update to the bodies attached to the Ragdoll
            _ob->update(delta);
        }
    }
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
void Cloud::setSceneNode(const std::shared_ptr<cugl::Node>& node){
    _node = node;
    std::shared_ptr<Texture> image = _texture;
    std::shared_ptr<PolygonNode> sprite = PolygonNode::allocWithTexture(image);
    sprite->setContentSize(_texture->getSize()*_size);
    _node->addChildWithName(sprite, "cloud");
}

void Cloud::incSize(float f) {
    CULog("increased size");
    _size += 0.35 + f;
}

void Cloud::decSize() {
    CULog("decreased size");
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


