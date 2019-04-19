//
//  LevelModel.hpp
//  WeatherDefender
//
//  Created by Zaibo  Wang on 3/26/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef __JS_LEVEL_MODEL_H__
#define __JS_LEVEL_MODEL_H__
#include <cugl/cugl.h>
#include <cugl/2d/physics/CUObstacleWorld.h>
#include <vector>
#include <cugl/assets/CUAsset.h>
#include <cugl/io/CUJsonReader.h>
#include "Cloud.hpp"
#include "Plant.hpp"
#include "Board.hpp"
#include <map>
#include "CloudNode.hpp"
#include "Pest.hpp"

using namespace cugl;

/** Forward references to the various classes used by this level */
class ExitModel;
class WallModel;
class CrateModel;


#pragma mark -
#pragma mark Level Model
/**
 * Class that represents a dynamically loaded level in the game
 *
 * This class is a subclass of Asset so that we can use it with a GenericLoader. As with
 * all assets, this class SHOULD NOT make any references to AssetManager in the load/unload
 * methods. Assets should be treated as if they load in parallel, not in sequence.  Therefore,
 * it is unsafe to assume that one asset loads before another.  If this level needs to connect
 * to other assets (sound, images, etc.) this should take place after asset loading, such as
 * during scene graph initialization.
 */
class LevelModel : public Asset {
protected:
    /** The root node of this level */
    std::shared_ptr<Node> _root;
    /** The bounds of this level in physics coordinates */
    Rect _bounds;
    /** The global gravity for this level */
    Vec2 _gravity;
    /** The level drawing scale (difference between physics and drawing coordinates) */
    Vec2 _scale;
    
    float _cscale;
    
    /** Reference to the physics root of the scene graph */
    std::shared_ptr<Node> _worldnode;
    /** Reference to the debug root of the scene graph */
    std::shared_ptr<Node> _debugnode;
    
    // Physics objects for the game
    /** The physics word; part of the model (though listeners elsewhere) */
    /** Reference to the rocket/player avatar */
//    std::shared_ptr<RocketModel> _rocket;
    /** Reference to the goalDoor (for collision detection) */
//    std::shared_ptr<ExitModel> _goalDoor;
    std::shared_ptr<cugl::JsonValue> _cloudLayer;   
    std::shared_ptr<cugl::JsonValue> _plantLayer;   
    std::shared_ptr<cugl::JsonValue> _pestLayer;   
    std::vector<std::shared_ptr<Cloud>> _cloud;
//    std::map<int, std::shared_ptr<CloudNode>> _cloudNodes;
    std::vector<std::shared_ptr<Plant>> _plants;
    std::vector<std::shared_ptr<Pest>> _pests;
    std::shared_ptr<Board> _board;

    int _time;

    Poly2 _poly;

    std::shared_ptr<cugl::ProgressBar>  _bar;
    

    // float _scale;


    /** Reference to all the active crates */
//    std::vector<std::shared_ptr<CrateModel>> _crates;
    /** Reference to all the walls */
//    std::vector<std::shared_ptr<WallModel>> _walls;
    
    /** The AssetManager for the game mode */
    std::shared_ptr<cugl::AssetManager> _assets;
    
#pragma mark Internal Helper Methods
    /**
     * Loads the singular rocket object
     *
     * The rocket will will be stored in _rocket field and retained.
     * If the rocket fails to load, then _rocket will be nullptr.
     *
     * @param  reader   a JSON reader with cursor ready to read the rocket
     *
     * @retain the rocket
     * @return true if the rocket was successfully loaded
     */
    // bool loadRocket(const std::shared_ptr<JsonValue>& json);

    // Load all clouds
    bool loadCloud(const std::shared_ptr<JsonValue>& json, int i);
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
    bool loadPlant(const std::shared_ptr<JsonValue>& json);
    
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
    bool loadPest(const std::shared_ptr<JsonValue>& json);
    
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
    bool loadCrate(const std::shared_ptr<JsonValue>& json);
    
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
    Color4 parseColor(std::string name);
    
    /**
     * Clears the root scene graph node for this level
     */
    void clearRootNode();
    
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
     * have different z-orders whenever possible.  This will cut down on the
     * amount of drawing done
     *
     * param obj    The physics object to add
     * param node   The scene graph node to attach it to
     * param zOrder The drawing order
     */
    void addObstacle(const std::shared_ptr<cugl::Obstacle>& obj, const std::shared_ptr<cugl::Node>& node, int zOrder);
    
public:
#pragma mark Static Constructors
    /**
     * Creates a new game level with no source file.
     *
     * The source file can be set at any time via the setFile() method. This method
     * does NOT load the asset.  You must call the load() method to do that.
     *
     * @return  an autoreleased level file
     */
    static std::shared_ptr<LevelModel> alloc() {
        std::shared_ptr<LevelModel> result = std::make_shared<LevelModel>();
        return (result->init("") ? result : nullptr);
    }
    
    /**
     * Creates a new game level with the given source file.
     *
     * This method does NOT load the level. You must call the load() method to do that.
     * This method returns false if file does not exist.
     *
     * @return  an autoreleased level file
     */
    static std::shared_ptr<LevelModel> alloc(std::string file) {
        std::shared_ptr<LevelModel> result = std::make_shared<LevelModel>();
        return (result->init(file) ? result : nullptr);
    }
    
    void setDebugNode(std::shared_ptr<cugl::Node> node) {_debugnode = node;}
    
    std::vector<std::shared_ptr<Cloud>> getClouds() { return _cloud; };
    
    std::shared_ptr<Node> getWorldNode() { return _worldnode; };
    std::shared_ptr<Node> getDebugNode() { return _debugnode; };

    
    std::vector<std::shared_ptr<Plant>> getPlants() { return _plants; };
    std::vector<std::shared_ptr<Pest>> getPests() { return _pests; };
//    void removeCloudNode(int id);
    
    
#pragma mark Model Access
    /**
     * Returns the rocket in this game level
     *
     * @return the rocket in this game level
     */
//    const std::shared_ptr<RocketModel>& getRocket() { return _rocket; }
    
    /**
     * Returns the exit door in this game level
     *
     * @return the exit door in this game level
     */
//    const std::shared_ptr<ExitModel>& getExit() { return _goalDoor; }
    
    
    
#pragma mark Physics Attributes
    /**
     * Returns the bounds of this level in physics coordinates
     *
     * @return the bounds of this level in physics coordinates
     */
    const Rect& getBounds() const   { return _bounds; }
    
    /**
     * Returns the global gravity for this level
     *
     * @return the global gravity for this level
     */
    const Vec2& getGravity() const { return _gravity; }
    
    
#pragma mark Drawing Methods
    /**
     * Returns the drawing scale for this game level
     *
     * The drawing scale is the number of pixels to draw before Box2D unit. Because
     * mass is a function of area in Box2D, we typically want the physics objects
     * to be small.  So we decouple that scale from the physics object.  However,
     * we must track the scale difference to communicate with the scene graph.
     *
     * We allow for the scaling factor to be non-uniform.
     *
     * @return the drawing scale for this game level
     */
    const Vec2& getDrawScale() const { return _scale; }
    std::shared_ptr<Cloud> createNewCloud(int id, Vec2 pos, float size);
    const float getCloudDrawScale() const {return _cscale;}
    
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
    void setDrawScale(float value);
//    void addObstacle(const std::shared_ptr<cugl::Obstacle>& obj, const std::shared_ptr<cugl::Node>& node, int zOrder);
    
    /**
     * Returns the scene graph node for drawing purposes.
     *
     * The scene graph is completely decoupled from the physics system.  The node
     * does not have to be the same size as the physics body. We only guarantee
     * that the node is positioned correctly according to the drawing scale.
     *
     * @return the scene graph node for drawing purposes.
     */
    const std::shared_ptr<Node>& getRootNode() const { return _root; }
    
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
    void setRootNode(const std::shared_ptr<Node>& root);
    
    /**
     * Sets the loaded assets for this game level
     *
     * @param assets the loaded assets for this game level
     */
    void setAssets(const std::shared_ptr<AssetManager>& assets) { _assets = assets;  }
    
    /**
     * Toggles whether to show the debug layer of this game world.
     *
     * The debug layer displays wireframe outlines of the physics fixtures.
     *
     * @param  flag whether to show the debug layer of this game world
     */
    void showDebug(bool flag);
    
    
#pragma mark -
#pragma mark Asset Loading
    /**
     * Loads this game level from the source file
     *
     * This load method should NEVER access the AssetManager.  Assets are loaded in
     * parallel, not in sequence.  If an asset (like a game level) has references to
     * other assets, then these should be connected later, during scene initialization.
     *
     * @param file the name of the source file to load from
     *
     * @return true if successfully loaded the asset from a file
     */
    virtual bool preload(const std::string& file) override;
    
    
    /**
     * Loads this game level from a JsonValue containing all data from a source Json file.
     *
     * This load method should NEVER access the AssetManager.  Assets are loaded in
     * parallel, not in sequence.  If an asset (like a game level) has references to
     * other assets, then these should be connected later, during scene initialization.
     *
     * @param json the json loaded from the source file to use when loading this game level
     *
     * @return true if successfully loaded the asset from the input JsonValue
     */
    virtual bool preload(const std::shared_ptr<cugl::JsonValue>& json) override;
    
    /**
     * Unloads this game level, releasing all sources
     *
     * This load method should NEVER access the AssetManager.  Assets are loaded and
     * unloaded in parallel, not in sequence.  If an asset (like a game level) has
     * references to other assets, then these should be disconnected earlier.
     */
    void unload();
    
    
    //#pragma mark -
    //#pragma mark Initializers
    /**
     * Creates a new, empty level.
     */
    LevelModel(void);
    
    /**
     * Destroys this level, releasing all resources.
     */
    virtual ~LevelModel(void);

    /**
     * Update nodes of the level model including clouds, plants and the timer
    */
    void update(int ticks);
};

#endif /* defined(__JS_LEVEL_MODEL_H__) */
