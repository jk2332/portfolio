//
//  LevelModel.cpp
//  WeatherDefender
//
//  Created by Zaibo  Wang on 3/26/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include <cugl/assets/CUJsonLoader.h>
#include "LevelModel.hpp"
#include <map>
#include "Constants.hpp"

#pragma mark -
#pragma mark Static Constructors

using namespace std;

map<string, int> shadeMap = {{"tomato", 40}, {"corn", 25}};
map<string, int> rainMap = {{"tomato", 25}, {"corn", 10}};
float CLOUD2[] = { 0.f, 0.f, 5.1f, 0.f, 5.1f, 2.6f, 0.f, 2.6};

/**
 * Creates a new, empty level.
 */
LevelModel::LevelModel(void) : Asset(),
_root(nullptr),
_world(nullptr),
_worldnode(nullptr),
_debugnode(nullptr),
_cloudLayer(nullptr),
_plantLayer(nullptr)
{
    _bounds.size.set(1.0f, 1.0f);
}

/**
 * Destroys this level, releasing all resources.
 */
LevelModel::~LevelModel(void) {
    unload();
    clearRootNode();
}


#pragma mark -
#pragma mark Drawing Methods
/**
 * Sets the drawing scale for this game level
 *
 * The drawing scale is the number of pixels to draw before Box2D unit. Because
 * mass is a function of area in Box2D, we typically want the physics objects
 * to be small.  So we decouple that scale from the physics object.  However,
 * we must track the scale difference to communicate with the scene graph.
 *
 * We allow for the scaling factor to be non-uniform.
 *
 * @param value  the drawing scale for this game level
 */
void LevelModel::setDrawScale(float value) {
    _cscale = value;
}

/**
 * Clears the root scene graph node for this level
 */
void LevelModel::clearRootNode() {
    if (_root == nullptr) {
        return;
    }
    _worldnode->removeFromParent();
    _worldnode->removeAllChildren();
    _worldnode = nullptr;
    
    _debugnode->removeFromParent();
    _debugnode->removeAllChildren();
    _debugnode = nullptr;
    
    _root = nullptr;
}

/**
 * Sets the scene graph node for drawing purposes.
 *
 * The scene graph is completely decoupled from the physics system.  The node
 * does not have to be the same size as the physics body. We only guarantee
 * that the node is positioned correctly according to the drawing scale.
 *
 * @param value  the scene graph node for drawing purposes.
 *
 * @retain  a reference to this scene graph node
 * @release the previous scene graph node used by this object
 */
void LevelModel::setRootNode(const std::shared_ptr<Node>& node) {
    if (_root != nullptr) {
        clearRootNode();
    }
    
    _root = node;
    _scale.set(_root->getContentSize().width/_bounds.size.width,
               _root->getContentSize().height/_bounds.size.height);
    
    // Create, but transfer ownership to root
    _worldnode = Node::alloc();
    _worldnode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    _worldnode->setPosition(Vec2::ZERO);
    
    _debugnode = Node::alloc();
    _debugnode->setScale(_scale); // Debug node draws in PHYSICS coordinates
    _debugnode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    _debugnode->setPosition(Vec2::ZERO);
    
    _root->addChild(_worldnode,0);
    _root->addChild(_debugnode,1);
    
    // Add the individual elements
    std::shared_ptr<PolygonNode> poly;
    std::shared_ptr<WireNode> draw;

   for(auto it = _cloud.begin(); it != _cloud.end(); ++it) {
        std::shared_ptr<Cloud> cloud = *it;
        cloud->setScale(_cscale); 
        auto cloudNode = CloudNode::alloc(_assets->get<Texture>("particle"));
        cloudNode->setName(cloud->getName());
        cloud->setSceneNodeParticles(cloudNode, GRID_HEIGHT + DOWN_LEFT_CORNER_Y, _assets->get<Texture>("cloudFace"), _assets->get<Texture>("shadow"));
        addObstacle(cloud,cloudNode,1);
   }

    auto plantNode = Node::alloc();
    for(auto &plant : _plants) {
        if (plant != nullptr) {
            plant->setAssets(_assets);
            plant->setSceneNode(plantNode, plant->getName());
        }
   }
   _worldnode->addChildWithName(plantNode, "plantNode", 3);

   auto pestNode = Node::alloc();
    for(auto &pest : _pests) {
        if (pest != nullptr) {
            pest->setAssets(_assets);
            pest->setSceneNode(pestNode, pest->getName());
        }
   }
   _worldnode->addChildWithName(pestNode, "pestNode", 0);
}

/**
 * Toggles whether to show the debug layer of this game world.
 *
 * The debug layer displays wireframe outlines of the physics fixtures.
 *
 * @param  flag whether to show the debug layer of this game world
 */
void LevelModel::showDebug(bool flag) {
    if (_debugnode != nullptr) {
        _debugnode->setVisible(flag);
    }
}


#pragma mark -
#pragma mark Asset Loading
/**
 * Loads this game level from the source file
 *
 * This load method should NEVER access the AssetManager.  Assets are loaded in
 * parallel, not in sequence.  If an asset (like a game level) has references to
 * other assets, then these should be connected later, during scene initialization.
 *
 * @return true if successfully loaded the asset from a file
 */
bool LevelModel::preload(const std::string& file) {
    std::shared_ptr<JsonReader> reader = JsonReader::allocWithAsset(file);
    return preload(reader->readJson());
}

/**
 * Loads this game level from the source file
 *
 * This load method should NEVER access the AssetManager.  Assets are loaded in
 * parallel, not in sequence.  If an asset (like a game level) has references to
 * other assets, then these should be connected later, during scene initialization.
 *
 * @return true if successfully loaded the asset from a file
 */
bool LevelModel:: preload(const std::shared_ptr<cugl::JsonValue>& json) {
    if (json == nullptr) {
        CUAssertLog(false, "Failed to load level file");
        return false;
    }
    // Initial geometry
    auto layers = json->get("layers");
    for(int i = 0; i < layers->size(); i++) {
        auto layer = layers->get(i);
        auto name = layer->get("name")->asString();
        if (name == "Clouds") {
            _cloudLayer = layer->get("objects");
        } else if (name == "Plants") {
            _plantLayer = layer->get("objects");
        } else if (name == "Pests") {
            _pestLayer = layer->get("objects");
        }
       CULog(name.c_str());
    }
    
   float w = json->get(WIDTH_FIELD)->asFloat();
   float h = json->get(HEIGHT_FIELD)->asFloat();
   _bounds.size.set(w, h);
    
    /** Create the physics world */
    _world = ObstacleWorld::alloc(getBounds(),getGravity());

    if (_cloudLayer != nullptr) {
        // Convert the object to an array so we can see keys and values
        int csize = (int)_cloudLayer->size();
        for(int ii = 0; ii < csize; ii++) {
            loadCloud(_cloudLayer->get(ii), ii);
        }
    } else {
        CUAssertLog(false, "Failed to load crates");
        return false;
    }

    if (_plantLayer != nullptr) {
        // Convert the object to an array so we can see keys and values
        int csize = (int)_plantLayer->size();
        for(int ii = 0; ii < csize; ii++) {
            loadPlant(_plantLayer->get(ii));
        }
    } else {
        CUAssertLog(false, "Failed to load crates");
        return false;
    }
    
    if (_pestLayer != nullptr) {
        // Convert the object to an array so we can see keys and values
        int csize = (int)_pestLayer->size();
        for(int ii = 0; ii < csize; ii++) {
            loadPest(_pestLayer->get(ii));
        }
    } else {
        CUAssertLog(false, "Failed to load crates");
        return false;
    }
    
    return true;
}

/**
 * Unloads this game level, releasing all sources
 *
 * This load method should NEVER access the AssetManager.  Assets are loaded and
 * unloaded in parallel, not in sequence.  If an asset (like a game level) has
 * references to other assets, then these should be disconnected earlier.
 */
void LevelModel::unload() {
//    if (_rocket != nullptr) {
//        if (_world != nullptr) {
//            _world->removeObstacle(_rocket.get());
//        }
//        _rocket = nullptr;
//    }
//    if (_goalDoor != nullptr) {
//        if (_world != nullptr) {
//            _world->removeObstacle(_goalDoor.get());
//        }
//        _goalDoor = nullptr;
//    }
//    for(auto it = _crates.begin(); it != _crates.end(); ++it) {
//        if (_world != nullptr) {
//            _world->removeObstacle((*it).get());
//        }
//        (*it) = nullptr;
//    }
//    _crates.clear();
//    for(auto it = _walls.begin(); it != _walls.end(); ++it) {
//        if (_world != nullptr) {
//            _world->removeObstacle((*it).get());
//        }
//        (*it) = nullptr;
//    }
//    _walls.clear();
//    if (_world != nullptr) {
//        _world->clear();
//        _world = nullptr;
//    }
}


#pragma mark -
#pragma mark Individual Loaders

bool LevelModel::loadCloud(const std::shared_ptr<JsonValue>& cloudJson, int i) {
  bool success = true;
  std::shared_ptr<JsonValue> cloudLayer;

  // Create the polygon outline
  Poly2 cloudpoly(CLOUD2, 8);
  SimpleTriangulator triangulator;
  triangulator.set(cloudpoly);
  triangulator.calculate();
  cloudpoly.setIndices(triangulator.getTriangulation());
  cloudpoly.setType(Poly2::Type::SOLID);

  success = success && cloudJson->get(X_COORD)->isNumber();
  auto x = cloudJson->getInt(X_COORD);
  success = success && cloudJson->get(Y_COORD)->isNumber();
  auto y = cloudJson->getInt(Y_COORD);

  std::shared_ptr<Cloud> cloud = Cloud::alloc(cloudpoly, Vec2(x, y));
  cloud->setDebugColor(DYNAMIC_COLOR);
  cloud->setName("cloud" + std::to_string(i));
  cloud->setId(i);
  // Why is scale a vec2, not a float lol
  cloud->setScale(_cscale);
  cloud->setTextureKey(cloudJson->getString(TEXTURE_FIELD));

  if (success) {
      _cloud.push_back(cloud);
  } else {
      cloud = nullptr;
  }
  return success;
}

/**
 * Loads the singular exit door
 *
 * The exit door will will be stored in _goalDoor field and retained.
 * If the exit fails to load, then _goalDoor will be nullptr.
 *
 * @param  reader   a JSON reader with cursor ready to read the exit
 *
 * @retain the exit door
 * @return true if the exit door was successfully loaded
 */


bool LevelModel::loadPlant(const std::shared_ptr<JsonValue>& json) {
    bool success = true;

    success = success && json->get(ID)->isNumber();
    auto plantId = json->getInt(ID);

    success = success && json->get(X_COORD)->isNumber();
    auto x = json->getInt(X_COORD);
    success = success && json->get(Y_COORD)->isNumber();
    auto y = json->getInt(Y_COORD);

    success = success && json->get(TYPE)->isString();
    auto plantType = json->getString(TYPE);

    auto plant = Plant::alloc(x, y, rainMap[plantType.c_str()], shadeMap[plantType], 32.0f);
    auto plantName = "plant" + std::to_string(plantId);
    plant->setName(plantName);
    plant->setPlantType(plantType);

    if (success) {
        _plants.push_back(plant);
    } else {
        plant = nullptr;
    }

    return success;
}

/**
 * Loads a single wall object
 *
 * The wall will be retained and stored in the vector _walls.  If the
 * wall fails to load, then it will not be added to _walls.
 *
 * @param  reader   a JSON reader with cursor ready to read the wall
 *
 * @retain the wall
 * @return true if the wall was successfully loaded
 */
bool LevelModel::loadPest(const std::shared_ptr<JsonValue>& json) {
    bool success = true;

    success = success && json->get(ID)->isNumber();
    auto plantId = json->getInt(ID);

    success = success && json->get(X_COORD)->isNumber();
    auto x = json->getInt(X_COORD);
    success = success && json->get(Y_COORD)->isNumber();
    auto y = json->getInt(Y_COORD);

    success = success && json->get(TYPE)->isString();
    auto pestType = json->getString(TYPE);

    success = success && json->get("side")->isString();
    auto side = json->getString("side");

    auto pest = Pest::alloc(x, y, pestType, side, 32.0f);
    auto pestName = pestType + std::to_string(plantId);
    pest->setName(pestName);

    if (success) {
        _pests.push_back(pest);
    } else {
        pest = nullptr;
    }

    return true;
}

/**
 * Loads a single crate object
 *
 * The crate will be retained and stored in the vector _crates.  If the
 * crate fails to load, then it will not be added to _crates.
 *
 * @param  reader   a JSON reader with cursor ready to read the crate
 *
 * @retain the crate
 * @return true if the crate was successfully loaded
 */
bool LevelModel::loadCrate(const std::shared_ptr<JsonValue>& json) {
    return true;
}

/**
 * Converts the string to a color
 *
 * Right now we only support the following colors: yellow, red, blur, green,
 * black, and grey.
 *
 * @param  name the color name
 *
 * @return the color for the string
 */
Color4 LevelModel::parseColor(std::string name) {
    if (name == "yellow") {
        return Color4::YELLOW;
    } else if (name == "red") {
        return Color4::RED;
    } else if (name == "green") {
        return Color4::GREEN;
    } else if (name == "blue") {
        return Color4::BLUE;
    } else if (name == "black") {
        return Color4::BLACK;
    } else if (name == "gray") {
        return Color4::GRAY;
    }
    return Color4::WHITE;
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
 void LevelModel::addObstacle(const std::shared_ptr<cugl::Obstacle>& obj,
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










