//
//  Cloud.cpp
//  RagdollDemo (Mac)
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
    
    std::string name("ragdoll");
    setName(name);
    
    _node = nullptr;
    _centroid  = nullptr;
    _drawscale = scale;
    _unitNum = 2;
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
    _bodies.clear();
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
    CUAssertLog(_bodies.empty(), "Bodies are already initialized");
    
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
    std::shared_ptr<BoxObstacle> part;
    
    // TORSO
    Vec2 pos = getPosition();
    part = makeUnit(BODY, PART_NONE, pos);
    part->setFixedRotation(true);
    
//    // HEAD
//    makeUnit(UP, BODY, Vec2(0, TORSO_OFFSET));
    // ARMS
    part = makeUnit(LEFT, BODY, Vec2(-ARM_XOFFSET, ARM_YOFFSET));
    part->setFixedRotation(true);
//    makeUnit(RIGHT, BODY, Vec2(ARM_XOFFSET, ARM_YOFFSET));
//    makeUnit(DOWN, BODY, Vec2(0, -TORSO_OFFSET));
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
//    if (_textures.size() <= 4) {
//        _textures.resize(PART_RIGHT_SHIN+1, nullptr);
//    }
//    _textures[part] = texture;
    _texture = texture;
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
    size.width /= (_drawscale*2);
    size.height /= (_drawscale*2);
    
    Vec2 pos2 = pos;
    if (connect != PART_NONE) {
        pos2 += _bodies[connect]->getPosition();
    }
    
    std::shared_ptr<BoxObstacle> body = BoxObstacle::alloc(pos2, size);
    //body->setName(getPartName(part));
    body->setDensity(DEFAULT_DENSITY);
    
    _bodies.push_back(body);
    return body;
}

bool Cloud::dropUnit(b2World& world){
    if (_unitNum > 1){
        _unitNum -= 1;
        world.DestroyJoint(_joints[0]);
        _joints.clear();
        return true;
    }
    return false;
}

bool Cloud::joinUnit(b2World& world){
    CULog("Join clouds");
    _unitNum += 1;
    Cloud::createJoints(world);
//    b2RevoluteJointDef jointDef;
//    b2Joint* joint;
//    jointDef.bodyA = _bodies[LEFT]->getBody();
//    jointDef.bodyB = _bodies[BODY]->getBody();
//    jointDef.localAnchorA.Set(ARM_XOFFSET / 2, 0);
//    jointDef.localAnchorB.Set(-ARM_XOFFSET / 2, ARM_YOFFSET);
//    jointDef.enableLimit = true;
//    jointDef.upperAngle = 0;
//    jointDef.lowerAngle = 0;
//    joint = world.CreateJoint(&jointDef);
//    _joints.push_back(joint);
//    b2WeldJointDef weldDef;
//
//    // Weld center of mass to torso
//    weldDef.bodyA = _bodies[BODY]->getBody();
//    weldDef.bodyB = _body;
//    weldDef.localAnchorA.Set(0, 0);
//    weldDef.localAnchorB.Set(0, 0);
//    joint = world.CreateJoint(&weldDef);
//    _joints.push_back(joint);
    return true;
}

///**
// * Returns the texture key for the given body part.
// *
// * As some body parts are symmetrical, we reuse textures.
// *
// * @return the texture key for the given body part
// */
//std::string RagdollModel::getPartName(int part) {
//    switch (part) {
//        case PART_HEAD:
//            return HEAD_TEXTURE;
//        case PART_BODY:
//            return BODY_TEXTURE;
//        case PART_LEFT_ARM:
//        case PART_RIGHT_ARM:
//            return ARM_TEXTURE;
//        case PART_LEFT_FOREARM:
//        case PART_RIGHT_FOREARM:
//            return FOREARM_TEXTURE;
//        case PART_LEFT_THIGH:
//        case PART_RIGHT_THIGH:
//            return THIGH_TEXTURE;
//        case PART_LEFT_SHIN:
//        case PART_RIGHT_SHIN:
//            return SHIN_TEXTURE;
//        default:
//            return "UNKNOWN";
//    }
//}

///**
// * Creates the bubble generator for this ragdoll
// *
// * The bubble generator will be offset at the snorkel on the head.
// *
// * @param texture   The texture for an individual bubble
// */
//void Cloud::makeBubbleGenerator(const std::shared_ptr<Texture>& bubble) {
//    CUAssertLog(_bubbler == nullptr, "Bubbler is already initialized");
//    CUAssertLog(_bodies.size() > PART_RIGHT_SHIN, "Bodies must be initialized before adding bubbler");
//
//    Vec2 offpos = (Vec2)(BUBB_OFF)+getPosition();
//    _bubbler = BubbleGenerator::alloc(bubble, offpos);
//    _bubbler->setDrawScale(_drawscale);
//    _bodies.push_back(_bubbler);
//}



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
        int i = 0;
        
        // Update the nodes of the attached bodies
        for (auto it = children.begin(); it != children.end(); ++it) {
            (*it)->setPosition(_bodies[i]->getPosition()*_drawscale);
            (*it)->setAngle(_bodies[i]->getAngle());
            
            // Propagate the update to the bodies attached to the Ragdoll
            _bodies[i]->update(delta);
            i++;
        }
    }
}

/**
 * Creates the joints for this object.
 *
 * This method is executed as part of activePhysics. This is the primary method to
 * override for custom physics objects.
 *
 * @param world Box2D world to store joints
 *
 * @return true if object allocation succeeded
 */
bool Cloud::createJoints(b2World& world) {
    CULog("Create joints calleds");

    
    b2RevoluteJointDef jointDef;
    b2Joint* joint;
    
    
//    // NECK JOINT
//    jointDef.bodyA = _bodies[UP]->getBody();
//    jointDef.bodyB = _bodies[BODY]->getBody();
//    jointDef.localAnchorA.Set(0, (-TORSO_OFFSET) / 2);
//    jointDef.localAnchorB.Set(0, (TORSO_OFFSET) / 2);
//    jointDef.enableLimit = true;
//    jointDef.upperAngle = M_PI / 2.0f;
//    jointDef.lowerAngle = -M_PI / 2.0f;
//    joint = world.CreateJoint(&jointDef);
//    _joints.push_back(joint);
//
//    jointDef.bodyA = _bodies[DOWN]->getBody();
//    jointDef.bodyB = _bodies[BODY]->getBody();
//    jointDef.localAnchorA.Set(0, (TORSO_OFFSET) / 2);
//    jointDef.localAnchorB.Set(0, (-TORSO_OFFSET) / 2);
//    jointDef.enableLimit = true;
//    jointDef.upperAngle = M_PI / 2.0f;
//    jointDef.lowerAngle = -M_PI / 2.0f;
//    joint = world.CreateJoint(&jointDef);
//    _joints.push_back(joint);
    
    // SHOULDERS
    jointDef.bodyA = _bodies[LEFT]->getBody();
    jointDef.bodyB = _bodies[BODY]->getBody();
    jointDef.localAnchorA.Set(ARM_XOFFSET / 2, 0);
    jointDef.localAnchorB.Set(-ARM_XOFFSET / 2, ARM_YOFFSET);
    jointDef.enableLimit = true;
    jointDef.upperAngle = 0;
    jointDef.lowerAngle = 0;
    joint = world.CreateJoint(&jointDef);
    _joints.push_back(joint);

    
//    jointDef.bodyA = _bodies[RIGHT]->getBody();
//    jointDef.bodyB = _bodies[BODY]->getBody();
//    jointDef.localAnchorA.Set(-ARM_XOFFSET / 2, 0);
//    jointDef.localAnchorB.Set(ARM_XOFFSET / 2, ARM_YOFFSET);
//    jointDef.enableLimit = true;
//    jointDef.upperAngle = 0;
//    jointDef.lowerAngle = 0;
//    joint = world.CreateJoint(&jointDef);
//    _joints.push_back(joint);
    
//    // ELBOWS
//    jointDef.bodyA = _bodies[PART_LEFT_FOREARM]->getBody();
//    jointDef.bodyB = _bodies[PART_LEFT_ARM]->getBody();
//    jointDef.localAnchorA.Set(FOREARM_OFFSET / 2, 0);
//    jointDef.localAnchorB.Set(-FOREARM_OFFSET / 2, 0);
//    jointDef.enableLimit = true;
//    jointDef.upperAngle = M_PI / 2;
//    jointDef.lowerAngle = -M_PI / 2;
//    joint = world.CreateJoint(&jointDef);
//    _joints.push_back(joint);
//
//    jointDef.bodyA = _bodies[PART_RIGHT_FOREARM]->getBody();
//    jointDef.bodyB = _bodies[PART_RIGHT_ARM]->getBody();
//    jointDef.localAnchorA.Set(-FOREARM_OFFSET / 2, 0);
//    jointDef.localAnchorB.Set(FOREARM_OFFSET / 2, 0);
//    jointDef.enableLimit = true;
//    jointDef.upperAngle = M_PI / 2;
//    jointDef.lowerAngle = -M_PI / 2;
//    joint = world.CreateJoint(&jointDef);
//    _joints.push_back(joint);
    
//    // HIPS
//    jointDef.bodyA = _bodies[PART_LEFT_THIGH]->getBody();
//    jointDef.bodyB = _bodies[PART_BODY]->getBody();
//    jointDef.localAnchorA.Set(0, THIGH_YOFFSET / 2);
//    jointDef.localAnchorB.Set(-THIGH_XOFFSET, -THIGH_YOFFSET / 2);
//    jointDef.enableLimit = true;
//    jointDef.upperAngle = M_PI / 2;
//    jointDef.lowerAngle = -M_PI / 2;
//    joint = world.CreateJoint(&jointDef);
//    _joints.push_back(joint);
//
//    jointDef.bodyA = _bodies[PART_RIGHT_THIGH]->getBody();
//    jointDef.bodyB = _bodies[PART_BODY]->getBody();
//    jointDef.localAnchorA.Set(0, THIGH_YOFFSET / 2);
//    jointDef.localAnchorB.Set(THIGH_XOFFSET, -THIGH_YOFFSET / 2);
//    jointDef.enableLimit = true;
//    jointDef.upperAngle = M_PI / 2;
//    jointDef.lowerAngle = -M_PI / 2;
//    joint = world.CreateJoint(&jointDef);
//    _joints.push_back(joint);
//
//    // KNEES
//    jointDef.bodyA = _bodies[PART_LEFT_THIGH]->getBody();
//    jointDef.bodyB = _bodies[PART_LEFT_SHIN]->getBody();
//    jointDef.localAnchorA.Set(0, -SHIN_OFFSET / 2);
//    jointDef.localAnchorB.Set(0, SHIN_OFFSET / 2);
//    jointDef.enableLimit = true;
//    jointDef.upperAngle = M_PI / 2;
//    jointDef.lowerAngle = -M_PI / 2;
//    joint = world.CreateJoint(&jointDef);
//    _joints.push_back(joint);
//
//    jointDef.bodyA = _bodies[PART_RIGHT_THIGH]->getBody();
//    jointDef.bodyB = _bodies[PART_RIGHT_SHIN]->getBody();
//    jointDef.localAnchorA.Set(0, -SHIN_OFFSET / 2);
//    jointDef.localAnchorB.Set(0, SHIN_OFFSET / 2);
//    jointDef.enableLimit = true;
//    jointDef.upperAngle = M_PI / 2;
//    jointDef.lowerAngle = -M_PI / 2;
//    joint = world.CreateJoint(&jointDef);
//    _joints.push_back(joint);
    
//    // Weld bubbler to the head.
    b2WeldJointDef weldDef;
//    weldDef.bodyA = _bodies[PART_HEAD]->getBody();
//    weldDef.bodyB = _bubbler->getBody();
//    weldDef.localAnchorA.Set(BUBB_OFF[0], BUBB_OFF[1]);
//    weldDef.localAnchorB.Set(0, 0);
//    joint = world.CreateJoint(&weldDef);
//    _joints.push_back(joint);
    
    // Weld center of mass to torso
    weldDef.bodyA = _bodies[BODY]->getBody();
    weldDef.bodyB = _body;
    weldDef.localAnchorA.Set(0, 0);
    weldDef.localAnchorB.Set(0, 0);
    joint = world.CreateJoint(&weldDef);
    _joints.push_back(joint);
    
    return true;
}

/**
 * Create new fixtures for this body, defining the shape
 *
 * This method is typically undefined for complex objects.  However, it
 * is necessary if we want to weld the body to track the center of mass.
 * Joints without fixtures are undefined.
 */
void Cloud::createFixtures() {
    if (_body == nullptr) {
        return;
    }
    
    releaseFixtures();
    
    // Create the fixture for the center of mass
    b2CircleShape shape;
    shape.m_radius = CENTROID_RADIUS;
    _fixture.shape = &shape;
    _fixture.density = CENTROID_DENSITY;
    _centroid = _body->CreateFixture(&_fixture);
    
    markDirty(false);
}

/**
 * Release the fixtures for this body, reseting the shape
 *
 * This method is typically undefined for complex objects.  However, it
 * is necessary if we want to weld the body to track the center of mass.
 * Joints without fixtures are undefined.
 */
void Cloud::releaseFixtures() {
    if (_centroid != nullptr) {
        _body->DestroyFixture(_centroid);
        _centroid = nullptr;
    }
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
    for (int ii = 0; ii < _unitNum; ii++) {
        std::shared_ptr<Texture> image = _texture;
        std::shared_ptr<PolygonNode> sprite = PolygonNode::allocWithTexture(image);
        if (ii == RIGHT) {
            sprite->flipHorizontal(true); // More reliable than rotating 90 degrees.
        }
        _node->addChild(sprite);
    }
    
//    // Bubbler takes a standard node
//    std::shared_ptr<Node> bubbs = Node::alloc();
//    bubbs->setPosition(_node->getPosition());
//    _bubbler->setGeneratorNode(bubbs);
//    _node->addChild(bubbs);
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


