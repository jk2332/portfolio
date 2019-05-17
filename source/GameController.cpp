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
#include "Constants.hpp"
#include "ConstantsMusic.hpp"
#include <ctime>
#include <string>
#include <iostream>
#include <sstream>
#include <memory>
#include <random>

using namespace cugl;

#pragma mark -
#pragma mark Level Geography
#define GAMEPLAY_MUSIC      "gameplay"

//long splitCoolDown = -1;
long gesCoolDown = -1;
Cloud * pinchedCloud1 = nullptr;
Cloud * pinchedCloud2 = nullptr;
Vec2 pinchPos = Vec2::ZERO;
std::map<long, Obstacle*> cloudsToSplit_temp;
std::map<long, Obstacle*> cloudsToSplit;
std::map<long, Vec2> touchIDs_started_outside;


// Since these appear only once, we do not care about the magic numbers.
// In an actual game, this information would go in a data file.
// IMPORTANT: Note that Box2D units do not equal drawing units
/** The wall vertices */
float CLOUD[] = { 0.f, 0.f, 5.1f, 0.f, 5.1f, 2.6f, 0.f, 2.6};

int ticks = 0;
long rclick1 = -1;
long rclick2 = -1;
long rclick1_touchID = -1;
long lclick1 = -1;
long lclick2 = -1;
long lclick1_touchID = -1;

Obstacle * clicked_cloud = nullptr;
bool shadowClicked = false;
Obstacle * clickedShadowCloud = nullptr;

bool pinched = false;
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
bool GameScene::init(const std::shared_ptr<AssetManager>& assets, bool reset) {
    CULogGLError();
    return init(assets,Rect(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),Vec2(0,0), "level1", reset);
}

bool GameScene::init(const std::shared_ptr<AssetManager>& assets, std::string level, bool reset) {
    CULogGLError();
    return init(assets,Rect(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),Vec2(0,0), level, reset);
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
bool GameScene::init(const std::shared_ptr<AssetManager>& assets, const Rect& rect, bool reset) {
    return init(assets,rect,Vec2(0,WATER_GRAVITY), "level1", reset);
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
bool GameScene::init(const std::shared_ptr<AssetManager>& assets, const Rect& rect, const Vec2& gravity, std::string levelId, bool reset) {
    
    // Initialize the scene to a locked height (iPhone X is narrow, but wide)
    dimenWithIndicator = computeActiveSize();
    _tutorialshown = false;
    dimen = Size(dimenWithIndicator.x, dimenWithIndicator.y);
    _levelId = levelId;
    
    _paused = false;
    _mainSelected= false;
    _resetSelected = false;
    _continueSelected= false;
    
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
    _assets = assets;
    _curr_bkgd = 1;

    // Start up the input handler
    _input.init();
    
    // IMPORTANT: SCALING MUST BE UNIFORM
    // This means that we cannot change the aspect ratio of the physics world
    // Shift to center if a bad fit
    _scale = dimen.width == SCENE_WIDTH ? dimen.width/rect.size.width : dimen.height/rect.size.height;
    Vec2 offset((dimen.width-SCENE_WIDTH)/2.0f,(dimen.height-SCENE_HEIGHT)/2.0f);
    
    _rootnode = Node::alloc();
    _rootnode->setContentSize(SCENE_WIDTH, SCENE_HEIGHT);
    _rootnode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    _rootnode->setPosition(offset);

    _level->reload(false);
    _plants = _level->getPlants();
    _clouds = _level->getClouds();
    
    _world = ObstacleWorld::alloc(rect,gravity);
    _max_cloud_id = _clouds.size();
    
    // Create the scene graph
    std::shared_ptr<Texture> image = _assets->get<Texture>("background" + std::to_string(_curr_bkgd));
    _worldnode = PolygonNode::allocWithTexture(image);
    _worldnode->setName("world");
    _worldnode->setContentSize(SCENE_WIDTH, SCENE_HEIGHT);
    _worldnode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    _worldnode->setPosition(offset);
    
    _debugnode = Node::alloc();
    _debugnode->setScale(_scale); // Debug node draws in PHYSICS coordinates
    _debugnode->setName("debug");
    _debugnode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    _debugnode->setPosition(offset);
    
    image = _assets->get<Texture>("ipad-" + std::to_string(_curr_bkgd));
    _backgroundNode = PolygonNode::allocWithTexture(image);
    _backgroundNode->setName("bigBackground");
    _backgroundNode->setContentSize(dimen);
    _backgroundNode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    
    shared_ptr<Node> containerNode = Node::alloc();
    addChildWithName(containerNode, "container");
    containerNode->addChildWithName(_backgroundNode, "backgroundNode", Z_EXTENDEDBKGD);
    containerNode->addChildWithName(_worldnode, "worldNode", Z_BACKGROUND);
//    addChildWithName(_backgroundNode,"backgroundNode", Z_EXTENDEDBKGD);
//    addChildWithName(_worldnode,"worldNode", Z_BACKGROUND);
    addChildWithName(_debugnode,"debugNode", Z_BACKGROUND);
    containerNode->addChildWithName(_rootnode, "rootnode", Z_BACKGROUND);
//    addChildWithName(_rootnode,"rootnode", Z_BACKGROUND);
    
    _board = Board::alloc(SCENE_WIDTH, _scale, _assets->get<Texture>("tile"), GRID_NUM_X, GRID_NUM_Y);
    CULogGLError();
    auto boardNode = Node::alloc();
    _board->setSceneNode(boardNode);
    _worldnode->addChildWithName(boardNode, "boardNode", Z_GRID);
    
    _rootnode->setContentSize(Size(SCENE_WIDTH,SCENE_HEIGHT));
    _level->setDrawScale(_scale);
    _level->setAssets(_assets);
    _level->setRootNode(_rootnode, dimen, _board, _world, _worldnode, _backgroundNode); // Obtains ownership of root.
    _levelworldnode = _level->getWorldNode();
    _levelworldnode->sortZOrder();
    populate();
    _active = true;
    _complete = false;
    setDebug(false);

    // XNA nostalgia
    Application::get()->setClearColor(Color4f::CORNFLOWER);
    
    if (!reset){
        std::shared_ptr<Sound> source = _assets->get<Sound>(GAMEPLAY_MUSIC);
        AudioChannels::get()->playMusic(source, true, MUSIC_VOLUME, 1.0f);
    }
    AudioChannels::get()->setMusicVolume(MUSIC_VOLUME);
    
    return true;
}

/**
 * Disposes of all (non-static) resources allocated to this mode.
 */
void GameScene::dispose() {
    removePauseDisplay();
    removeVictoryDisplay();
    _input.dispose();
    
    // setting the variable values to default
    ticks = 0;
    gesCoolDown = -1;
    rclick1 = -1;
    rclick2 = -1;
    rclick1_touchID = -1;
    lclick1 = -1;
    lclick2 = -1;
    _st1plantnum = nullptr;
    _st2plantnum = nullptr;
    _st3plantnum = nullptr;
    _st4plantnum = nullptr;
    
    lclick1_touchID = -1;
    shadowClicked = false;
    _shadowSelectors.clear();
    pinched = false;
    _complete = false;

    
    // clear up the objects
    for (auto &c : _clouds){
        c->dispose();
    }
    _clouds.clear();
    for (auto &p : _plants){
        p->dispose();
    }
    pinchedCloud1 = nullptr;
    pinchedCloud2 = nullptr;
    clickedShadowCloud = nullptr;
    clicked_cloud = nullptr;
    masterCloudNode = nullptr;
    _memory = nullptr;
    _rainNode = nullptr;
    currentPlant = nullptr;
    _plants.clear();
    _particles.clear();
    _selectors.clear();
    cloudsToSplit.clear();
    cloudsToSplit_temp.clear();
    touchIDs_started_outside.clear();
//    _board->dispose();
    _world = nullptr;
    _vmainbutton = nullptr;
    _vresetbutton = nullptr;
    _pmainbutton = nullptr;
    _presetbutton = nullptr;
    _continuebutton = nullptr;
    _endscreen_nostar = nullptr;
    _endscreen_3star = nullptr;
    _endscreen_2star = nullptr;
    _endscreen_1star = nullptr;
    _pauseboard = nullptr;
    if (_pauseButton != nullptr){
        _pauseButton->deactivate();
    }
    _pauseButton = nullptr;
    if (_tcontinuebutton != nullptr){
        _tcontinuebutton->deactivate();
        continueSelected();
    }
    _tutorialpage = nullptr;
    if (_level) _level->dispose();
    
    if (_worldnode) _worldnode->removeAllChildren();
    if (_debugnode) _debugnode->removeAllChildren();
    _worldnode = nullptr;
    _level = nullptr;
    _rootnode = nullptr;
    _debugnode = nullptr;
    _debug = false;
    _active = false;
    Scene::dispose();
}


#pragma mark -
#pragma mark Level Layout

/**
 * Resets the status of the game so that we can play again.
 *
 * This method disposes of the world and creates a new one.
 */
void GameScene::reset() {
    
    
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
    //Change from draw coordinates to Box2D coordinates
    float w = SCENE_WIDTH/_scale;
    float h = SCENE_HEIGHT/_scale;
    //Define wall vertices in terms of the width and height of the playable area
    float WALL1[] = { w,h/5.0f, w,0.9f*h/5.0f, 0.0f,0.9f*h/5.0f, 0.0f,h/5.0f }; //Bottom
    float WALL2[] = { w,h, w,h*0.97f, 0.0f,h*0.97f, 0.0f,h }; //Top
    float WALL3[] = { 0.0f,h*0.97f, 0.0f,h/3.0f, 0.025f*w,h/3.0f, 0.025f*w,h*0.97f }; //Left
    float WALL4[] = { w*0.975f,h*0.97f, w*0.975f,h/3.0f, w,h/3.0f, w,h*0.97f }; //Right
    
#pragma mark : Wall polygon 1
    // Create ground pieces
    // All walls share the same texture
    std::shared_ptr<Texture> image  = _assets->get<Texture>("earth");
    std::string wname = "wall";
    
//     Create the polygon outline
    Poly2 wall1(WALL1,8);
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
    sprite->setColor(Color4::CLEAR);
    addObstacle(_worldnode, wallobj1,sprite,Z_BACKGROUND);  // All walls share the same texture

#pragma mark : Wall polygon 2
    Poly2 wall2(WALL2,8);
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
    sprite->setColor(Color4::CLEAR);
    addObstacle(_worldnode, wallobj2,sprite,Z_BACKGROUND);  // All walls share the same texture

#pragma mark : Wall polygon 3
    Poly2 wall3(WALL3,8);
    triangulator.set(wall3);
    triangulator.calculate();
    wall3.setIndices(triangulator.getTriangulation());
    wall3.setType(Poly2::Type::SOLID);

    std::shared_ptr<PolygonObstacle> wallobj3 = PolygonObstacle::alloc(wall3);
    wallobj3->setDebugColor(STATIC_COLOR);
    wallobj3->setName(wname);

    // Set the physics attributes
    wallobj3->setBodyType(b2_staticBody);
    wallobj3->setDensity(BASIC_DENSITY);
    wallobj3->setFriction(BASIC_FRICTION);
    wallobj3->setRestitution(BASIC_RESTITUTION);

    // Add the scene graph nodes to this object
    wall3 *= _scale;
    sprite = PolygonNode::allocWithTexture(image,wall3);
    sprite->setColor(Color4::CLEAR);
    addObstacle(_worldnode, wallobj3,sprite,Z_BACKGROUND);  // All walls share the same texture

#pragma mark : Wall polygon 4
    Poly2 wall4(WALL4,8);
    triangulator.set(wall4);
    triangulator.calculate();
    wall4.setIndices(triangulator.getTriangulation());
    wall4.setType(Poly2::Type::SOLID);

    std::shared_ptr<PolygonObstacle> wallobj4 = PolygonObstacle::alloc(wall4);
    wallobj4->setDebugColor(STATIC_COLOR);
    wallobj4->setName(wname);

    // Set the physics attributes
    wallobj4->setBodyType(b2_staticBody);
    wallobj4->setDensity(BASIC_DENSITY);
    wallobj4->setFriction(BASIC_FRICTION);
    wallobj4->setRestitution(BASIC_RESTITUTION);

    // Add the scene graph nodes to this object
    wall4 *= _scale;
    sprite = PolygonNode::allocWithTexture(image,wall4);
    sprite->setColor(Color4::CLEAR);
    addObstacle(_worldnode, wallobj4,sprite,Z_BACKGROUND);  // All walls share the same texture
    
    //Add all the buttons for pause
    auto pauseButtonNode = PolygonNode::allocWithTexture(_assets->get<Texture>("pauseButton"));
    pauseButtonNode->setContentSize(Size(3, 3)*_scale);
    pauseButtonNode->setName("pauseButton");
    pauseButtonNode->setPosition(0, 0);
    
    _pauseButton = Button::alloc(pauseButtonNode);
    _pauseButton->deactivate();
    _pauseButton->setVisible(false);
    _pauseButton->setAnchor(Vec2::ANCHOR_TOP_LEFT);

    _pauseButton->setPosition(0, dimen.height - abs(SCENE_HEIGHT/2.0f - dimen.height/2.0f));
    _pauseButton->setListener([=](const std::string& name, bool down) {
//        std::shared_ptr<Sound> source = _assets->get<Sound>(BUTTON_EFFECT);
//        AudioChannels::get()->playEffect(BUTTON_EFFECT,source,false,EFFECT_VOLUME);
        removeVictoryDisplay();
        displayPause();
        _paused = true;
        _mainSelected = false;
        _resetSelected = false;
        _continueSelected = false;
        AudioChannels::get()->setMusicVolume(MUSIC_VOLUME);
    });
    _levelworldnode->addChild(_pauseButton, Z_PAUSE);
    
    auto tutorialNode = Node::alloc();
    tutorialNode->setName("tutorial Node");
    
    
    //Must add this node right before the actual clouds
    CULogGLError();
    masterCloudNode = CloudNode::alloc(_scale, dimenWithIndicator, masterParticleQuad, particleFactor, true);
    _levelworldnode->addChildWithName(masterCloudNode, "masterCloudNode", Z_CLOUD);

    int i = 0;
    Vec2 offset((dimen.width-SCENE_WIDTH)/2.0f,(dimen.height-SCENE_HEIGHT)/2.0f);
    for(auto it = _clouds.begin(); it != _clouds.end(); ++it) {
        CULog("setting particles for the clouds");
        std::shared_ptr<Cloud> cloud = *it;
        cloud->setDrawScale(_scale);
        //        cloud->setCloudSizeScale(1);
        auto cloudNode = CloudNode::alloc(_scale, dimenWithIndicator, masterParticleQuad, particleFactor, false);
        masterCloudNode->subCloudNodes.push_back(shared_ptr<CloudNode>(cloudNode));
        cloudNode->setName(cloud->getName());
        cloudNode->setDrawScale(_scale);
        Vec2 offset((dimen.width-SCENE_WIDTH)/2.0f,(dimen.height-SCENE_HEIGHT)/2.0f);
        vector<shared_ptr<Node>> newNodes = cloud->setSceneNode(cloudNode, -_scale*Vec2(0, GRID_HEIGHT + DOWN_LEFT_CORNER_Y) - offset, _assets->get<Texture>("cloudFace"), _assets->get<Texture>("shadow"), _assets->get<Texture>("rain-film"), _assets->get<Texture>("lightning-film"));
        
        for (shared_ptr<Node> n : newNodes){
            int zToUse = 0;
            if(n->getName() == "shadow"){zToUse = Z_SHADOW;}
            else if(n->getName() == "lightning"){zToUse = Z_LIGHTNING;}
            else if(n->getName() == "rainAnimation"){zToUse = Z_RAIN;}
            _levelworldnode->addChildWithName(n, n->getName() + cloudNode->getName(), zToUse);
        }
        addObstacle(_levelworldnode, cloud, cloudNode, Z_CLOUD);
        _levelworldnode->sortZOrder();
        i++;
    }
    
    _pauseboard = std::dynamic_pointer_cast<Node>(_assets->get<Node>("pause_pboard"));
    _pauseboard->setVisible(false);
    
    _endscreen_nostar = std::dynamic_pointer_cast<Node>(_assets->get<Node>("pause_vicboard"));
    _endscreen_nostar->setVisible(false);
    
    _endscreen_1star = std::dynamic_pointer_cast<Node>(_assets->get<Node>("pause_vicboard-star1"));
    _endscreen_1star->setVisible(false);
    
    _endscreen_2star = std::dynamic_pointer_cast<Node>(_assets->get<Node>("pause_vicboard-star2"));
    _endscreen_2star->setVisible(false);
    
    _endscreen_3star = std::dynamic_pointer_cast<Node>(_assets->get<Node>("pause_vicboard-star3"));
    _endscreen_3star->setVisible(false);
    
    auto layer = _assets->get<Node>("pause");
    if (_pmainbutton == nullptr || _presetbutton == nullptr || _continuebutton == nullptr || _vmainbutton == nullptr || _vresetbutton == nullptr){
        CULog("adding the buttons to the worldnode");
        layer->setContentSize(dimen);
        layer->doLayout();
        _levelworldnode->addChild(layer, 100);
    
        _pmainbutton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("pause_pboard_main"));
        _pmainbutton->deactivate();
        _pmainbutton->setVisible(false);
        _pmainbutton->setListener([=](const std::string& name, bool down) {
//            std::shared_ptr<Sound> source = _assets->get<Sound>(BUTTON_EFFECT);
//            AudioChannels::get()->playEffect(BUTTON_EFFECT,source,false,EFFECT_VOLUME);
            this->_active = down;
            _mainSelected = true;
        });
        
        _vmainbutton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("pause_main"));
        _vmainbutton->deactivate();
        _vmainbutton->setVisible(false);
        _vmainbutton->setListener([=](const std::string& name, bool down) {
//            std::shared_ptr<Sound> source = _assets->get<Sound>(BUTTON_EFFECT);
//            AudioChannels::get()->playEffect(BUTTON_EFFECT,source,false,EFFECT_VOLUME);
            this->_active = down;
            _mainSelected = true;
        });
        
        _presetbutton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("pause_pboard")->getChildByName("restart"));
        _presetbutton->deactivate();
        _presetbutton->setVisible(false);
        _presetbutton->setListener([=](const std::string& name, bool down) {
//            std::shared_ptr<Sound> source = _assets->get<Sound>(BUTTON_EFFECT);
//            AudioChannels::get()->playEffect(BUTTON_EFFECT,source,false,EFFECT_VOLUME);
            this->_active = down;
            _resetSelected = true;
        });
        
        _vresetbutton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("pause_restart"));
        _vresetbutton->deactivate();
        _vresetbutton->setVisible(false);
        _vresetbutton->setListener([=](const std::string& name, bool down) {
//            std::shared_ptr<Sound> source = _assets->get<Sound>(BUTTON_EFFECT);
//            AudioChannels::get()->playEffect(BUTTON_EFFECT,source,false,EFFECT_VOLUME);
            this->_active = down;
            _resetSelected = true;
        });
        
        _continuebutton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("pause_pboard")->getChildByName("continue"));
        _continuebutton->deactivate();
        _continuebutton->setVisible(false);
        _continuebutton->setListener([=](const std::string& name, bool down) {
//            std::shared_ptr<Sound> source = _assets->get<Sound>(BUTTON_EFFECT);
//            AudioChannels::get()->playEffect(BUTTON_EFFECT,source,false,EFFECT_VOLUME);
            _continueSelected = true;
            AudioChannels::get()->setMusicVolume(MUSIC_VOLUME);
        });
    }

    if (_levelId == "level1" || _levelId == "level2" || _levelId == "level3" || _levelId == "level5" || _levelId == "level6"){
        CULog("setting tutorial");
        image = _assets->get<Texture>(_levelId + "-tutorial");
        _tutorialpage = PolygonNode::allocWithTexture(image);
        _tutorialpage->setContentSize(dimen/1.2);
        _tutorialpage->setVisible(false);
        _tutorialpage->setPosition(SCENE_WIDTH/2, SCENE_HEIGHT/2);
        tutorialNode->addChild(_tutorialpage);
        
        auto tutorialButtonNode = PolygonNode::allocWithTexture(_assets->get<Texture>("pause_continuebutton"));
        tutorialButtonNode->setContentSize(Size(3, 3)*_scale);
        tutorialButtonNode->setName("tutorial back button");
        tutorialButtonNode->setPosition(0, 0);
        
        _tcontinuebutton = Button::alloc(tutorialButtonNode);
        _tcontinuebutton->setScale(0.8);
        _tcontinuebutton->setPosition(SCENE_WIDTH/2 + 200, SCENE_HEIGHT/2 - 200);
        _tcontinuebutton->setListener([=](const std::string& name, bool down) {
            CULog("button pressed");
            _tutorialpage->setVisible(false);
            _tutorialshown = true;
            _continueSelected = true;
        });
        _tcontinuebutton->setVisible(false);
        _tcontinuebutton->deactivate();
        tutorialNode->addChild(_tcontinuebutton);
        _levelworldnode->addChild(tutorialNode, 999);
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
    auto p = obj->getPosition();
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

void GameScene::displayPause(){
//    CULog("displaying pause button");
    _continuebutton->setVisible(true);
    _continuebutton->activate(80);
    _presetbutton->setVisible(true);
    _presetbutton->activate(81);
    _pmainbutton->setVisible(true);
    _pmainbutton->activate(82);
    _pauseboard->setVisible(true);
}

void GameScene::displayVictory(){
    // display right end screen here
    _levelworldnode->sortZOrder();
    AudioChannels::get()->setMusicVolume(MUSIC_REDUCED_VOLUME);
    float correction = dimen.height/SCENE_HEIGHT;
    float upFactor = 1.2f/correction;
    float downFactor = 0.8f/correction;
    float downFactor2 = 0.75f/correction;
    float lowestFactor = 0.57f/correction;
    float leftFactor = 1.0f;
    float rightFactor = 1.3f;
    float rightestFactor = 1.17f;
    int percent = (int)(100.0f * float(_level->getPlantScore())/float(_level->getPossibleMaxScore()));
    _st1plantnum = Label::alloc(std::to_string(_level->getEachStagePlantNum(1)), _assets->get<Font>(PRIMARY_FONT));
    _st1plantnum->setForeground(Color4(99, 59, 7));
    _st1plantnum->setAnchor(0.5, 0.5);
    _st1plantnum->setScale(0.7);
    _st1plantnum->setPosition(_endscreen_nostar->getPositionX()*leftFactor,
                              _endscreen_nostar->getPositionY()*upFactor);
    _levelworldnode->addChild(_st1plantnum, Z_UI);
    
    _st2plantnum =  Label::alloc(std::to_string(_level->getEachStagePlantNum(2)), _assets->get<Font>(PRIMARY_FONT));
    _st2plantnum->setForeground(Color4(99, 59, 7));
    _st2plantnum->setAnchor(0.5, 0.5);
    _st2plantnum->setScale(0.7);
    _st2plantnum->setPosition(_endscreen_nostar->getPositionX()*rightFactor,
                              _endscreen_nostar->getPositionY()*upFactor);
    _levelworldnode->addChild(_st2plantnum,  Z_UI);
    
    _st3plantnum =  Label::alloc(std::to_string(_level->getEachStagePlantNum(3)), _assets->get<Font>(PRIMARY_FONT));
    _st3plantnum->setForeground(Color4(99, 59, 7));
    _st3plantnum->setAnchor(0.5, 0.5);
    _st3plantnum->setScale(0.7);
    _st3plantnum->setPosition(_endscreen_nostar->getPositionX()*leftFactor,
                              _endscreen_nostar->getPositionY()*downFactor);
    _levelworldnode->addChild(_st3plantnum,  Z_UI);
    
    _st4plantnum =  Label::alloc(std::to_string(_level->getEachStagePlantNum(4)), _assets->get<Font>(PRIMARY_FONT));
    _st4plantnum->setForeground(Color4(99, 59, 7));
    _st4plantnum->setAnchor(0.5, 0.5);
    _st4plantnum->setScale(0.7);
    _st4plantnum->setPosition(_endscreen_nostar->getPositionX()*rightFactor,
                              _endscreen_nostar->getPositionY()*downFactor2);
    _levelworldnode->addChild(_st4plantnum,  Z_UI);

    _finalScore = Label::alloc(std::to_string(percent) + "%", _assets->get<Font>(PRIMARY_FONT));
    _finalScore->setForeground(Color4(99, 59, 7));
    _finalScore->setAnchor(0.5, 0.5);
    _finalScore->setScale(0.7);
    _finalScore->setPosition(_endscreen_nostar->getPositionX()*rightestFactor,
                             _endscreen_nostar->getPositionY()*lowestFactor);
    _levelworldnode->addChild(_finalScore,  Z_UI);
    
    if (percent == 0){
        _endscreen_nostar->setPosition(_endscreen_nostar->getPositionX(),
                                       _endscreen_nostar->getPositionY()/correction);
        _endscreen_nostar->setVisible(true);
        std::shared_ptr<Sound> source = _assets->get<Sound>(LOSE_EFFECT);
        AudioChannels::get()->playEffect(LOSE_EFFECT,source,false,EFFECT_VOLUME);
    }
    else if (percent <= 60){
        _endscreen_1star->setPosition(_endscreen_1star->getPositionX(),
                                       _endscreen_1star->getPositionY()/correction);
        _endscreen_1star->setVisible(true);
        std::shared_ptr<Sound> source = _assets->get<Sound>(LOSE_EFFECT);
        AudioChannels::get()->playEffect(LOSE_EFFECT,source,false,EFFECT_VOLUME);
    }
    else if (percent != 100){
        _endscreen_2star->setPosition(_endscreen_2star->getPositionX(),
                                      _endscreen_2star->getPositionY()/correction);
        _endscreen_2star->setVisible(true);
        std::shared_ptr<Sound> source = _assets->get<Sound>(ENDGAME_EFFECT);
        AudioChannels::get()->playEffect(ENDGAME_EFFECT,source,false,EFFECT_VOLUME);
    }
    else {
        _endscreen_3star->setPosition(_endscreen_3star->getPositionX(),
                                      _endscreen_3star->getPositionY()/correction);
        _endscreen_3star->setVisible(true);
        std::shared_ptr<Sound> source = _assets->get<Sound>(ENDGAME_EFFECT);
        AudioChannels::get()->playEffect(ENDGAME_EFFECT,source,false,EFFECT_VOLUME);
    }
    _vresetbutton->setVisible(true);
    _vresetbutton->activate(90);
    _vmainbutton->setVisible(true);
    _vmainbutton->activate(91);
}


#pragma mark -
#pragma mark Physics Handling

void GameScene::combineByPinch(Cloud* cind1, Cloud* cind2){
    if (cind1 == nullptr || cind2 == nullptr) {
        return;
    }
    auto c1p = cind1->getPosition();
    auto c2p = cind2->getPosition();
    CULog("combine by pinch");
    auto c1p_rad = sqrt(pow(CLOUD_DEFAULT_SIZE.width/2*cind1->getCloudSizeScale(), 2) + pow(CLOUD_DEFAULT_SIZE.height/2*cind1->getCloudSizeScale(), 2));
    auto c2p_rad = sqrt(pow(CLOUD_DEFAULT_SIZE.width/2*cind2->getCloudSizeScale(), 2) + pow(CLOUD_DEFAULT_SIZE.height/2*cind2->getCloudSizeScale(), 2));
    
    float offset = PINCH_OFFSET/_scale;
    
    if (c1p.distance(c2p) > c1p_rad + c2p_rad + offset) return;
    if (cind1->getCloudSizeScale() >= 5 && cind2->getCloudSizeScale() >= 5){
        std::shared_ptr<Sound> source = _assets->get<Sound>(FAIL_EFFECT);
        AudioChannels::get()->playEffect(FAIL_EFFECT,source,false,EFFECT_VOLUME);
        return;
    }
    
    Cloud * cloudToGetLarger = cind1;
    Cloud * cloudToBeDeleted = cind2;
    if (cind2->getCloudSizeScale() < 5 && cind2->getCloudSizeScale() < cind1->getCloudSizeScale()){
        cloudToGetLarger = cind2;
        cloudToBeDeleted = cind1;
    }

    Vec2 pinchpos = _worldnode->screenToNodeCoords(pinchPos);
    pinchpos /= _scale;
    
    if (pinchpos.distance(c1p) <= c1p_rad + offset && pinchpos.distance(c2p) <= c2p_rad + offset){
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
        std::shared_ptr<Sound> source = _assets->get<Sound>(COMBINE_EFFECT);
        AudioChannels::get()->playEffect(COMBINE_EFFECT,source,false,EFFECT_VOLUME);
    }
}

void GameScene::checkForCombining(Obstacle * ob){
//    CULog("check for combining");
    if (pinched && ob){
        if (pinchedCloud1 == nullptr) {
            pinchedCloud1 = (Cloud *) ob;
        }
        else if (pinchedCloud2 == nullptr && ob->getName() != pinchedCloud1->getName()) {
            pinchedCloud2 = (Cloud *) ob;
        }
        if (pinchedCloud2 != nullptr && pinchedCloud1 != nullptr) {
            combineByPinch(pinchedCloud1, pinchedCloud2);
            pinched = false;
            pinchedCloud1 = nullptr;
            pinchedCloud2 = nullptr;
        }
    }
}

void GameScene::checkForRain(Obstacle * o, long touchID){
    if (o) {
        if (rclick1 == -1){
            rclick1 = ticks;
            clicked_cloud = o;
            rclick1_touchID = touchID;
        }
        else if (rclick2 == -1){
            rclick2 = ticks;
            long gap = rclick2 - rclick1;
            if (gap <= 60 && clicked_cloud && clicked_cloud->getName() == o->getName()){
            // if (gap <= 60 && clicked_cloud && clicked_cloud->getName() == o->getName() && touchID != rclick1_touchID){
                gesCoolDown = ticks;
                makeRain(o);
            }
            rclick1 = -1;
            rclick2 = -1;
            rclick1_touchID = -1;
            clicked_cloud = nullptr;
        }
    }
}

void GameScene::makeLightning(Obstacle * ob){
    if (!ob) return;
    auto c = (Cloud *) ob;
    if (!c->isRainCloud()) return;
    c->setLightning();
    std::shared_ptr<Sound> source = _assets->get<Sound>(THUNDER_EFFECT);
    AudioChannels::get()->playEffect(THUNDER_EFFECT,source,false,EFFECT_VOLUME);
    bool lightning;
    shared_ptr<Node> thisNode;
//    for (int i=0; i < GRID_NUM_X; i++){
//        for (int j=0; j < GRID_NUM_Y; j++){
//            lightning = false;
//            thisNode = _board->getNodeAt(i, j);
//            lightning = c->shadowCheck(_worldnode, thisNode);
//
//            if (lightning) {
//                for (std::shared_ptr<Pest> p : _level->getPests()){
//                    if (p != nullptr && p->getTarget() == Vec2(i, j)){
//                        CULog("Set scared!");
//                        p->setScared(true);
//                    }
//                }
//            }
            
                for (std::shared_ptr<Pest> p : _level->getPests()){
                    if (c->lightningCheck(_worldnode, _rootnode, p->getNode())){
                        CULog("Set scared!");
                        p->setScared(true);
                    }
                }
            
//        }
//    }
}

void GameScene::checkForLightning(Obstacle * o, long touchID){
    if (o) {
        if (lclick1 == -1){
            lclick1 = ticks;
            clickedShadowCloud = o;
            lclick1_touchID = touchID;
        }
        else if (lclick2 == -1){
            lclick2 = ticks;
            long gap = lclick2 - lclick1;
            if (gap <= 60 && clickedShadowCloud && clickedShadowCloud->getName() == o->getName() ){
            // if (gap <= 60 && clickedShadowCloud && clickedShadowCloud->getName() == o->getName() && touchID != lclick1_touchID){
                makeLightning(o);
                gesCoolDown = ticks;
            }
            lclick1 = -1;
            lclick2 = -1;
            lclick1_touchID = -1;
            clickedShadowCloud = nullptr;
        }
    }
}

Obstacle * GameScene::getSelectedObstacle(Vec2 pos, long touchID){
    Obstacle * ob = nullptr;
    shadowClicked = false;
    for (auto &c : _clouds){
        auto left = c->getPosition().x - c->getWidth()/2;
        auto right = c->getPosition().x + c->getWidth()/2;
        auto up = c->getPosition().y + c->getHeight()/2;
        auto down = c->getPosition().y - c->getHeight()/2;        
        Vec2 shadowNodePos = _worldnode->nodeToWorldCoords(c->getShadowNode()->getPosition());
        Vec2 shadowClickPos = _worldnode->nodeToWorldCoords(_scale*pos);

        int p = (pow((shadowClickPos.x - shadowNodePos.x), 2) / pow(c->getShadowNode()->getWidth()/2, 2)) + (pow((shadowClickPos.y - shadowNodePos.y), 2) / pow(c->getShadowNode()->getHeight()/2, 2));
        
        if (left <= pos.x && pos.x <= right && down <= pos.y && pos.y <= up){
            ob = (Obstacle *) c.get();
            break;
        }
        else if (p < 1){
            shadowClicked = true;
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
    
    // Process the toggled key commands
    if (_input.didDebug()) { setDebug(!isDebug()); }
    if (_input.didExit())  {
        Application::get()->quit();
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

    for (auto &c : _clouds) {
        if (c->isRaining()) {
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
                 for (std::shared_ptr<Plant> p : _plants){
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
    for (auto &plant : _level->getPlants()){
        plant->updateState(ticks);
        plant->update(dt);
   }

    for (auto &pest : _level->getPests()){
        if (ticks % 50*((int)pest->getSpeed()) == 0) {
            int targetY = pest->getTarget().y;
            int targetX = pest->getTarget().x;
            for(auto &plant : _plants) {
                if (plant->getX() == targetX && plant->getY() == targetY &&  plant->getStage() > 2) {
                    pest->walk();
                    break;
                }
            }
        }
    }

    for (auto &pest : _level->getPests()){
        pest->update(dt);
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
                if (ob){
                    if (!shadowClicked) {
                        checkForCombining(ob);
                        ((Cloud *) ob)->setTargetPos(pos);
                        _selectors.insert({touchID, ob});
                    }
                    else {
                        _shadowSelectors.insert({touchID, ob});
                    }
                }
            }
            else{
                ob =_selectors.at(touchID);
                if (ob == nullptr){
                    ob = getSelectedObstacle(pos, touchID);
                }
                if (ob && !shadowClicked) {
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
                        ((Cloud *) ob)->setTargetPos(pos);
                    } else {
                        ((Cloud *) ob)->setTargetPos(((Cloud *) ob)->getPosition());
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
//            if (!(pos.y <= 17 && pos.y >= 4 && pos.x >= 2 && pos.x <= 30)) {
//                continue;
//                CULog("out of boundary");
//            }
            if (cloudsToSplit_temp.count(touchID)){
                auto cloudPos = cloudsToSplit_temp.at(touchID)->getPosition();
                if (_selectors.count(touchID) && abs(pos.y - cloudPos.y) >= SWIPE_VERT_OFFSET){
                    CULog("inserting");
                    cloudsToSplit.insert({touchID, cloudsToSplit_temp.at(touchID)});
                }
                cloudsToSplit_temp.erase(touchID);
            }
//            if (!(pos.y <= 17 && pos.y >= 4 && pos.x >= 2 && pos.x <= 30)) {
//                continue;
//                CULog("out of boundary");
//            }
            Obstacle * o;
            if (_selectors.count(touchID)){
                o = _selectors.at(touchID);
                if (o != nullptr){
                    if (ticks - gesCoolDown >= GES_COOLDOWN){
                        checkForRain(o, touchID);
                    }
                    ((Cloud *) o)->setTargetPos(((Cloud *) o)->getPosition());
                }
                _selectors.at(touchID) = nullptr;
                _selectors.erase(touchID);
            }
            if (_shadowSelectors.count(touchID)){
                o = _shadowSelectors.at(touchID);
                if (o != nullptr && ticks - gesCoolDown >= GES_COOLDOWN){
                    checkForLightning(o, touchID);
                }
                _shadowSelectors.at(touchID) = nullptr;
                _shadowSelectors.erase(touchID);
            }
            if (touchIDs_started_outside.count(touchID)){
                touchIDs_started_outside.erase(touchID);
            }
            _input.removeFromTouchID(touchID);
        }
    }
    
    // process clouds to split
    splitClouds();
    createResourceClouds();
    cloudsToSplit.clear();
    
    processRemoval();
    
    for (auto &c : _clouds) {
        if (c != nullptr) {
            auto cloudNode = _levelworldnode->getChildByName(c->getName());
            cloudNode->setContentSize(c->getCloudSize());
            float scale = c->getCloudSizeScale();
            c->setCloudSizeScale(scale);
            
            c->setSize(CLOUD_DEFAULT_SIZE*scale);
            c->getBody()->DestroyFixture(&c->getBody()->GetFixtureList()[0]);
            b2Shape * shape = c->getBody()->GetFixtureList()[0].GetShape();
            b2PolygonShape * polyshape = (b2PolygonShape *) shape;
            polyshape->SetAsBox(c->getSize().width/2, c->getSize().height/2);
            c->getBody()->CreateFixture(polyshape, 3);
            
            auto targetPos = c->getTargetPos();
            auto cloudPos = c->getPosition();

            c->setLinearVelocity(5*(targetPos->x - cloudPos.x), 5*(targetPos->y - cloudPos.y));
        }
    }
    
    _level->update(ticks);
    _world->update(dt);
    
    if (_level->isOver()){
        CULog("display victory screen");
        _pauseButton->deactivate();
        removePauseDisplay();
        _mainSelected =false;
        _resetSelected=false;
        _continueSelected= false;
        displayVictory();
        _paused = true;
    }
    else {
        _pauseButton->activate(10);
        _pauseButton->setVisible(true);
    }
    
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
            c->dispose();
            masterCloudNode->subCloudNodes.erase(masterCloudNode->subCloudNodes.begin() + i);
            _clouds.erase(_clouds.begin() + i);
        }
    }
}

void GameScene::makeRain(Obstacle * ob){
    if (!ob) return;
    auto c = (Cloud *) ob;
    // CULog("toggle rain");
    std::shared_ptr<Sound> source = _assets->get<Sound>(RAIN_EFFECT);
    c->toggleRain(source);
}

void GameScene::splitClouds(){
//    if (splitCoolDown == -1) splitCoolDown = ticks;
    for (auto &ic : cloudsToSplit){
        // split clouds here
        Cloud * c =(Cloud *)(ic.second);
        
        // too small to split
        if (c->getCloudSizeScale()/sqrt(2) < 0.5) {
            std::shared_ptr<Sound> source = _assets->get<Sound>(FAIL_EFFECT);
            AudioChannels::get()->playEffect(FAIL_EFFECT,source,false,EFFECT_VOLUME);
            continue;
        }

        c->setCloudSizeScale(c->getCloudSizeScale()/sqrt(2));
        auto cloudPos = c->getPosition();
        
        _max_cloud_id++;
        Vec2 new_pos;
        if (cloudPos.x - c->getWidth()*1.5 - 2 > 0){
            new_pos = Vec2(cloudPos.x - c->getWidth()/2 - 1, cloudPos.y);
        }
        else{
            new_pos = Vec2(cloudPos.x + c->getWidth()/2 + 1, cloudPos.y);
        }
        
        std::shared_ptr<Cloud> new_cloud = _level->createNewCloud(_max_cloud_id, new_pos);
        new_cloud->setCloudSizeScale(c->getCloudSizeScale());
        new_cloud->setDrawScale(_scale);

        auto cloudNode = CloudNode::alloc(_scale, dimenWithIndicator, masterParticleQuad, particleFactor, false);
        masterCloudNode->subCloudNodes.push_back(shared_ptr<CloudNode>(cloudNode));
        cloudNode->setName(new_cloud->getName());
        cloudNode->setPosition(Vec2(5, 5));
        cloudNode->setDrawScale(_scale);

        Vec2 offset((dimen.width-SCENE_WIDTH)/2.0f,(dimen.height-SCENE_HEIGHT)/2.0f);
        vector<shared_ptr<Node>> newNodes = new_cloud->setSceneNode(cloudNode, -_scale*Vec2(0, GRID_HEIGHT + DOWN_LEFT_CORNER_Y) - offset, _assets->get<Texture>("cloudFace"), _assets->get<Texture>("shadow"), _assets->get<Texture>("rain-film"), _assets->get<Texture>("lightning-film"));
        
        for (shared_ptr<Node> n : newNodes){
            int zToUse = 0;
            if(n->getName() == "shadow"){zToUse = Z_SHADOW;}
            else if(n->getName() == "lightning"){zToUse = Z_LIGHTNING;}
            else if(n->getName() == "rainAnimation"){zToUse = Z_RAIN;}
            _levelworldnode->addChildWithName(n, n->getName() + cloudNode->getName(), zToUse);
        }
        new_cloud->setDebugColor(DYNAMIC_COLOR);
        new_cloud->setDebugScene(_debugnode);

//        CULog("created new cloud %i", new_cloud->getId());
        _clouds.push_back(new_cloud);
        addObstacle(_levelworldnode, new_cloud, cloudNode, Z_CLOUD);
        std::shared_ptr<Sound> source = _assets->get<Sound>(SPLIT_EFFECT);
        AudioChannels::get()->playEffect(SPLIT_EFFECT,source,false,EFFECT_VOLUME);
        _levelworldnode->sortZOrder();
    }
}

void GameScene::createResourceClouds(){
    auto cloudInfo = _level->getNewClouds();

    while (!cloudInfo.empty())
    {
        auto cloud = cloudInfo.back();
        _max_cloud_id++;
        Vec2 new_pos = std::get<0>(cloud);
        float cloud_size = std::get<1>(cloud);
        std::shared_ptr<Cloud> new_cloud = _level->createNewCloud(_max_cloud_id, new_pos);
        new_cloud->setDrawScale(_scale);
//        new_cloud->setCloudSizeScale(1/sqrt(2));

        auto cloudNode = CloudNode::alloc(_scale, dimenWithIndicator, masterParticleQuad, particleFactor, false);
        masterCloudNode->subCloudNodes.push_back(shared_ptr<CloudNode>(cloudNode));
        cloudNode->setName(new_cloud->getName());
        cloudNode->setPosition(new_pos);
        cloudNode->setDrawScale(_scale);

        Vec2 offset((dimen.width-SCENE_WIDTH)/2.0f,(dimen.height-SCENE_HEIGHT)/2.0f);
        vector<shared_ptr<Node>> newNodes = new_cloud->setSceneNode(cloudNode, -_scale*Vec2(0, GRID_HEIGHT + DOWN_LEFT_CORNER_Y) - offset, _assets->get<Texture>("cloudFace"), _assets->get<Texture>("shadow"), _assets->get<Texture>("rain-film"), _assets->get<Texture>("lightning-film"));
        
        for (shared_ptr<Node> n : newNodes){
            int zToUse = 0;
            if(n->getName() == "shadow"){zToUse = Z_SHADOW;}
            else if(n->getName() == "lightning"){zToUse = Z_LIGHTNING;}
            else if(n->getName() == "rainAnimation"){zToUse = Z_RAIN;}
            _levelworldnode->addChildWithName(n, n->getName() + cloudNode->getName(), zToUse);
        }
        new_cloud->setDebugColor(DYNAMIC_COLOR);
        new_cloud->setDebugScene(_debugnode);

        _clouds.push_back(new_cloud);
        addObstacle(_levelworldnode, new_cloud, cloudNode, Z_CLOUD);
        _levelworldnode->sortZOrder();
        cloudInfo.pop_back();
        std::shared_ptr<Sound> source = _assets->get<Sound>(CREATE_EFFECT);
        AudioChannels::get()->playEffect(CREATE_EFFECT,source,false,EFFECT_VOLUME);
    }
    _level->setNewClouds(cloudInfo);
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

bool GameScene::tutorialDisplay(){
    if (_levelId == "level1" || _levelId == "level2" || _levelId == "level3" || _levelId == "level5" || _levelId == "level6"){
        _tutorialpage->setVisible(true);
        _tcontinuebutton->activate(101);
        _tcontinuebutton->setVisible(true);
        return true;
    }
    return false;
}


/**
 * Returns the active screen size of this scene.
 *
 * This method is for graceful handling of different aspect
 * ratios
 */
Vec3 GameScene::computeActiveSize() const {
    Size displaySize = Application::get()->getDisplaySize();
    Size dimen = displaySize;
    float thirdCoord;
    float ratio1 = dimen.width/dimen.height;
    float ratio2 = ((float)SCENE_WIDTH)/((float)SCENE_HEIGHT);
    bool setQuad = false;
    if (particleFactor == 0.0f){setQuad = true;}
    if (ratio1 < ratio2) {
        dimen *= SCENE_WIDTH/dimen.width;
        thirdCoord = 0.0f;
        particleFactor = PARTICLE_FACTOR_W;
    }else if (ratio1 > ratio2) {
        dimen *= SCENE_HEIGHT/dimen.height;
        thirdCoord = 1.0f;
        particleFactor = PARTICLE_FACTOR_H;
    }else {
        dimen *= SCENE_HEIGHT/dimen.height;
        thirdCoord = 0.0f;
        particleFactor = PARTICLE_FACTOR_W;
    }
    if (setQuad){
        for(int i = 0; i < 4; i++){
            masterParticleQuad[i*4] = (particleFactor)*masterParticleQuad[i*4];
            masterParticleQuad[i*4 + 1] = (particleFactor)*masterParticleQuad[i*4 + 1];
        }
    }
    return Vec3(dimen.width, dimen.height, thirdCoord);
}
