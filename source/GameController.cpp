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
#include <Box2D/Collision/b2Collision.h>
#include "Board.hpp"

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
long ticks = 0l;
long click1 = -1l;
long click2 = -1l;
long temp = 01;
Obstacle * clicked_ob = nullptr;
long rainingTicks = 0l;
long shadeCoolDown = 50l;
// std::vector<Obstacle *> toBeRemoved;


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
//    _world->onBeginContact = [this](b2Contact* contact) {
//        beginContact(contact);
//    };
//    _world->beforeSolve = [this](b2Contact* contact, const b2Manifold* oldManifold) {
//        beforeSolve(contact,oldManifold);
//    };

  
    // IMPORTANT: SCALING MUST BE UNIFORM
    // This means that we cannot change the aspect ratio of the physics world
    // Shift to center if a bad fit
    _scale = dimen.width == SCENE_WIDTH ? dimen.width/rect.size.width : dimen.height/rect.size.height;
    Vec2 offset((dimen.width-SCENE_WIDTH)/2.0f,(dimen.height-SCENE_HEIGHT)/2.0f);
    
    // Create the scene graph
    std::shared_ptr<Texture> image = _assets->get<Texture>("backg");
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

//    // Add foreground layer
//    image = _assets->get<Texture>(FRGD_TEXTURE);
//    std::shared_ptr<Node> node  = PolygonNode::allocWithTexture(image);
//    node->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
//    node->setPosition(offset);
//    node->setColor(Color4(255, 255, 255, FRGD_OPACITY));
//    addChild(node, 2);
    
    // Create selector
    _selector = ObstacleSelector::alloc(_world);
    _selector->setDebugColor(DYNAMIC_COLOR);
    _selector->setDebugScene(_debugnode);
    

//    for (unsigned int i = 0; i < sizeof(PLANT_POS_X)/sizeof(PLANT_POS_X[0]); i++){
//        _plants[i] = Plant::alloc(Vec2(PLANT_POS_X[i], PLANT_POS_Y[i]));
//        std::shared_ptr<Node> node  = PolygonNode::allocWithTexture(image);
//        node->setScale(0.3f);
//        node->setPosition(Vec2(PLANT_POS_X[i], PLANT_POS_Y[i]));
//        addChildWithName(node, "plant" + std::to_string(i));
//    }
    
//    _assets->load<Texture>("sun", "/textures/bestsun.png");
//    image = _assets->get<Texture>("sun");
//    sunNode = PolygonNode::allocWithTexture(image);
//    sunNode->setName("sun");
//    sunNode->setScale(0.4f);
//    sunNode->setAnchor(Vec2::ANCHOR_TOP_LEFT);
//    sunNode->setPosition(Vec2(40,SCENE_HEIGHT - 40));
//    addChild(sunNode, 4);
  
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
        _cloud2 = nullptr;
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
    std::shared_ptr<PolygonNode> sprite = PolygonNode::allocWithTexture(image,wall1);
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
    
    auto boardNode = Node::alloc();
    _board->setSceneNode(boardNode);
    _worldnode->addChildWithName(boardNode, "gridNode");

    
    auto plantNode = Node::alloc();
    for (int i = 0; i < GRID_NUM_X; i++){
        for (int j = 0; j < GRID_NUM_Y; j++){
            auto plant = Plant::alloc(i, j, _assets->get<Texture>("plant"), 32.0f);
            plant->setSceneNode(plantNode);
        }
    }
    _worldnode->addChildWithName(plantNode, "plantNode");
    
    auto cloudNode = Node::alloc();
    
    _cloud = Cloud::alloc(Vec2(16, 10), _scale);
    _cloud->initialBuild(_assets);
    _cloud->setSceneNode(cloudNode);
    _cloud->setDebugColor(DYNAMIC_COLOR);
    _cloud->setName("cloud");
    _cloud->setDebugScene(_debugnode);
    _worldnode->addChildWithName(cloudNode, "cloudNode");
    _world->addObstacle(_cloud);
    
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

/*Raycasting callback function*/
float callback(b2Fixture* fixture, const Vec2& point, const Vec2& normal, float fraction){
    if (fixture->GetBody()->GetType() == 2){
        currentPlant->isShaded = true;
    }
    //hoefully we will not collide with other plants
    return 0.0;
}

Vec2 transformPoint(Vec2 point) {
    float x = (32.0/961.0)*point.x - (1024.0/961.0);
    float y = (9.0/256.0)*point.y - (9.0/8.0);
    
    return Vec2(x,y);
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
    ticks ++;
    
    // Process the toggled key commands
    if (_input.didDebug()) { setDebug(!isDebug()); }
    if (_input.didReset()) { reset(); }
    if (_input.didExit())  {
        CULog("Shutting down");
        Application::get()->quit();
    }

    Vec2 v = _cloud->getBodies()[0]->getPosition();
    
    if (_board->isInBounds(v.x, v.y)){
        Vec2 gridPos = _board->posToGridCoord(v.x,v.y);
        _board->getNodeAt(gridPos.x, gridPos.y)->setColor(getColor() - Color4(0, 255, 255, 0));
    }
    
    if (ticks % 50 == 0){
        for (int i =0; i < GRID_NUM_X; i++){
            for (int j=0; j < GRID_NUM_Y; j++){
                _board->getNodeAt(i, j)->setColor(getColor() + Color4(255, 0, 0, 0));
            }
        }
    }
    
    //Get Input
    //If Clouds Dragged, Update Physics Location of Clouds
    //Else if Cloud Combined/Split, Destroy/Create Cloud
    //Else if Cloud Form Changed, Change Cloud State
    
    //Update Clouds
    //Update Pests
    //Update Plants
    //Draw View
    
    //Check win/loss conditions
    
//    for (unsigned int i = 0; i < sizeof(PLANT_POS_Y)/sizeof(PLANT_POS_Y[0]); i++){
//        currentPlant = _plants[i];
//        currentPlant->isShaded = false;
//        Vec2 plantPos = currentPlant->BoxObstacle::getPosition();
//        Vec2 sunPos = Vec2(plantPos.x, 0);
//        std::function<float (b2Fixture *, const Vec2 &, const Vec2 &, float)> f = callback;
//        _world->rayCast(f, transformPoint(plantPos), transformPoint(sunPos));
//        currentPlant->updateState();
//        int st = currentPlant->getState();
//        if (ticks % 250 == 0 && st == noNeed){
//            int n = rand() % 4;
//            if (n == needRain){n = noNeed;}
//            currentPlant->setState(n);
//        }
//        std::string childName = "plant" + std::to_string(i);
//        if (st == noNeed) {
//            getChildByName(childName)->setColor(Color4(0, 255, 0));
//        }
//        if (st == needRain){
//            getChildByName(childName)->setColor(Color4(0, 0, 255));
//        }
//        else if (st == needSun){
//            getChildByName(childName)->setColor(Color4(255, 165, 0));
//        }
//        else if (st == needShade) {
//            getChildByName(childName)->setColor(Color4(255, 0, 0));
//        }
//        else if (st == dead){
//            getChildByName(childName)->setColor(Color4(0, 0, 0));
//        }
//    }
    
    //shade
//    Vec2 cloudPos = _cloud->getPosition();      //in box2d coord
//    if (_board->isInBounds(cloudPos.x, cloudPos.y)){
//        Vec2 gridp = _board->posToGridCoord(cloudPos.x, cloudPos.y);
//        _board->getNodeAt(gridp.x, gridp.y)->setColor(Color4(255, 0, 0));
//    }
    
    // Move an object if touched
    if (_input.didSelect()) {
        // Transform from screen to physics coords
        auto pos =  _input.getSelection();
        pos = _worldnode->screenToNodeCoords(pos);
        
        // Place the cross hair
        _selector->setPosition(pos/_scale);
      
        // Attempt to select an obstacle at the current position
        if (!_selector->isSelected()) {
            _selector->select();
        }
    } else {
        if (_selector->isSelected()) {

//            std::cout <<"-------"<<endl;
//            std::cout << _cloud->getPosition().x <<endl;
//            std::cout << _cloud->getPosition().y <<endl;
//            std::cout <<"--------"<<endl;
            //_selector->getObstacle()->setPosition(_selector->getPosition());
            if (click1 == -1){
                clicked_ob = _selector->getObstacle();
                Vec2 pos = clicked_ob->getPosition();
                std::cout << pos.x << endl;
                std::cout << pos.y << endl;
                click1 = ticks;
            }
            else if (click2 == -1){
                click2 = ticks;
                if (click2 - click1 <= 50 && clicked_ob == _selector->getObstacle()){
                    ((Cloud *) clicked_ob)->setIsRaining(true);
                    _rainDrops.clear();
                    for (int i = -5; i < 5; i++){
                        Vec2 cloud_pos = ((Cloud *) clicked_ob)->getPosition();
                        std::shared_ptr<BoxObstacle> rainDrop = BoxObstacle::alloc(Vec2(cloud_pos.x + 0.1*i, cloud_pos.y - 1.5), _assets->get<Texture>("bubble")->getSize()/_scale);
                        rainDrop->setName("bubble");
                        rainDrop->setMass(0);
                        rainDrop->setLinearVelocity(0, -1);
                        std::shared_ptr<PolygonNode> rainNode = PolygonNode::allocWithTexture(_assets->get<Texture>("bubble"));
                        _toBeRemoved.push_back(rainDrop);
                        addObstacle(rainDrop, rainNode, 5);
                    }
                }
                click1 = -1;
                click2 = -1;
                clicked_ob = nullptr;
            }
            _selector->deselect();
        }
        
    }
    
    //assert (_rainDrops.size() == _toBeRemoved.size());

//    if (ticks % 10 == 0){
//        std::cout <<"here"<<endl;
//        for (int i = 0; i < _toBeRemoved.size(); i++){
//            std::shared_ptr<Obstacle> ob = _toBeRemoved.at(i);
//            std::cout << _world->getObstacles().size() <<endl;
//            std::cout << ob->getName() << endl;
//            _world->removeObstacle(ob.get());
//            _toBeRemoved.at(i)->markRemoved(true);
//        }
//        _toBeRemoved.clear();
//    }
    
    // Turn the physics engine crank.
    _cloud->update(dt);
    _world->update(dt);
    
}

void GameScene::beginContact(b2Contact* contact) {
    
    b2Body* body1 = contact->GetFixtureA()->GetBody();
    b2Body* body2 = contact->GetFixtureB()->GetBody();

    Obstacle * b1 = (Obstacle *)(body1->GetUserData());
    Obstacle * b2 = (Obstacle *)(body2->GetUserData());
//
//
//    // If we hit the "win" door, we are done
//    Obstacle * b1 = (Obstacle *)(body1->GetUserData());
//    Obstacle * b2 = (Obstacle *)(body2->GetUserData());
//
//    if(body1->IsBullet() && (b2->getName() == "crop" || b2->getName() == "wall")) {
//        Obstacle * b1 = (Obstacle *)(body1->GetUserData());
//        toBeRemoved.push_back(b1);
//        std::cout << b1->getName() <<endl;
//        std::cout << b2->getName() <<endl;
//        //delete b1;
//    }

    assert (body1->GetUserData() != nullptr and body2->GetUserData() != nullptr);
    
    if(body1->GetUserData() == _cloud.get() || body2->GetUserData() == _cloud.get()) {
        
        //std::cout <<"here"<<endl;
        std::cout << "-------" << endl;
        b2->setDebugColor(Color4(255, 0, 0));
        b1->setDebugColor(Color4(0, 255, 0));
        std::cout << "-------" << endl;
            //delete b1;
    }
//    else if (body2->IsBullet() && (b1->getName() == "crop" || b1->getName() == "wall")){
//        Obstacle * b2 = (Obstacle *)(body2->GetUserData());
//        toBeRemoved.push_back(b2);
//        std::cout << b1->getName() <<endl;
//        std::cout << b2->getName() <<endl;
//        //delete b2;j
//    }
////    std::cout << "END" <<endl;
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
//    float speed = 0;
////
////    // Use Ian Parberry's method to compute a speed threshold
//    b2Body* body1 = contact->GetFixtureA()->GetBody();
//    b2Body* body2 = contact->GetFixtureB()->GetBody();
//    b2WorldManifold worldManifold;
//    contact->GetWorldManifold(&worldManifold);
//    b2PointState state1[2], state2[2];
//    b2GetPointStates(state1, state2, oldManifold, contact->GetManifold());
//    for(int ii =0; ii < 2; ii++) {
//        if (state2[ii] == b2_addState) {
//            b2Vec2 wp = worldManifold.points[0];
//            b2Vec2 v1 = body1->GetLinearVelocityFromWorldPoint(wp);
//            b2Vec2 v2 = body2->GetLinearVelocityFromWorldPoint(wp);
//            b2Vec2 dv = v1-v2;
//            speed = b2Dot(dv,worldManifold.normal);
//        }
//    }
////
////    // Play a sound if above threshold
////    if (speed > SOUND_THRESHOLD) {
////        // These keys result in a low number of sounds.  Too many == distortion.
////        std::string key = ((Obstacle*)body1->GetUserData())->getName()+((Obstacle*)body2->GetUserData())->getName();
////        auto source = _assets->get<Sound>(COLLISION_SOUND);
////        if (!AudioChannels::get()->isActiveEffect(key)) {
////            AudioChannels::get()->playEffect(key, source, false, source->getVolume());
////        }
////    }
//    Obstacle * b1 = (Obstacle *) body1;
//    Obstacle * b2 = (Obstacle *) body2;
//    if (b1->getName() == b2->getName()){
//        contact->SetEnabled(false);
//    }
//    else{
//        contact->SetEnabled(true);
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
