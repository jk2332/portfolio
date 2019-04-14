//
//  GameController.cpp
//  Weather Defender
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
#include "GameController.h"
#include "Plant.hpp"
#include <Box2D/Dynamics/b2World.h>
#include <Box2D/Dynamics/Contacts/b2Contact.h>
#include <Box2D/Dynamics/Joints/b2RevoluteJoint.h>
#include <Box2D/Dynamics/Joints/b2WeldJoint.h>
#include <cugl/2d/CUPathNode.h>

#include <Box2D/Collision/b2Collision.h>
#include "Board.hpp"
#include "Particle.hpp"

#include <ctime>
#include <string>
#include <iostream>
#include <sstream>
#include <memory>
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
#define SWIPE_VERT_OFFSET   4
#define SWIPE_HORI_OFFSET   3
#define GES_COOLDOWN      30
//#define PINCH_COOLDOWN      30
//#define DOUBLETAP_COOLDOWN       30
#define SPLIT_COOLDOWN      30

//long swipeCoolDown = -1;
//long pinchCoolDown = -1;

long splitCoolDown = -1;
//long doubleTapCoolDown = -1;
long gesCoolDown = -1;
float iosToDesktopScaleX;
float iosToDesktopScaleY;
Cloud * pinchedCloud1 = nullptr;
Cloud * pinchedCloud2 = nullptr;
int num_clouds = 2;
int max_cloud_id = num_clouds;


// Since these appear only once, we do not care about the magic numbers.
// In an actual game, this information would go in a data file.
// IMPORTANT: Note that Box2D units do not equal drawing units
/** The wall vertices */
float WALL1[] = { 16.0f, 19.0f, 16.0f, 18.0f,  0.0f, 18.0f,
                   0.0f,  0.0f, 16.0f,  0.0f, 16.0f,  -1.0f,
                   -1.0f,  -1.0f,  -1.0f, 19.0f };
float WALL2[] = { 33.0f, 19.0f, 33.0f, -1.0f, 16.0f,  -1.0f,
                  16.0f, 0.0f, 32.0f, 0.0f, 32.0f, 18.0f,
                  16.0f, 18.0f, 16.0f, 19.0f };
float CLOUD[] = { 0.f, 0.f, 5.1f, 0.f, 5.1f, 2.6f, 0.f, 2.6};
//float WALL1[] = { 16.0f, 18.0f, 16.0f, 17.0f,  1.0f, 17.0f,
//    1.0f,  1.0f, 16.0f,  1.0f, 16.0f,  0.0f,
//    0.f,  0.0f,  0.0f, 18.0f };
//float WALL2[] = { 32.0f, 18.0f, 32.0f,  0.0f, 16.0f,  0.0f,
//    16.0f,  1.0f, 31.0f,  1.0f, 31.0f, 17.0f,
//    16.0f, 17.0f, 16.0f, 18.0f };

int plants[] = { 1, 4, 18, 21, 35};
//int plants[] = { 9 };


map<int, int> rainMap = {{1, 20}, {5, 50}, {17, 0}, {21, 0}, {35, 25}, {9, 99}};
map<int, int> shadeMap = {{1, 40}, {5, 0}, {17, 40}, {21, 0}, {35, 55}, {9, 0}};

/** The initial position of the ragdoll head */
long ticks = 0l;
long click1 = -1;
long click2 = -1;
Obstacle * clicked_cloud = nullptr;

long temp = 01;
std::unordered_set<int> raining_clouds;
long rainingTicks = 0l;
long shadeCoolDown = 50l;
bool pinched = false;
long pinchTicks = -1;
bool pinchedWhenContact = false;
std::map<long, Vec2> touchIDs_started_outside;
std::map<long, Obstacle*> cloudsToSplit;
std::vector<Obstacle *> rainDrops;
//bool processedGes = false;


#pragma mark -
#pragma mark Physics Constants

/** The density for all of (external) objects */
#define BASIC_DENSITY       0.0f
/** The friction for all of (external) objects */
#define BASIC_FRICTION      0.1f
/** The restitution for all of (external) objects */
#define BASIC_RESTITUTION   0.1f
/** The new lessened gravity for this world */
#define WATER_GRAVITY   1.0f


#pragma mark Assset Constants
/** The key for the rocket texture in the asset manager */
#define BKGD_TEXTURE    "background"
#define EARTH_TEXTURE   "earth"
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
#define GRID_NUM_X          9
#define GRID_NUM_Y          4
#define PINCH_OFFSET        1.5
#define PINCH_CLOUD_DIST_OFFSET     5

std::shared_ptr<Plant> currentPlant;

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
    return init(assets,Rect(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),Vec2(0,0));
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
    std::vector<std::shared_ptr<Texture>> textures;
    for (int i = 1; i < 6; i++){
        textures.push_back(_assets->get<Texture>("tile"));
    }

    _board = Board::alloc(32, textures, GRID_NUM_X, GRID_NUM_Y);
    
    _input.init();
    
    // Create the world and attach the listeners.
    _world = ObstacleWorld::alloc(rect,gravity);
//    _world->activateCollisionCallbacks(true);
    _world->onBeginContact = [this](b2Contact* contact) {
        beginContact(contact);
    };
    _world->onEndContact = [this](b2Contact* contact){
        endContact(contact);
    };
    _world->beforeSolve = [this](b2Contact* contact, const b2Manifold* oldManifold) {
        beforeSolve(contact,oldManifold);
    };

  
    // IMPORTANT: SCALING MUST BE UNIFORM
    // This means that we cannot change the aspect ratio of the physics world
    // Shift to center if a bad fit
    _scale = dimen.width == SCENE_WIDTH ? dimen.width/rect.size.width : dimen.height/rect.size.height;
    Vec2 offset((dimen.width-SCENE_WIDTH)/2.0f,(dimen.height-SCENE_HEIGHT)/2.0f);
    
    // Create the scene graph
    std::shared_ptr<Texture> image = _assets->get<Texture>("background");
    _worldnode = PolygonNode::allocWithTexture(image);
    _worldnode->setName("world");
    _worldnode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    _worldnode->setPosition(offset);
    
    _debugnode = Node::alloc();
    _debugnode->setScale(_scale); // Debug node draws in PHYSICS coordinates
    _debugnode->setName("debug");
    _debugnode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    _debugnode->setPosition(offset);

    addChildWithName(_worldnode,"worldNode");
    addChildWithName(_debugnode,"debugNode");
  
    populate();
    _active = true;
    _complete = false;
    setDebug(true);
    
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
        _selectors.clear();
        _worldnode = nullptr;
        _debugnode = nullptr;
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
//    _selector->deselect();
    for (auto &s : _selectors){
        s.second->deselect();
    }
    _world->clear();
//    _cloud = nullptr;
//    _selector->setDebugScene(nullptr);
    for (auto &s : _selectors){
        s.second->setDebugScene(nullptr);
    }
    _worldnode->removeAllChildren();
    _debugnode->removeAllChildren();
    
    // Add the selector back to the debug node
//    _selector->setDebugScene(_debugnode);
    for (auto &s : _selectors){
        s.second->setDebugScene(_debugnode);
    }
    
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
#pragma mark : Wall polygon 1
   // Create ground pieces
   // All walls share the same texture
   std::shared_ptr<Texture> image  = _assets->get<Texture>("earth");
   std::string wname = "wall";

   // Create the polygon outline
   Poly2 wall1(WALL1,16);
   SimpleTriangulator triangulator;
   triangulator.set(wall1);
   triangulator.calculate();
   wall1.setIndices(triangulator.getTriangulation());
   wall1.setType(Poly2::Type::SOLID);

   std::shared_ptr<PolygonObstacle> wallobj1 = PolygonObstacle::alloc(wall1);
   wallobj1->setDebugColor(STATIC_COLOR);
   wallobj1->setName(wname);

   // Set the physics attributes
   wallobj1->setBodyType(b2_staticBody);
   wallobj1->setDensity(BASIC_DENSITY);
   wallobj1->setFriction(BASIC_FRICTION);
   wallobj1->setRestitution(BASIC_RESTITUTION);

   // Add the scene graph nodes to this object
   wall1 *= _scale;
   std::shared_ptr<PolygonNode> sprite = PolygonNode::allocWithTexture(image,wall1);
   addObstacle(wallobj1,sprite,1);  // All walls share the same texture

#pragma mark : Wall polygon 2
   Poly2 wall2(WALL2,16);
   triangulator.set(wall2);
   triangulator.calculate();
   wall2.setIndices(triangulator.getTriangulation());
   wall2.setType(Poly2::Type::SOLID);

   std::shared_ptr<PolygonObstacle> wallobj2 = PolygonObstacle::alloc(wall2);
   wallobj2->setDebugColor(STATIC_COLOR);
   wallobj2->setName(wname);

   // Set the physics attributes
   wallobj2->setBodyType(b2_staticBody);
   wallobj2->setDensity(BASIC_DENSITY);
   wallobj2->setFriction(BASIC_FRICTION);
   wallobj2->setRestitution(BASIC_RESTITUTION);

   // Add the scene graph nodes to this object
   wall2 *= _scale;
   sprite = PolygonNode::allocWithTexture(image,wall2);
   addObstacle(wallobj2,sprite,1);  // All walls share the same texture
    
    auto boardNode = Node::alloc();
    _board->setSceneNode(boardNode);
    _worldnode->addChildWithName(boardNode, "gridNode");

    std::vector<std::shared_ptr<Texture>> pTextures;
    pTextures.push_back(_assets->get<Texture>("tomato1"));
    pTextures.push_back(_assets->get<Texture>("tomato2"));
    pTextures.push_back(_assets->get<Texture>("tomato3"));
    pTextures.push_back(_assets->get<Texture>("tomato4"));

    auto plantNode = Node::alloc();
    for (int i = 0; i < GRID_NUM_X; i++){
        for (int j = 0; j < GRID_NUM_Y; j++){
           int plantNum = i*GRID_NUM_Y + j;
           // Create an array of string objects

           auto plant = Plant::alloc(i, j, rainMap[plantNum], shadeMap[plantNum], pTextures, 32.0f);

           auto *idx = std::find(std::begin(plants), std::end(plants), plantNum);
           if (idx == std::end(plants)) {
               continue;
           }

           auto plantName = "plant" + std::to_string(plantNum);
           plant->setSceneNode(plantNode, plantName);
           plant->setName(plantName);
           _plants[i*GRID_NUM_Y + j] = plant;
        }
    }
    _worldnode->addChildWithName(plantNode, "plantNode");
    
    for (int i = 0; i < num_clouds; i++) {
        // Create the polygon outline
        Poly2 cloudpoly(CLOUD, 8);
        SimpleTriangulator triangulator;
        triangulator.set(cloudpoly);
        triangulator.calculate();
        cloudpoly.setIndices(triangulator.getTriangulation());
        cloudpoly.setType(Poly2::Type::SOLID);
        
        std::shared_ptr<Cloud> cloud = Cloud::alloc(cloudpoly, Vec2(28-i*6, 10));
        cloud->setDebugColor(DYNAMIC_COLOR);
        cloud->setName("cloud" + std::to_string(i));
        cloud->setId(i);
        cloud->setScale(_scale);
        _clouds.push_back(cloud);
        
        // Set the physics attributes
        std::shared_ptr<PolygonObstacle> cloudobj = PolygonObstacle::alloc(cloudpoly);
        cloudobj->setBodyType(b2_dynamicBody);
        
        // Add the scene graph nodes to this object
        cloudpoly *= _scale;
        sprite = PolygonNode::allocWithTexture(_assets->get<Texture>("cloud"),cloudpoly);
        sprite->setName("cloud" + std::to_string(i));
        cloud->setSceneNode(sprite);
        addObstacle(cloud,sprite,1);  // All walls share the same texture

    }
    _rainNode = ParticleNode::allocWithTexture(_assets->get<Texture>("bubble"));
    _rainNode->setBlendFunc(GL_ONE, GL_ONE);
    _rainNode->setPosition(Vec2::ZERO);
//CAPACITY
    _memory = FreeList<Particle>::alloc(100);
    Size size = Application::get()->getDisplaySize();
    _rainNode->setContentSize(size);
    _worldnode->addChild(_rainNode);
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
//    _world->addObstacle(obj);
//    obj->setDebugScene(_debugnode);
//
//    // Position the scene graph node (enough for static objects)
//    node->setPosition(obj->getPosition()*_scale);
//    _worldnode->addChild(node,zOrder);
    _world->addObstacle(obj);
    obj->setDebugScene(_debugnode);
    
    // Position the scene graph node (enough for static objects)
    node->setPosition(obj->getPosition()*_scale);
    _worldnode->addChild(node,zOrder);
    
    // Dynamic objects need constant updating
    if (obj->getBodyType() == b2_dynamicBody) {
        Node* weak = node.get(); // No need for smart pointer in callback
        obj->setListener([=](Obstacle* obs){
//            weak->setPosition(obs->getPosition()*_scale);
            weak->setPosition(obs->getPosition()*_scale);

//            weak->setAngle(obs->getAngle());
        });
    }
}


#pragma mark -
#pragma mark Physics Handling

/*Raycasting callback function*/
float callback(b2Fixture* fixture, const Vec2& point, const Vec2& normal, float fraction){
    if (fixture->GetBody()->GetType() == 2){
        currentPlant->setShade(true);
    }
    //hoefully we will not collide with other plants
    return 0.0;
}

Vec2 transformPoint(Vec2 point) {
    float x = (32.0/961.0)*point.x - (1024.0/961.0);
    float y = (9.0/256.0)*point.y - (9.0/8.0);
    
    return Vec2(x,y);
}

void GameScene::combineByPinch(Cloud* cind1, Cloud* cind2, Vec2 pinchPos){
    if (cind1 == nullptr || cind2 == nullptr) {
        return;
    }

    auto c1p = cind1->getPosition();
    auto c2p = cind2->getPosition();
    CULog("combine by pinch");
    
    if (c1p.distance(c2p) <= PINCH_CLOUD_DIST_OFFSET) return;

    pinchPos.x = pinchPos.x/iosToDesktopScaleX;
    pinchPos.y = 18 - pinchPos.y/iosToDesktopScaleY;
    
    // check that pinch center is in between collided clouds
    if (min(c1p.x, c2p.x) - PINCH_OFFSET <= pinchPos.x && pinchPos.x <= max(c1p.x, c2p.x) + PINCH_OFFSET &&
        min(c1p.y, c2p.y) - PINCH_OFFSET <= pinchPos.y && pinchPos.y <= max(c1p.y, c2p.y) + PINCH_OFFSET){
        CULog("contact between %s and %s", cind1->getName().c_str(), cind2->getName().c_str());
        cind2->incSize(cind1->getSize() - 1.0f);
        long toDelete = -1;
        for(auto &ts : _selectors) {
            long touchID = ts.first;
            auto s = ts.second;
            if (s->isSelected() && s->getObstacle() && s->getObstacle()->getName() == cind1->getName()){
                s->deselect();
                toDelete = touchID;
                break;
            }
        }
        if (toDelete != -1){
            if (_selectors.count(toDelete)) {
                _selectors.erase(toDelete);
            }
            if (touchIDs_started_outside.count(toDelete)) touchIDs_started_outside.erase(toDelete);
            if (cloudsToSplit.count(toDelete)) cloudsToSplit.erase(toDelete);
        }
        std::cout << "cloud to be removed: " + cind1->getName() << endl;
        cind1->markForRemoval();
    }
}

bool isCloud(Obstacle * ob){
    if (ob->getName().find("cloud") == 0) return true;
    return false;
}

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
    ticks++;
//    processedGes = false;
    
    // Process the toggled key commands
    if (_input.didDebug()) { setDebug(!isDebug()); }
    if (_input.didReset()) { reset(); }
    if (_input.didExit())  {
        CULog("Shutting down");
        Application::get()->quit();
    }
    // process combining
    if (_input.didPinchSelect()){
        if (ticks - gesCoolDown >= GES_COOLDOWN){
            CULog("pinched");
            gesCoolDown = ticks;
            pinched = true;
        }
    }
    
    for (int i=0; i < GRID_NUM_X; i++){
        for (int j=0; j < GRID_NUM_Y; j++){
            _board->getNodeAt(i, j)->setColor(getColor() + Color4(255, 0, 0, 0));
        }
    }
    for (auto &c : _clouds) {
        if (c == nullptr) {
            continue;
        }
        else {
             Vec2 v = c->getPosition();
            if (v.y > GRID_HEIGHT + DOWN_LEFT_CORNER_Y){
                v.y = v.y - GRID_HEIGHT - DOWN_LEFT_CORNER_Y;
            }
            if (_board->isInBounds(v.x, v.y)){
                std::pair<int, int> coord = _board->posToGridCoord(v.x,v.y);
                if (coord.first >= 0 && coord.second >= 0) {
                    _board->getNodeAt(coord.first, coord.second)->setColor(getColor() - Color4(230, 230, 230, 0));
                    int plantIdx = coord.first * GRID_NUM_Y + coord.second;
                    auto plant = _plants[plantIdx];
                    if (plant != nullptr) {
                        plant->setShade(true);
                    }

                }
            }

        }
    }
    
    
    //Check win/loss conditions
    
    auto plantNode = _worldnode->getChildByName("plantNode");
    for (int i = 0; i < sizeof(_plants)/sizeof(_plants[0]); ++i){
        auto *idx = std::find(std::begin(plants), std::end(plants), i);
        if (idx == std::end(plants)) {
            continue;
        }

        currentPlant = _plants[i];
        if (ticks % 200 == 0 && ticks > 200) {
//        if (ticks % 100 == 0) {
            currentPlant->updateState();
//            CULog(std::to_string(ticks).c_str());
        }
        int st = currentPlant->getState();
        
        bool debugPlantColor = false;
        
        std::string childName = "plant" + std::to_string(i);
        if (st == noNeed) {
            if (debugPlantColor) {
                CULog("no need");
            }
            plantNode->getChildByName(childName)->setColor(Color4::WHITE);
        }
        if (st == needRain){
            if (debugPlantColor) {
                CULog("need rain");
            }
            plantNode->getChildByName(childName)->setColor(Color4(0, 0, 255));
        }
        else if (st == needSun){
            if (debugPlantColor) {
                CULog("need sun");
            }
            plantNode->getChildByName(childName)->setColor(Color4(255, 165, 0));
        }
        else if (st == needShade) {
            if (debugPlantColor) {
                CULog("need shade");
            }
            plantNode->getChildByName(childName)->setColor(Color4(255, 0, 0));
        }
        else if (st == dead){
            if (debugPlantColor) {
                CULog("dead");
            }
        }
    }
    
    
    auto IDs = _input.getTouchIDs();
    auto selected = _input.didSelect();
    for (auto const& touchID : IDs) {
        if (selected.count(touchID)){
            auto pos = _input.getSelection(touchID);
            pos = _worldnode->screenToNodeCoords(pos);
            std::shared_ptr<ObstacleSelector> selector;
            
            if (!_selectors.count(touchID)){
                selector = ObstacleSelector::alloc(_world);
                selector->setDebugColor(DYNAMIC_COLOR);
                selector->setDebugScene(_debugnode);
                selector->setPosition(pos/_scale);
                if (!selector->isSelected()){
                    selector->select();
                }
                auto ob = selector->getObstacle();
                if (!(selector->isSelected() && ob && isCloud(ob)) && !touchIDs_started_outside.count(touchID)){
                    touchIDs_started_outside.insert({touchID, selector->getPosition()});
                }
                if (pinched && ob && isCloud(ob)){
                    if (pinchedCloud1 == nullptr) {
                        CULog("storing cloud1 to combine 1");
                        pinchedCloud1 = (Cloud *) ob;
                    }
                    else if (pinchedCloud2 == nullptr && ob->getName() != pinchedCloud1->getName()) {
                        CULog("storing cloud2 to combine 1");
                        pinchedCloud2 = (Cloud *) ob;
                    }
                    if (pinchedCloud2 != nullptr && pinchedCloud1 != nullptr) {
                        combineByPinch(pinchedCloud1, pinchedCloud2, _input.getPinchSelection());
                        pinched = false;
                        pinchedCloud1 = nullptr;
                        pinchedCloud2 = nullptr;
                    }
                }
                if (selector->isSelected() && ob && !isCloud(ob)){
                    selector->deselect();
                }
                _selectors.insert({touchID, selector});
            }
            else{
                selector =_selectors.at(touchID);
                //                CULog("already exists");
                selector->setPosition(pos/_scale);
                if (!selector->isSelected()){
                    selector->select();
                }
                if (selector->isSelected()){
                    auto ob = selector->getObstacle();
                    if (touchIDs_started_outside.count(touchID)){
                        //                        CULog("deselecting existing selector for swiping");
                        if (!cloudsToSplit.count(touchID)){
                            float y_dist = ob->getPosition().y - touchIDs_started_outside.at(touchID).y;
                            if (y_dist > SWIPE_VERT_OFFSET){
                                if (ticks - gesCoolDown >= GES_COOLDOWN){
                                    CULog("swiped");
                                    gesCoolDown = ticks;
                                    if (ticks - splitCoolDown > SPLIT_COOLDOWN){
                                        splitCoolDown = -1;
                                        cloudsToSplit.insert({touchID, ob});
                                    }
                                }
                            }
                        }
                        selector->deselect();
                    }
                    if (ob && !isCloud(ob)){
                        selector->deselect();
                    }
                    if (pinched && ob && isCloud(ob)){
                        if (pinchedCloud1 == nullptr) {
                            CULog("storing cloud1 to combine 2");
                            pinchedCloud1 = (Cloud *) ob;
                        }
                        else if (pinchedCloud2 == nullptr) {
                            if (ob->getName() != pinchedCloud1->getName()){
                                CULog("storing cloud2 to combine 2");
                                pinchedCloud2 = (Cloud *) ob;
                            }
                        }
                        if (pinchedCloud2 != nullptr && pinchedCloud1 != nullptr) {
                            combineByPinch(pinchedCloud1, pinchedCloud2, _input.getPinchSelection());
                            pinched = false;
                            pinchedCloud1 = nullptr;
                            pinchedCloud2 = nullptr;
                        }
                    }
                }
            }
        }
    }
    
    selected = _input.didSelect();
    for (auto const& touchID : IDs) {
        if (!selected.count(touchID)){
            //            CULog("touch ID  after selection %i", touchID);
            if (_selectors.count(touchID)){
                if (_selectors.at(touchID)->isSelected()){
                    if (cloudsToSplit.count(touchID)){
                        CULog("started swipping outside the cloud but ended inside");
                        cloudsToSplit.erase(touchID);
                    }
                    if (ticks - gesCoolDown >= GES_COOLDOWN + 10){
                        auto o = _selectors.at(touchID)->getObstacle();
                        if (o && o->getName().find("cloud") == 0) {
                            if (click1 == -1){
                                click1 = ticks;
                                clicked_cloud = o;
                            }
                            else if (click2 == -1){
                                click2 = ticks;
                                long gap = click2 - click1;
                                if (gap <= 50 && clicked_cloud && clicked_cloud->getName() == o->getName()){
                                    CULog("double tapped");
                                    gesCoolDown = ticks;
                                    makeRain(o);
                                }
                                click1 = -1;
                                click2 = -1;
                                clicked_cloud = nullptr;
                                
                            }
                        }
                    }
                }
                else if (cloudsToSplit.count(touchID)){
                    auto cloudPos = cloudsToSplit.at(touchID)->getPosition();
                    if (!(_selectors.at(touchID)->getPosition().y - cloudPos.y >= SWIPE_VERT_OFFSET)){
                        cloudsToSplit.erase(touchID);
                    }
                }
                _selectors.at(touchID)->deselect();
                _selectors.erase(touchID);
            }
            if (touchIDs_started_outside.count(touchID)){
                touchIDs_started_outside.erase(touchID);
            }
            _input.removeFromTouchID(touchID);
        }
    }
   
    if (ticks % 80 == 0) {
        for(auto it = _pD.begin(); it != _pD.end(); ++it) {
            Particle* p = *it;
            _rainNode->removeParticle(p);
            _memory->free(p);
        }
        _pD.clear();
        for(auto it = _pQ.begin(); it != _pQ.end(); ++it) {
            Particle* p = *it;
            _pD.push_back(p);
        }
        _pQ.clear();
    }
    _rainNode->update(_particles);
    
    // process clouds to split
    splitClouds();
    cloudsToSplit.clear();
    
    // process removal
    processRemoval();
    
    Size s = _assets->get<Texture>("cloud")->getSize();
    for (auto &c : _clouds) {
        if (c != nullptr) {
            auto cloudNode = _worldnode->getChildByName(c->getName());
            cloudNode->setContentSize(s*c->getCloudSize());
            c->update(dt);
        }
    }
    
    _world->update(dt);

}

void GameScene::processRemoval(){
    //    process list for deletion
    std::vector<int> indices_to_delete;
    for (int i = 0; i < _clouds.size(); i++) {
        auto c = _clouds.at(i);
        if (c && c->isRemoved()) {
            CULog("removing in update");
            std::string cname = "cloud" + std::to_string(c->getId());
            _worldnode->removeChildByName(cname);
            long toDelete = -1;
            for (auto &ts : _selectors){
                auto s = ts.second;
                if (s->isSelected() && s->getObstacle() && s->getObstacle()->getName() == cname){
                    CULog("removing selector for deletion");
                    s->deselect();
                    toDelete = ts.first;
                    break;
                }
            }
            if (toDelete != -1){
                _selectors.erase(toDelete);
                touchIDs_started_outside.erase(toDelete);
                cloudsToSplit.erase(toDelete);
            }
            _world->removeObstacle(((Obstacle *) c.get()));
            c->deactivatePhysics(*_world->getWorld());
            c->dispose();
            indices_to_delete.push_back(i);
        }
    }
    for (auto ind : indices_to_delete){
        _clouds.erase(_clouds.begin() + ind);
    }
}

void GameScene::makeRain(Obstacle * cloud){
    auto c = (Cloud *) cloud;
    Vec2 cloud_pos = c->getPosition();
    
    // Draw rain droplets
    c->decSize();
    for (int i = -3; i < 3; i++){
        Particle* sprite = _memory->malloc();
        if (sprite != nullptr) {
            sprite->setTrajectory(-0.5f*M_PI);
            sprite->setPosition(Vec2(cloud_pos.x + 0.5 * i + 0.3, cloud_pos.y - 1.5)*_scale);
            _rainNode->addParticle(sprite);
            _pQ.push_back(sprite);
        }
    }
    
    // Set plants to rained
    if (cloud_pos.y > GRID_HEIGHT + DOWN_LEFT_CORNER_Y){
        cloud_pos.y = cloud_pos.y - GRID_HEIGHT - DOWN_LEFT_CORNER_Y;
    }
    std::pair<int, int> coord = _board->posToGridCoord(cloud_pos);
    if (coord.first >= 0 && coord.second >= 0) {
        int plantIdx = coord.first * GRID_NUM_Y + coord.second;
        auto plant = _plants[plantIdx];
        if (plant != nullptr) {
            plant->setRained(true);
        }
    }
    c->setIsRaining(false);
}


void GameScene::splitClouds(){
    if (splitCoolDown == -1) splitCoolDown = ticks;
    for (auto &ic : cloudsToSplit){
        
        // split clouds here
        auto cloudNode = _worldnode->getChildByName(ic.second->getName());
        Cloud* c = (Cloud*)ic.second;
        c->decSize();
        Vec2 cloudPos = ic.second->getPosition();
        Poly2 cloudpoly(CLOUD, 8);
        SimpleTriangulator triangulator;
        triangulator.set(cloudpoly);
        triangulator.calculate();
        cloudpoly.setIndices(triangulator.getTriangulation());
        cloudpoly.setType(Poly2::Type::SOLID);
        
        std::shared_ptr<Cloud> cloud = Cloud::alloc(cloudpoly, Vec2(cloudPos.x-1.5, cloudPos.y));
        cloud->setDebugColor(DYNAMIC_COLOR);
        
        max_cloud_id ++;
        cloud->setName("cloud" + std::to_string(max_cloud_id));
        cloud->setScale(_scale);
        cloud->setSize(c->getCloudSize());
        cloud->setId(max_cloud_id);
        _clouds.push_back(cloud);
        
        // Set the physics attributes
        std::shared_ptr<PolygonObstacle> cloudobj = PolygonObstacle::alloc(cloudpoly);
        cloudobj->setBodyType(b2_dynamicBody);
        
        // Add the scene graph nodes to this object
        cloudpoly *= _scale;
        std::shared_ptr<PolygonNode> sprite = PolygonNode::allocWithTexture(_assets->get<Texture>("cloud"),cloudpoly);
        sprite->setName("cloud" + std::to_string(max_cloud_id));
        cloud->setSceneNode(sprite);
        addObstacle(cloud,sprite,1);  // All walls share the same texture
    }
}

    
    

/**
 * Processes the start of a collision
 *
 * This method is called when we first get a collision between two objects.  We use
 * this method to test if it is the "right" kind of collision.  In particular, we
 * use it to test if we make it to the win door.
 *
 * @param  contact  The two bodies that collided
 */
void GameScene::beginContact(b2Contact* contact) {
//    CULog("begin contact");
    
//    b2Body* body1 = contact->GetFixtureA()->GetBody();
//    b2Body* body2 = contact->GetFixtureB()->GetBody();
//
//    // If we hit the "win" door, we are done
//    Obstacle * b1 = (Obstacle *)(body1->GetUserData());
//    Obstacle * b2 = (Obstacle *)(body2->GetUserData());
//
//    if (b1->isBullet() && b2->isBullet()) return;
////    if (b1->isBullet() && b2->isBullet()) return;
//    if (b1->isBullet() && !b1->isRemoved()){
//        CULog("bullet in contact");
//        ((BoxObstacle *) b1)->markRemoved(true);
//        return;
//    }
//
//    if (b2->isBullet() && !b2->isRemoved()){
//        CULog("bullet in contact");
//        ((BoxObstacle *) b2)->markRemoved(true);
//        return;
//    }
    
    Cloud *cloud1 = static_cast<Cloud*>(contact->GetFixtureA()->GetBody()->GetUserData());
    if (cloud1 != nullptr && cloud1->getName().empty()) {
        cloud1 = static_cast<Cloud*>(contact->GetFixtureA()->GetBody()->GetNext()->GetUserData());
    }

    Cloud *cloud2 = static_cast<Cloud*>(contact->GetFixtureB()->GetBody()->GetUserData());

    if (cloud2 != nullptr && cloud2->getName().empty()) {
        cloud2 = static_cast<Cloud*>(contact->GetFixtureB()->GetBody()->GetNext()->GetUserData());
    }

    if (cloud1 == nullptr || cloud2 == nullptr || cloud1 == cloud2 || cloud1->getName() == cloud2->getName() || cloud1->isRemoved() || cloud2->isRemoved()) {
        CULog("clouds null");
        return;
    }

}

/**
 * Handles any modifications necessary before collision resolution
 *
 * This method is called just before Box2D resolves a collision.  We use this method
 * to implement sound on contact, using the algorithms outlined in Ian Parberry's
 * "Introduction to Game Physics with Box2D".
 *
 * @param  contact      The two bodies that collided
 * @param  oldManfold      The collision manifold before contact
 */
void GameScene::beforeSolve(b2Contact* contact, const b2Manifold* oldManifold) {
}

void GameScene::endContact(b2Contact *contact){
}

/**
 * Returns the active screen size of this scene.
 *
 * This method is for graceful handling of different aspect
 * ratios
 */
Size GameScene::computeActiveSize() const {
    Size dimen = Application::get()->getDisplaySize();
    iosToDesktopScaleX = dimen.getIWidth()/32.0f;
    iosToDesktopScaleY = dimen.getIHeight()/18.0f;
    float ratio1 = dimen.width/dimen.height;
    float ratio2 = ((float)SCENE_WIDTH)/((float)SCENE_HEIGHT);
    if (ratio1 < ratio2) {
        dimen *= SCENE_WIDTH/dimen.width;
    } else {
        dimen *= SCENE_HEIGHT/dimen.height;
    }
    return dimen;
}
