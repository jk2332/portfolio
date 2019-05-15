//
//  Cloud.cpp
//  Weather Defender (Mac)
//
//  Created by 김지원 on 2/25/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "Cloud.hpp"
#include <math.h>
#include <Box2D/Dynamics/Joints/b2RevoluteJoint.h>
#include <Box2D/Dynamics/Joints/b2WeldJoint.h>
#include <Box2D/Dynamics/b2World.h>


using namespace cugl;

#pragma mark -
#pragma mark Animation and Physics Constants

/** This is adjusted by screen aspect ratio to get the height */
#define SCALE   0.5

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
    _disp = Vec2::ZERO;
    _contacting = false;
    _centroid  = nullptr;
     _drawscale = 1.0f;
    _isRainCloud = true;
    _unitNum = 1;
    _isRaining = false;
    _rainCoolDown = 50l;
    _world = nullptr;
    _targetPos = pos;
    _cloudSizeScale = 1;
    _ob = nullptr;

    _actions = ActionManager::alloc();
    _actions2 = ActionManager::alloc();
    _rain = Animate::alloc(1, 18, 1.0f, 1);
    _lightning = Animate::alloc(0, 11, 1.0f, 1);
    return true;
}

/**
 * Disposes all resources and assets of this Ragdoll
 *
 * Any assets owned by this object will be immediately released.  Once
 * disposed, a Ragdoll may not be used until it is initialized again.
 */
void Cloud::dispose() {
    CULog("cloud disposed");
    _texture = nullptr;
    _cloudNode = nullptr;
    _shadowNode = nullptr;
    _ob = nullptr;
    _actions = nullptr;
    _actions2 = nullptr;
    _rain = nullptr;
    _lightning = nullptr;
    if (_rain_node != nullptr){
        _rain_node->removeFromParent();
        _rain_node = nullptr;
    }
    if (_lightning_node != nullptr){
        _lightning_node->removeFromParent();
        _lightning_node = nullptr;
    }
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
//    CULog("cloud to be removed");
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
    if (_cloudNode != nullptr) {
        _cloudNode->setPosition(_drawscale*(getPosition() + Vec2(getWidth()/2.0f, getHeight()/2.0f)));
        _cloudNode->setAngle(getAngle());
        _cloudNode->ps.update(getPosition()*_drawscale, delta, _cloudSizeScale, _isRaining);
        shared_ptr<Node> faceSprite = _cloudNode->getChild(0);
        _shadowNode->setContentSize(_shadowNode->getTexture()->getSize()*_cloudSizeScale);
        _shadowNode->setPosition(faceSprite->nodeToWorldCoords(faceSprite->getPosition()
                                                               + faceSprite->getSize()/2.0f)
                                 + _disp);
        _lightning_node->setScale(0.35f*_cloudSizeScale);
        _lightning_node->setPosition(faceSprite->nodeToWorldCoords(faceSprite->getPosition()
                                                               + faceSprite->getSize()/2.0f)
                                 + _disp);
        _rain_node->setScale(0.35f*_cloudSizeScale);
        _rain_node->setPosition(faceSprite->nodeToWorldCoords(faceSprite->getPosition()
                                                               + faceSprite->getSize()/2.0f)
                                 + _disp);
    }

    _actions2->update(delta);
    if (_isRaining) {
        _actions->update(delta);
        setCloudSizeScale(_cloudSizeScale*sqrt(9.985/10.0f));
        _actions->activate("current", _rain, _rain_node);
    }
}

void Cloud::setLightning() {
    // Needs to be raining to lightning
     _actions2->activate("current", _lightning, _lightning_node);
}


void Cloud::toggleRain() {
    setCloudSizeScale(_cloudSizeScale);
     if (_isRaining) {
        //  CULog("undo rain");
         _rain_node->setVisible(false);
        _rain_node->setFrame(0);
        _isRaining = false;
    } else if (_isRainCloud){
        // CULog("do rain");
        _rain_node->setVisible(true);
        _actions->activate("current", _rain, _rain_node);
        _isRaining = true;
    }
}

void Cloud::setScale(float s) {
    _scale = s;
}

bool Cloud::shadowCheck(shared_ptr<Node> worldNode, shared_ptr<Node> gridNode){
    Vec2 sc = worldNode->nodeToWorldCoords(_shadowNode->getPosition());
    Vec2 gridPos = worldNode->nodeToWorldCoords(gridNode->getPosition());
    float a = _shadowNode->getWidth()/2;
    float b = _shadowNode->getHeight()/2;
    
    int p = (pow((gridPos.x - sc.x), 2) / pow(a, 2)) + (pow((gridPos.y - sc.y), 2) / pow(b, 2));
    //inside
    if (p < 1){
//        gridNode->setColor(Color4(255, 0, 0));
        return true;
        
    }
    //outside
    else{return false;}
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
 *
 * RETURNS THE POINTER TO THIS CLOUD'S SHADOW. ASSUME CALLER WILL ADD SHADOW TO THE WORLD
 */
vector<shared_ptr<Node>> Cloud::setSceneNode(const shared_ptr<cugl::CloudNode>& node,Vec2 displacement, shared_ptr<Texture> cloudFace, shared_ptr<Texture> shadow, shared_ptr<Texture> rain, shared_ptr<Texture> lightning){
    vector<shared_ptr<Node>> nodesToReturn;
    _cloudNode = node;
    _texture = cloudFace;
    shared_ptr<PolygonNode> sprite = PolygonNode::allocWithTexture(cloudFace);
    sprite->setAnchor(Vec2::ANCHOR_CENTER);
    sprite->setContentSize(cloudFace->getSize());
    sprite->setPosition(_cloudNode->getSize()/2.0f);
    //don't need zordering for the cloud face
    _cloudNode->addChildWithName(sprite, "cloudFace");
    _disp = displacement;
    _shadowNode = PolygonNode::allocWithTexture(shadow);
    _shadowNode->setContentSize(_shadowNode->getTexture()->getSize()*_cloudSizeScale);
    _shadowNode->setPosition(_cloudNode->getPosition() + _cloudNode->getSize()/2.0f + displacement);
    _shadowNode->setName("shadow");

    // Set lightning animation
    _lightning_node = AnimationNode::alloc(lightning, 1, 12);
    _lightning_node->setAnchor(Vec2::ANCHOR_BOTTOM_CENTER);
    _lightning_node->setScale(0.5f*_cloudSizeScale);
    _lightning_node->setPosition(_cloudNode->getPosition() + _cloudNode->getSize()/2.0f + displacement);
    _lightning_node->setName("lightning");
    
    // Set rain animation
    _rain_node = AnimationNode::alloc(rain, 1, 20);
    _rain_node->setAnchor(Vec2::ANCHOR_BOTTOM_CENTER);
    _rain_node->setScale(0.5f*_cloudSizeScale);
    _rain_node->setPosition(_cloudNode->getPosition() + _cloudNode->getSize()/2.0f + displacement);
    _rain_node->setName("rainAnimation");

    //rely on caller to add shadow, lightning, and rain nodes to the world
    nodesToReturn.push_back(_shadowNode);
    nodesToReturn.push_back(_lightning_node);
    nodesToReturn.push_back(_rain_node);
    return nodesToReturn;
}

void Cloud::setCloudSizeScale(float s) {
     if (s >= 1) {
        _isRainCloud = true;
    }
    else {
        _isRainCloud = false;
        if (_isRaining) {
            _rain_node->setFrame(0);
            _rain_node->setVisible(false);
            _isRaining = false;
        }
        _cloudSizeScale = s;
    }
    if (s < 0.5) {
        
    }
    else if (s > 5){
        _cloudSizeScale = 5;
    }
    else {
        _cloudSizeScale = s;
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
void Cloud::setDrawScale(float drawscale) {
    _drawscale = drawscale;
}

