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
#include "CloudNode.hpp"
#include <Box2D/Dynamics/b2World.h>
#include <Box2D/Dynamics/Contacts/b2Contact.h>
#include <cugl/2d/CUPathNode.h>
#include <Box2D/Collision/b2Collision.h>

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
#define SWIPE_VERT_OFFSET   3.5
#define SWIPE_HORI_OFFSET   3
#define GES_COOLDOWN      20
//#define PINCH_COOLDOWN      30
//#define DOUBLETAP_COOLDOWN       30
#define SPLIT_COOLDOWN      30

//long swipeCoolDown = -1;
//long pinchCoolDown = -1;

#define PARTICLE_MODE  true

long splitCoolDown = -1;
//long doubleTapCoolDown = -1;
long gesCoolDown = -1;
Cloud * pinchedCloud1 = nullptr;
Cloud * pinchedCloud2 = nullptr;
Vec2 pinchPos = Vec2::ZERO;
std::map<long, Obstacle*> cloudsToSplit_temp;
std::map<long, Obstacle*> cloudsToSplit;
Size CLOUD_DEFAULT_SIZE;
Size CLOUDFACE_DEFAULT_SIZE = Size(25, 20);
Size CLOUD_SHADOW_DEFAULT_SIZE = Size(171, 43);

int max_cloud_id;


// Since these appear only once, we do not care about the magic numbers.
// In an actual game, this information would go in a data file.
// IMPORTANT: Note that Box2D units do not equal drawing units
/** The wall vertices */
float CLOUD[] = { 0.f, 0.f, 5.1f, 0.f, 5.1f, 2.6f, 0.f, 2.6};

//TODO::FIX WALLS!
//float WALL1[] = { 16.0f, 18.0f, 16.0f, 17.0f,  1.0f, 17.0f,
//    1.0f,  7.1f, 16.0f,  7.1f, 16.0f,  7.0f,
//    0.0f,  7.0f,  0.0f, 18.0f };
//float WALL2[] = { 32.0f, 18.0f, 32.0f,  7.0f, 16.0f,  7.0f,
//    16.0f,  7.1f, 31.0f,  7.1f, 31.0f, 17.0f,
//    16.0f, 17.0f, 16.0f, 18.0f };

float WALL1[] = { 16.0f, 18.0f, 16.0f, 17.0f,  1.0f, 17.0f,
    1.0f,  1.0f, 16.0f,  1.0f, 16.0f,  0.0f,
    0.f,  0.0f,  0.0f, 18.0f };
float WALL2[] = { 32.0f, 18.0f, 32.0f,  0.0f, 16.0f,  0.0f,
    16.0f,  1.0f, 31.0f,  1.0f, 31.0f, 17.0f,
    16.0f, 17.0f, 16.0f, 18.0f };


int plants[] = { 1, 4, 18, 21, 24};
//int plants[] = { 9 };

/** The initial position of the ragdoll head */
int ticks = 0;
long click1 = -1;
long click2 = -1;
long click3 = -1;
Obstacle * clicked_cloud = nullptr;

long temp = 01;
std::unordered_set<int> raining_clouds;
long rainingTicks = 0l;
long shadeCoolDown = 50l;
bool pinched = false;
long pinchTicks = -1;
bool pinchedWhenContact = false;
std::map<long, Vec2> touchIDs_started_outside;
std::vector<Obstacle *> rainDrops;
//bool processedGes = false;
std::shared_ptr<Plant> currentPlant;

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
    CULogGLError();
    return init(assets,Rect(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),Vec2(0,0), "level1");
}

bool GameScene::init(const std::shared_ptr<AssetManager>& assets, std::string level) {
    CULogGLError();
    return init(assets,Rect(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),Vec2(0,0), level);
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
    return init(assets,rect,Vec2(0,WATER_GRAVITY), "level1");
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
bool GameScene::init(const std::shared_ptr<AssetManager>& assets, const Rect& rect, const Vec2& gravity, std::string levelId) {

    // Initialize the scene to a locked height (iPhone X is narrow, but wide)
    dimen = computeActiveSize();
    if (assets == nullptr) {
        return false;
    } else if (!Scene::init(dimen)) {
        return false;
    }
    

    _level = assets->get<LevelModel>(levelId);
    if (_level == nullptr) {
        CULog("Fail!");
        return false;
    }

    // Start up the input handler
    _input.init();
    _clouds = _level->getClouds();
    _world = ObstacleWorld::alloc(rect,gravity);

    
    max_cloud_id = _clouds.size();
    CLOUD_DEFAULT_SIZE = _clouds[0]->getSize();

    // Create the world and attach the listeners.
    
//    _world->activateCollisionCallbacks(true);
//    _world->onBeginContact = [this](b2Contact* contact) {
//        beginContact(contact);
//    };
//    _world->onEndContact = [this](b2Contact* contact){
//        endContact(contact);
//    };
//    _world->beforeSolve = [this](b2Contact* contact, const b2Manifold* oldManifold) {
//        beforeSolve(contact,oldManifold);
//    };

    // IMPORTANT: SCALING MUST BE UNIFORM
    // This means that we cannot change the aspect ratio of the physics world
    // Shift to center if a bad fit
    
    _scale = dimen.width == SCENE_WIDTH ? dimen.width/rect.size.width : dimen.height/rect.size.height;
    Vec2 offset((dimen.width-SCENE_WIDTH)/2.0f,(dimen.height-SCENE_HEIGHT)/2.0f);

    _rootnode = Node::alloc();
    _rootnode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    _rootnode->setPosition(offset);

    _assets = assets;

    
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
    _level->setDebugNode(_debugnode);


    addChildWithName(_worldnode,"worldNode");
    addChildWithName(_debugnode,"debugNode");
    addChildWithName(_rootnode,"rootnode");
    

    std::vector<std::shared_ptr<Texture>> textures;
    textures.push_back(_assets->get<Texture>("tile"));
    _board = Board::alloc(32, textures, GRID_NUM_X, GRID_NUM_Y);
    CULogGLError();
    auto boardNode = Node::alloc();
//    boardNode->setZOrder(1);
    _board->setSceneNode(boardNode);
    _worldnode->addChildWithName(boardNode, "boardNode");
    
//     //Create selector
//    _selector = ObstacleSelector::alloc(_world);
//    _selector->setDebugColor(DYNAMIC_COLOR);
//    _selector->setDebugScene(_debugnode);
    
    //Cloud Shadows
    shared_ptr<Texture> shadow = _assets->get<Texture>("shadow");
    for(auto it = _clouds.begin(); it != _clouds.end(); ++it) {
        shared_ptr<PolygonNode> _shadowNode = PolygonNode::allocWithTexture(shadow);
        _shadows.push_back(_shadowNode);
    }
    
    _rootnode->setContentSize(Size(SCENE_WIDTH,SCENE_HEIGHT));
    _level->setDrawScale(_scale);
    _level->setAssets(_assets);
    _level->setRootNode(_rootnode, dimen, _shadows); // Obtains ownership of root.
    _levelworldnode = _level->getWorldNode();

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
        _levelworldnode = nullptr;
        _rootnode = nullptr;
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
        s.second = nullptr;
    }
    _world->clear();
//    _cloud = nullptr;
//    _selector->setDebugScene(nullptr);
    for (auto &s : _selectors){
        if (s.second){
            s.second->setDebugScene(nullptr);
        }
    }
    _worldnode->removeAllChildren();
    _debugnode->removeAllChildren();
    _rootnode->removeAllChildren();
    _levelworldnode->removeAllChildren();

    // Add the selector back to the debug node
//    _selector->setDebugScene(_debugnode);
//    for (auto &s : _selectors){
//        s.second->setDebugScene(_debugnode);
//    }

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
    addObstacle(_worldnode, wallobj1,sprite,1);  // All walls share the same texture
    
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
    addObstacle(_worldnode, wallobj2,sprite,1);  // All walls share the same texture
    
    _rainNode = ParticleNode::allocWithTexture(_assets->get<Texture>("smallRain"));
    // _rainNode->setBlendFunc(GL_ONE, GL_ONE);
    _rainNode->setPosition(Vec2::ZERO);
    //CAPACITY
    _memory = FreeList<Particle>::alloc(100);
    Size size = Application::get()->getDisplaySize();
    _rainNode->setContentSize(size);
    _levelworldnode->addChild(_rainNode);
    
    auto levelSelectButtonNode = PolygonNode::allocWithTexture(_assets->get<Texture>("backToLevelSelectButton"));
    levelSelectButtonNode->setContentSize(dimen/12);
    levelSelectButtonNode->setName("backToLevelSelectButton");
    levelSelectButtonNode->setPosition(0, 0);
    
    _levelSelectButton = Button::alloc(levelSelectButtonNode);
    _levelSelectButton->setPosition(0, 0);
    _levelSelectButton->setListener([=](const std::string& name, bool down) {
        this->_active = down;
    });
    _levelSelectButton->setContentSize(dimen/12);
    _levelSelectButton->setVisible(true);
    _levelSelectButton->activate(10);
    _worldnode->addChild(_levelSelectButton);
    
    int i = 0;
    for(auto it = _clouds.begin(); it != _clouds.end(); ++it) {
        std::shared_ptr<Cloud> cloud = *it;
        cloud->setScale(_level->getCloudDrawScale());
        auto cloudNode = CloudNode::alloc(_assets->get<Texture>("particle"));
        cloudNode->setName(cloud->getName());
        cloud->setSceneNodeParticles(cloudNode, GRID_HEIGHT + DOWN_LEFT_CORNER_Y, _assets->get<Texture>("cloudFace"), _shadows[i]);
        addObstacle(_levelworldnode, cloud, cloudNode, 1);
        i++;
    }
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
void GameScene::addObstacle(const std::shared_ptr<cugl::Node> worldNode, const std::shared_ptr<cugl::Obstacle>& obj, const std::shared_ptr<cugl::Node>& node, int zOrder) {
    _world->addObstacle(obj);
    obj->setDebugScene(_debugnode);

    // Position the scene graph node (enough for static objects)
    node->setPosition(obj->getPosition()*_scale);
    worldNode->addChild(node,zOrder);

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

void GameScene::combineByPinch(Cloud* cind1, Cloud* cind2, Vec2& pinchPos){
    if (cind1 == nullptr || cind2 == nullptr) {
        return;
    }
    auto c1p = cind1->getPosition();
    auto c2p = cind2->getPosition();
//    CULog("combine by pinch");
    auto c1p_rad = sqrt(pow(CLOUD_DEFAULT_SIZE.width/2*cind1->getCloudSizeScale(), 2) + pow(CLOUD_DEFAULT_SIZE.height/2*cind1->getCloudSizeScale(), 2));
    auto c2p_rad = sqrt(pow(CLOUD_DEFAULT_SIZE.width/2*cind2->getCloudSizeScale(), 2) + pow(CLOUD_DEFAULT_SIZE.height/2*cind2->getCloudSizeScale(), 2));
    if (c1p.distance(c2p) > c1p_rad + c2p_rad + PINCH_OFFSET) return;
    if (cind1->getCloudSizeScale() >= 5 && cind2->getCloudSizeScale() >= 5) return;
    
    Cloud * cloudToGetLarger = cind1;
    Cloud * cloudToBeDeleted = cind2;
    if (cind2->getCloudSizeScale() < 5 && cind2->getCloudSizeScale() < cind1->getCloudSizeScale()){
        cloudToGetLarger = cind2;
        cloudToBeDeleted = cind1;
    }

    Vec2 pos = _worldnode->screenToNodeCoords(pinchPos);
    pos /= _scale;

    // check that pinch center is in between collided clouds
    float x_left_gap = pos.x - min(c1p.x, c2p.x);
    float x_right_gap = max(c1p.x, c2p.x) - pos.x;
    float y_bottom_gap = pos.y - min(c1p.y, c2p.y);
    float y_top_gap = max(c1p.y, c2p.y) - pos.y;

    if ((x_left_gap >= 0 && x_left_gap <= PINCH_OFFSET && x_right_gap >= 0 && x_right_gap <= PINCH_OFFSET) || (y_bottom_gap >= 0 && y_bottom_gap <= PINCH_OFFSET && y_top_gap <= PINCH_OFFSET && y_top_gap >= 0)){
        CULog("contact between %s and %s", cind1->getName().c_str(), cind2->getName().c_str());
        
        cloudToGetLarger->setCloudSizeScale(sqrt(pow(cind1->getCloudSizeScale(), 2) + pow(cind2->getCloudSizeScale(), 2)));
        long toDelete = -1;
        for(auto &ts : _selectors) {
            long touchID = ts.first;
            auto s = ts.second;
            if (s != nullptr && s->getName() == cind1->getName()){
                s = nullptr;
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
        cloudToBeDeleted->markForRemoval();
    }
}



void GameScene::checkForCombining(Obstacle * ob){
//    CULog("check for combining");
//    std::cout << ob << endl;
    if (pinched && ob){
        if (pinchedCloud1 == nullptr) {
            pinchedCloud1 = (Cloud *) ob;
        }
        else if (pinchedCloud2 == nullptr && ob->getName() != pinchedCloud1->getName()) {
            pinchedCloud2 = (Cloud *) ob;
        }
        if (pinchedCloud2 != nullptr && pinchedCloud1 != nullptr) {
            combineByPinch(pinchedCloud1, pinchedCloud2, pinchPos);
            pinched = false;
            pinchedCloud1 = nullptr;
            pinchedCloud2 = nullptr;
        }
    }
}

void GameScene::checkForThunder(Obstacle * o){
    if (o && ((Cloud *) o)->isRainCloud()) {
        if (click1 == -1){
            click1 = ticks;
            clicked_cloud = o;
        }
        else if (click2 == -1){
            click2 = ticks;
            long gap = click2 - click1;
            if (gap <= 50 && clicked_cloud && clicked_cloud->getName() == o->getName()){
                gesCoolDown = ticks;
//                makeRain(o);
                CULog("make thunder");
            }
            click1 = -1;
            click2 = -1;
            clicked_cloud = nullptr;
        }
        else if (click3 == -1){
            
        }
    }
}

Obstacle * GameScene::getSelectedObstacle(Vec2 pos, long touchID){
    Obstacle * ob = nullptr;
    for (auto &c : _clouds){
        auto left = c->getPosition().x - c->getWidth()/2;
        auto right = c->getPosition().x + c->getWidth()/2;
        auto up = c->getPosition().y + c->getHeight()/2;
        auto down = c->getPosition().y - c->getHeight()/2;
        if (left <= pos.x && pos.x <= right && down <= pos.y && pos.y <= up){
            ob = (Obstacle *) c.get();
            break;
        }
    }
    return ob;
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
//    CULog("game scene updating");
    _input.update(dt);
    ticks++;
    
//    CULog("after updating input");

//    processedGes = false;

    // Process the toggled key commands
    if (_input.didDebug()) { setDebug(!isDebug()); }
    if (_input.didReset()) { reset(); }
    if (_input.didExit())  {
        CULog("Shutting down");
        Application::get()->quit();
    }
    if (_levelSelectButton->isDown()){
        _backToLevelSelect = true;
    }
    
    // process combining
    if (_input.didPinchSelect()){
        if (ticks - gesCoolDown >= GES_COOLDOWN){
            CULog("pinched");
            gesCoolDown = ticks;
            pinched = true;
            pinchPos = _input.getPinchSelection();
        }
    }
    
    bool shaded;
    shared_ptr<Node> thisNode;
    for (int i=0; i < GRID_NUM_X; i++){
        for (int j=0; j < GRID_NUM_Y; j++){
            shaded = false;
            thisNode = _board->getNodeAt(i, j);

            for (auto &c : _clouds) {
                if (c == nullptr) {
                    continue;
                }
                else { //do this better later
                    shaded = shaded || c->shadowCheck(_worldnode, thisNode);
                }
            }

             if (shaded) {
                 for (std::shared_ptr<Plant> p : _level->getPlants()){
                     if (p != nullptr && p->getX() == i && p->getY() == j){
                        p->setShade(true);
                     }
                 }
                 //uncomment to ensure the right tiles are shaded
//                 _board->getNodeAt(i, j)->setColor(getColor() - Color4(230,230,230,0));
             }
             else{
                 //uncomment to ensure the right tiles are shaded
//                 _board->getNodeAt(i, j)->setColor(getColor() - Color4(255,0,0,0));
             }
        }
    }

    //Check win/loss conditions
    auto plantNode = _levelworldnode->getChildByName("plantNode");
    for (auto &plant : _level->getPlants()){
       if (ticks % 250 == 0 && ticks > 150) {
           plant->updateState();
       }
       int st = plant->getState();
   }

    if (ticks % 50 == 0 && ticks > 50) {
        for (auto &pest : _level->getPests()){
            int targetY = pest->getTarget().y;
            int targetX;
            for(auto &plant : _level->getPlants()) {
                if (plant->getStage() > 2 && plant->getX()) {
                    targetX = plant->getX();
                    pest->walk();
                    break;
                }
            }

        }
    }

    for (auto &pest : _level->getPests()){
        pest->update(dt);
    }

   for(auto &plant : _level->getPlants()) {
       plant->update(dt);
   }
    
    auto IDs = _input.getTouchIDs();
    auto selected = _input.didSelect();
    for (auto const& touchID : IDs) {
        if (selected.count(touchID)){
            auto pos = _input.getSelection(touchID);
            pos = _worldnode->screenToNodeCoords(pos)/_scale;
            Obstacle * ob;
            if (!_selectors.count(touchID)){
                ob = getSelectedObstacle(pos, touchID);
                if (ob == nullptr) {
                    touchIDs_started_outside.insert({touchID, pos});
                }
                checkForCombining(ob);
                _selectors.insert({touchID, ob});
            }
            else{
                ob =_selectors.at(touchID);
                if (ob == nullptr){
                    ob = getSelectedObstacle(pos, touchID);
                }
                if (ob) {
                    checkForCombining(ob);
                    bool flag = false;
                    if (touchIDs_started_outside.count(touchID)){
                        flag = true;
                        if (cloudsToSplit_temp.count(touchID) == 0){
                            float y_dist = abs(ob->getPosition().y - touchIDs_started_outside.at(touchID).y);
                            if (y_dist > SWIPE_VERT_OFFSET){
                                if (ticks - gesCoolDown >= GES_COOLDOWN){
                                    CULog("swiped");
                                    gesCoolDown = ticks;
                                    cloudsToSplit_temp.insert({touchID, ob});
                                }
                            }
                        }
                    }
                    if (!flag){
                        ob->setPosition(pos);
                        _levelworldnode->getChildByName(ob->getName())->setPosition(pos);
                    }
                }
            }
        }
    }
    
    selected = _input.didSelect();
    for (auto const& touchID : IDs) {
        if (!selected.count(touchID)){
            auto pos = _input.getSelection(touchID);
            pos = _worldnode->screenToNodeCoords(pos)/_scale;
            
            if (cloudsToSplit_temp.count(touchID)){
                auto cloudPos = cloudsToSplit_temp.at(touchID)->getPosition();
                if (_selectors.count(touchID) && abs(pos.y - cloudPos.y) >= SWIPE_VERT_OFFSET){
                    cloudsToSplit.insert({touchID, cloudsToSplit_temp.at(touchID)});
                }
                cloudsToSplit_temp.erase(touchID);
            }
            if (_selectors.count(touchID)){
                if (_selectors.at(touchID) != nullptr){
                    auto o = _selectors.at(touchID);
                    if (ticks - gesCoolDown >= GES_COOLDOWN){
                        checkForThunder(o);
                    }
                    if (_input.longPressed() && ticks - gesCoolDown >= GES_COOLDOWN){
                        gesCoolDown = ticks;
                        makeRain(o);
                    }
                }
                _selectors.at(touchID) = nullptr;
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
    
    processRemoval();
    
    for (auto &c : _clouds) {
        if (c != nullptr) {
            auto cloudNode = _level->getWorldNode()->getChildByName(c->getName());
            cloudNode->setContentSize(c->getCloudSize());
            float scale = c->getCloudSizeScale();
            
            for (auto child: cloudNode->getChildren()){
                if (child->getName() == "cloudFace"){
//                    child->setContentSize(CLOUDFACE_DEFAULT_SIZE*scale);
                }
                else {
//                    child->setContentSize(CLOUD_SHADOW_DEFAULT_SIZE*scale);
                }
            }
            c->setSize(CLOUD_DEFAULT_SIZE*scale);
            c->getBody()->DestroyFixture(&c->getBody()->GetFixtureList()[0]);
            b2Shape * shape = c->getBody()->GetFixtureList()[0].GetShape();
            b2PolygonShape * polyshape = (b2PolygonShape *) shape;
            polyshape->SetAsBox(c->getSize().width/2, c->getSize().height/2);
            c->getBody()->CreateFixture(polyshape, 3);
        }
    }
    
    _level->update(ticks);
    _world->update(dt);
}


void GameScene::processRemoval(){
    //    process list for deletion
    for (int i = _clouds.size() - 1; i >= 0; i--) {
        auto c = _clouds.at(i);
        if (c && c->isRemoved()) {
            _levelworldnode->removeChildByName(c->getName());
            _levelworldnode->removeChildByName(c->getShadowNode()->getName());
            std::string cname = c->getName();
            long toDelete = -1;
            for (auto &ts : _selectors){
                auto s = ts.second;
                if (s != nullptr && s->getName() == cname){
                    toDelete = ts.first;
                    break;
                }
            }
            if (toDelete != -1){
                _selectors.erase(toDelete);
                touchIDs_started_outside.erase(toDelete);
                cloudsToSplit.erase(toDelete);
            }
            c->deactivatePhysics(*_world->getWorld());
            _world->removeObstacle(((Obstacle *) c.get()));
            _clouds.erase(_clouds.begin() + i);
        }
    }
}

void GameScene::makeRain(Obstacle * ob){
    if (!ob) return;
    
    auto c = (Cloud *) ob;
//    if (!c->isRainCloud()) return;
    c->setCloudSizeScale(c->getCloudSizeScale()*sqrt(9.3/10));
    
    Vec2 cloud_pos = c->getPosition();

    // Draw rain droplets
    for (int i = -3; i < 3; i++){
        Particle* sprite = _memory->malloc();
        if (sprite != nullptr) {
            sprite->setTrajectory(-0.5f*M_PI);
            sprite->setPosition(Vec2(cloud_pos.x + 0.9 * i + 0.3, cloud_pos.y - 1.5)*_scale);
            _rainNode->addParticle(sprite);
            _pQ.push_back(sprite);
        }
    }

    bool rained;
    shared_ptr<Node> thisNode;
    for (int i=0; i < GRID_NUM_X; i++){
        for (int j=0; j < GRID_NUM_Y; j++){
            rained = false;
            thisNode = _board->getNodeAt(i, j);
            rained = c->shadowCheck(_worldnode, thisNode);

            if (rained) {
                for (std::shared_ptr<Plant> p : _level->getPlants()){
                    if (p != nullptr && p->getX() == i && p->getY() == j){
                        p->setRained(true);
                    }
                }
            }
        }
    }

}


void GameScene::splitClouds(){
//    if (splitCoolDown == -1) splitCoolDown = ticks;
    for (auto &ic : cloudsToSplit){
        // split clouds here
        Cloud * c =(Cloud *)(ic.second);
        
        // to small to split
        if (c->getCloudSizeScale()/sqrt(2) <= 0.5) continue;
        
        c->setCloudSizeScale(c->getCloudSizeScale()/sqrt(2));
        auto cloudPos = c->getPosition();
        
        max_cloud_id++;
        auto new_pos = Vec2(cloudPos.x-1.5, cloudPos.y);
        std::shared_ptr<Cloud> new_cloud = _level->createNewCloud(max_cloud_id, new_pos);
        new_cloud->setCloudSizeScale(c->getCloudSizeScale());
        
        auto cloudNode = CloudNode::alloc(_assets->get<Texture>("particle"));
        cloudNode->setName(new_cloud->getName());
        cloudNode->setPosition(new_pos);
        shared_ptr<PolygonNode> new_shadow = PolygonNode::allocWithTexture(_assets->get<Texture>("shadow"));
        _shadows.push_back(new_shadow);
        _level->getWorldNode()->addChildWithName(new_shadow, "shadow" + std::to_string(max_cloud_id), -1);
        _level->getWorldNode()->sortZOrder();
        new_cloud->setSceneNodeParticles(cloudNode, GRID_HEIGHT + DOWN_LEFT_CORNER_Y, _assets->get<Texture>("cloudFace"), _shadows.back());
        new_cloud->setDebugColor(DYNAMIC_COLOR);
        new_cloud->setDebugScene(_debugnode);

//        CULog("created new cloud %i", new_cloud->getId());
        _clouds.push_back(new_cloud);
        addObstacle(_levelworldnode, new_cloud, cloudNode, 1);
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
//        CULog("clouds null");
        return;
    }

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
