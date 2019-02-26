//
//  RGGameScene.cpp
//  Ragdoll Demo
//
//  This is the most important class in this demo.  This class manages the gameplay
//  for this demo.  It also handles collision detection. There is not much to do for
//  collisions; our ObstacleWorld class takes care of all of that for us.  This
//  controller mainly transforms input into gameplay.
//
//  You will notice that we do not use a Scene asset this time.  While we could
//  have done this, we wanted to highlight the issues of connecting physics
//  objects to scene graph objects.  Hence we include all of the API calls.
//
//  WARNING: There are a lot of shortcuts in this design that will do not adapt well
//  to data driven design.  This demo has a lot of simplifications to make it a bit
//  easier to see how everything fits together.  However, the model classes and how
//  they are initialized will need to be changed if you add dynamic level loading.
//
//  This file is based on the CS 3152 PhysicsDemo Lab by Don Holden, 2007
//
//  Author: Walker White and Anthony Perello
//  Version: 1/26/17
//
#include "RGGameScene.h"
#include "Plant.hpp"
#include <Box2D/Dynamics/b2World.h>
#include <Box2D/Dynamics/Contacts/b2Contact.h>
#include <Box2D/Collision/b2Collision.h>

#include <ctime>
#include <string>
#include <iostream>
#include <sstream>
#include <random>

using namespace cugl;

#pragma mark -
#pragma mark Level Geography

/** This is adjusted by screen aspect ratio to get the height */
#define SCENE_WIDTH  1024
#define SCENE_HEIGHT 576

/** Width of the game world in Box2d units */
#define DEFAULT_WIDTH   32.0f
/** Height of the game world in Box2d units */
#define DEFAULT_HEIGHT  18.0f

// Since these appear only once, we do not care about the magic numbers.
// In an actual game, this information would go in a data file.
// IMPORTANT: Note that Box2D units do not equal drawing units
/** The wall vertices */
float WALL1[] = { 16.0f, 18.0f, 16.0f, 17.0f,  1.0f, 17.0f,
				   1.0f,  1.0f, 16.0f,  1.0f, 16.0f,  0.0f,
					0.f,  0.0f,  0.0f, 18.0f };
float WALL2[] = { 32.0f, 18.0f, 32.0f,  0.0f, 16.0f,  0.0f,
			      16.0f,  1.0f, 31.0f,  1.0f, 31.0f, 17.0f,
				  16.0f, 17.0f, 16.0f, 18.0f };

/** The initial position of the ragdoll head */
float DOLL_POS[] = { 16, 10 };
float PLANT_POS_X[] = {SCENE_WIDTH/2, SCENE_WIDTH/2 + 100};
float PLANT_POS_Y[] = {100, 100};

long ticks = 0l;

#pragma mark -
#pragma mark Physics Constants

/** The density for all of (external) objects */
#define BASIC_DENSITY       0.0f
/** The friction for all of (external) objects */
#define BASIC_FRICTION      0.1f
/** The restitution for all of (external) objects */
#define BASIC_RESTITUTION   0.1f
/** How big to make the crosshairs */
#define CROSSHAIR_SIZE      0.1f
/** The new lessened gravity for this world */
#define WATER_GRAVITY   0.0f


#pragma mark Assset Constants
/** The key for the earth texture in the asset manager */
#define EARTH_TEXTURE   "earth"
/** The key for the rocket texture in the asset manager */
#define BKGD_TEXTURE    "background"
/** The key for the win door texture in the asset manager */
#define FRGD_TEXTURE    "foreground"
/** The key for the crosshaior texture in the asset manager */
#define CROSS_TEXTURE   "crosshair"
/** The key prefix for the multiple crate assets */
#define SOUND_PREFIX    "bubble"
/** The number of bubble sounds available */
#define NUM_BUBBLES     4
/** Opacity of the foreground mask */
#define FRGD_OPACITY    64

/** Color to outline the physics nodes */
#define STATIC_COLOR    Color4::YELLOW
/** Opacity of the physics outlines */
#define DYNAMIC_COLOR   Color4::GREEN

/** The key for the font reference */
#define PRIMARY_FONT        "retro"

#pragma mark Physics Constants

// Physics constants for initialization
/** Density of non-crate objects */
#define BASIC_DENSITY       0.0f
/** Density of the crate objects */
#define CRATE_DENSITY       1.0f
/** Friction of non-crate objects */
#define BASIC_FRICTION      0.1f
/** Friction of the crate objects */
#define CRATE_FRICTION      0.2f
/** Angular damping of the crate objects */
#define CRATE_DAMPING       1.0f
/** Collision restitution for all objects */
#define BASIC_RESTITUTION   0.1f
/** Threshold for generating sound on collision */
#define SOUND_THRESHOLD     3


#pragma mark -
#pragma mark Constructors
/**
 * Creates a new game world with the default values.
 *
 * This constructor does not allocate any objects or start the controller.
 * This allows us to use a controller without a heap pointer.
 */
GameScene::GameScene() : Scene(),
_complete(false),
_debug(false),
_counter(0)
{    
}

/**
 * Initializes the controller contents, and starts the game
 *
 * The constructor does not allocate any objects or memory.  This allows
 * us to have a non-pointer reference to this controller, reducing our
 * memory allocation.  Instead, allocation happens in this method.
 *
 * The game world is scaled so that the screen coordinates do not agree
 * with the Box2d coordinates.  This initializer uses the default scale.
 *
 * @param assets    The (loaded) assets for this game mode
 *
 * @return true if the controller is initialized properly, false otherwise.
 */
bool GameScene::init(const std::shared_ptr<AssetManager>& assets) {
    return init(assets,Rect(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),Vec2(0,WATER_GRAVITY));
}

/**
 * Initializes the controller contents, and starts the game
 *
 * The constructor does not allocate any objects or memory.  This allows
 * us to have a non-pointer reference to this controller, reducing our
 * memory allocation.  Instead, allocation happens in this method.
 *
 * The game world is scaled so that the screen coordinates do not agree
 * with the Box2d coordinates.  The bounds are in terms of the Box2d
 * world, not the screen.
 *
 * @param assets    The (loaded) assets for this game mode
 * @param rect      The game bounds in Box2d coordinates
 *
 * @return  true if the controller is initialized properly, false otherwise.
 */
bool GameScene::init(const std::shared_ptr<AssetManager>& assets, const Rect& rect) {
    return init(assets,rect,Vec2(0,WATER_GRAVITY));
}

/**
 * Initializes the controller contents, and starts the game
 *
 * The constructor does not allocate any objects or memory.  This allows
 * us to have a non-pointer reference to this controller, reducing our
 * memory allocation.  Instead, allocation happens in this method.
 *
 * The game world is scaled so that the screen coordinates do not agree
 * with the Box2d coordinates.  The bounds are in terms of the Box2d
 * world, not the screen.
 *
 * @param assets    The (loaded) assets for this game mode
 * @param rect      The game bounds in Box2d coordinates
 * @param gravity   The gravitational force on this Box2d world
 *
 * @return  true if the controller is initialized properly, false otherwise.
 */
bool GameScene::init(const std::shared_ptr<AssetManager>& assets, const Rect& rect, const Vec2& gravity) {

    // Initialize the scene to a locked height (iPhone X is narrow, but wide)
    Size dimen = computeActiveSize();
    if (assets == nullptr) {
        return false;
    } else if (!Scene::init(dimen)) {
        return false;
    }
    
    // Start up the input handler
    _assets = assets;
    _input.init();
    
    // Create the world and attach the listeners.
    _world = ObstacleWorld::alloc(rect,gravity);
  
    // IMPORTANT: SCALING MUST BE UNIFORM
    // This means that we cannot change the aspect ratio of the physics world
    // Shift to center if a bad fit
    _scale = dimen.width == SCENE_WIDTH ? dimen.width/rect.size.width : dimen.height/rect.size.height;
    Vec2 offset((dimen.width-SCENE_WIDTH)/2.0f,(dimen.height-SCENE_HEIGHT)/2.0f);
    
    // Create the scene graph
    std::shared_ptr<Texture> image = _assets->get<Texture>(BKGD_TEXTURE);
    _worldnode = PolygonNode::allocWithTexture(image);
    _worldnode->setName("world");
    _worldnode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    _worldnode->setPosition(offset);
    
    _debugnode = Node::alloc();
    _debugnode->setScale(_scale); // Debug node draws in PHYSICS coordinates
    _debugnode->setName("debug");
    _debugnode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    _debugnode->setPosition(offset);

    addChild(_worldnode,0);
    addChild(_debugnode,1);

    // Add foreground layer
    image = _assets->get<Texture>(FRGD_TEXTURE);
    std::shared_ptr<Node> node  = PolygonNode::allocWithTexture(image);
    node->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    node->setPosition(offset);
    node->setColor(Color4(255, 255, 255, FRGD_OPACITY));
    addChild(node, 2);
    
    // Create selector
    _selector = ObstacleSelector::alloc(_world);
    _selector->setDebugColor(DYNAMIC_COLOR);
    _selector->setDebugScene(_debugnode);
    
    _assets->load<Texture>("crop", "/textures/_Crops-512.png");
    image = _assets->get<Texture>("crop");
    for (unsigned int i = 0; i < sizeof(PLANT_POS_X); i++){
        _plants[i] = Plant::alloc(Vec2(PLANT_POS_X[i], PLANT_POS_Y[i]));
        std::shared_ptr<Node> node  = PolygonNode::allocWithTexture(image);
        node->setName("plant" + std::to_string(i));
        node->setPosition(Vec2(PLANT_POS_X[i], PLANT_POS_Y[i]));
        node->setScale(0.4f);
        addChild(node, 3);
    }
  
    populate();
    _active = true;
    _complete = false;
    setDebug(false);
    
    // XNA nostalgia
    Application::get()->setClearColor(Color4f::CORNFLOWER);
    return true;
}

/**
 * Disposes of all (non-static) resources allocated to this mode.
 */
void GameScene::dispose() {
    if (_active) {
        _input.dispose();
        _world = nullptr;
        _selector = nullptr;
        _worldnode = nullptr;
        _debugnode = nullptr;
        //_ragdoll = nullptr;
        _cloud = nullptr;
        _complete = false;
        _debug = false;
        Scene::dispose();
    }
}


#pragma mark -
#pragma mark Level Layout

/**
 * Resets the status of the game so that we can play again.
 *
 * This method disposes of the world and creates a new one.
 */
void GameScene::reset() {
    _selector->deselect();
    _world->clear();
    //_ragdoll = nullptr;
    _cloud = nullptr;
    _selector->setDebugScene(nullptr);
    _worldnode->removeAllChildren();
    _debugnode->removeAllChildren();
    
    // Add the selector back to the debug node
    _selector->setDebugScene(_debugnode);
    
    setComplete(false);
    populate();
}

/**
 * Lays out the game geography.
 *
 * Pay close attention to how we attach physics objects to a scene graph.
 * The simplest way is to make a subclass, like we do for the rocket.  However,
 * for simple objects you can just use a callback function to lightly couple
 * them.  This is what we do with the crates.
 *
 * This method is really, really long.  In practice, you would replace this
 * with your serialization loader, which would process a level file.
 */
void GameScene::populate() {
#pragma mark : Ragdoll
	// Allocate the ragdoll and set its (empty) node. Its model handles creation of parts 
	// (both obstacles and nodes to be drawn) upon alllocation and setting the scene node.
//    _ragdoll = RagdollModel::alloc(DOLL_POS, _scale);
//    _ragdoll->buildParts(_assets);
//    _ragdoll->makeBubbleGenerator(_assets->get<Texture>("bubble"));
    _cloud = Cloud::alloc(DOLL_POS, _scale);
    _assets->load<Texture>("cloud", "/textures/cloud3.png");
    _cloud->initialBuild(_assets);
//
//    auto ragdollNode = Node::alloc();
//    // Add the ragdollNode to the world before calling setSceneNode, as noted in the documentation for the Ragdoll's method.
//    _worldnode->addChild(ragdollNode);
//    _ragdoll->setSceneNode(ragdollNode);
//
//    _ragdoll->setDrawScale(_scale);
//    _ragdoll->setDebugColor(DYNAMIC_COLOR);
//    _ragdoll->setDebugScene(_debugnode);
//    _world->addObstacle(_ragdoll);
    
    auto cloudNode = Node::alloc();
    _worldnode->addChild(cloudNode);
    _cloud->setSceneNode(cloudNode);
    _cloud->setDrawScale(_scale);
    _cloud->setDebugColor(DYNAMIC_COLOR);
    _cloud->setDebugScene(_debugnode);
    _world->addObstacle(_cloud);

    // All walls share the same texture
    std::shared_ptr<Texture> image = _assets->get<Texture>("earth");
    std::shared_ptr<PolygonNode> sprite;
    std::shared_ptr<WireNode> draw;

#pragma mark : Wall polygon 1
    // Create ground pieces
    // All walls share the same texture
    image  = _assets->get<Texture>(EARTH_TEXTURE);
    std::string wname = "wall";

    // Create the polygon outline
    Poly2 wall1(WALL1,16);
    SimpleTriangulator triangulator;
    triangulator.set(wall1);
    triangulator.calculate();
    wall1.setIndices(triangulator.getTriangulation());
    wall1.setType(Poly2::Type::SOLID);

    std::shared_ptr<PolygonObstacle> wallobj = PolygonObstacle::alloc(wall1);
    wallobj->setDebugColor(STATIC_COLOR);
    wallobj->setName(wname);

    // Set the physics attributes
    wallobj->setBodyType(b2_staticBody);
    wallobj->setDensity(BASIC_DENSITY);
    wallobj->setFriction(BASIC_FRICTION);
    wallobj->setRestitution(BASIC_RESTITUTION);

    // Add the scene graph nodes to this object
    wall1 *= _scale;
    sprite = PolygonNode::allocWithTexture(image,wall1);
    addObstacle(wallobj,sprite,1);  // All walls share the same texture
    
#pragma mark : Wall polygon 2
    Poly2 wall2(WALL2,16);
    triangulator.set(wall2);
    triangulator.calculate();
    wall2.setIndices(triangulator.getTriangulation());
    wall2.setType(Poly2::Type::SOLID);

    wallobj = PolygonObstacle::alloc(wall2);
    wallobj->setDebugColor(STATIC_COLOR);
    wallobj->setName(wname);
    
    // Set the physics attributes
    wallobj->setBodyType(b2_staticBody);
    wallobj->setDensity(BASIC_DENSITY);
    wallobj->setFriction(BASIC_FRICTION);
    wallobj->setRestitution(BASIC_RESTITUTION);

    // Add the scene graph nodes to this object
    wall2 *= _scale;
    sprite = PolygonNode::allocWithTexture(image,wall2);
    addObstacle(wallobj,sprite,1);  // All walls share the same texture
    
#pragma mark: Mouse Cross Hair
    image  = _assets->get<Texture>(CROSS_TEXTURE);
    _crosshair = PolygonNode::allocWithTexture(image);
    _worldnode->addChild(_crosshair);
    _crosshair->setVisible(false);
}

/**
 * Adds the physics object to the physics world and loosely couples it to the scene graph
 *
 * There are two ways to link a physics object to a scene graph node on the
 * screen.  One way is to make a subclass of a physics object, like we did 
 * with rocket.  The other is to use callback functions to loosely couple 
 * the two.  This function is an example of the latter.
 *
 * In addition, scene graph nodes have a z-order.  This is the order they are 
 * drawn in the scene graph node.  Objects with the different textures should 
 * have different z-orders whenever possible.  This will cut down on the amount of drawing done
 *
 * param obj    The physics object to add
 * param node   The scene graph node to attach it to
 * param zOrder The drawing order
 */
void GameScene::addObstacle(const std::shared_ptr<cugl::Obstacle>& obj,
                           const std::shared_ptr<cugl::Node>& node,
                           int zOrder) {
    _world->addObstacle(obj);
    obj->setDebugScene(_debugnode);
    
    // Position the scene graph node (enough for static objects)
    node->setPosition(obj->getPosition()*_scale);
    _worldnode->addChild(node,zOrder);
    
    // Dynamic objects need constant updating
    if (obj->getBodyType() == b2_dynamicBody) {
        Node* weak = node.get(); // No need for smart pointer in callback
        obj->setListener([=](Obstacle* obs){
            weak->setPosition(obs->getPosition()*_scale);
            weak->setAngle(obs->getAngle());
        });
    }
}


#pragma mark -
#pragma mark Physics Handling

/**
 * Executes the core gameplay loop of this world.
 *
 * This method contains the specific update code for this mini-game. It does
 * not handle collisions, as those are managed by the parent class WorldController.
 * This method is called after input is read, but before collisions are resolved.
 * The very last thing that it should do is apply forces to the appropriate objects.
 *
 * @param  delta    Number of seconds since last animation frame
 */
void GameScene::update(float dt) {
    _input.update(dt);
    ticks ++;
    
    // Process the toggled key commands
    if (_input.didDebug()) { setDebug(!isDebug()); }
    if (_input.didReset()) { reset(); }
    if (_input.didExit())  {
        CULog("Shutting down");
        Application::get()->quit();
    }
    
    if (_input.didSplit()){
        _cloud->dropUnit(*_world->getWorld());
    }

    if (_input.didJoin()){
        _cloud->joinUnit(*_world->getWorld());
    }
  
    // Move an object if touched
    if (_input.didSelect()) {
        // Transform from screen to physics coords
        auto pos =  _input.getSelection();
        pos = _worldnode->screenToNodeCoords(pos);
        
        // Place the cross hair
        _selector->setPosition(pos/_scale);
        _crosshair->setPosition(pos);
        _crosshair->setVisible(true);
      
        // Attempt to select an obstacle at the current position
        if (!_selector->isSelected()) {
            _selector->select();
        }
    } else {
        if (_selector->isSelected()) {
            _selector->deselect();
        }
        _crosshair->setVisible(false);
    }

    if (ticks % 100 == 0){
        for (unsigned int i = 0; i < sizeof(PLANT_POS_Y); i++){
            _plants[i]->updateState(std::rand() % 5);
            std::string childName = "plant" + std::to_string(i);
            int st = _plants[i]->getState();
            if (st == noNeed) {continue;}
            if (st == needRain){
                getChildByName(childName)->setColor(Color4(0, 0, 255));
            }
            else if (st == needSun){
                getChildByName(childName)->setColor(Color4(255, 0, 0));
            }
            else {
                getChildByName(childName)->setColor(Color4(211, 211, 211));
            }
        }
    }

    // Turn the physics engine crank.
    _world->update(dt);
  
    // Play a sound for each bubble
//    if (_ragdoll->getBubbleGenerator()->didBubble()) {
//        // Pick a sound by generating a random index and playing it
//        std::random_device rd;
//        std::mt19937 mt(rd());
//        std::uniform_real_distribution<float> dist(0, 1.0f);
//        float rand = dist(mt);
//        int indx = 1+ (int)(rand*NUM_BUBBLES) % NUM_BUBBLES;
//        std::string asset = std::string(SOUND_PREFIX) + (indx < 10 ? "0" : "" ) + cugl::to_string(indx);
//        std::string key = std::string(SOUND_PREFIX);
//        _counter++;
//        std::shared_ptr<Sound> source = _assets->get<Sound>(asset);
//        AudioChannels::get()->playEffect(key, source, false, source->getVolume());
//    }
}

/**
 * Returns the active screen size of this scene.
 *
 * This method is for graceful handling of different aspect
 * ratios
 */
Size GameScene::computeActiveSize() const {
    Size dimen = Application::get()->getDisplaySize();
    float ratio1 = dimen.width/dimen.height;
    float ratio2 = ((float)SCENE_WIDTH)/((float)SCENE_HEIGHT);
    if (ratio1 < ratio2) {
        dimen *= SCENE_WIDTH/dimen.width;
    } else {
        dimen *= SCENE_HEIGHT/dimen.height;
    }
    return dimen;
}
