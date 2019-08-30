//
//  GameController.h
//  Weather Defender
//
//  This is the most important class in this demo.  This class manages the gameplay
//  for this demo.  It also handles collision detection. There is not much to do for
//  collisions; our WorldController class takes care of all of that for us.  This
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
#ifndef __GAME_CONTROLLER_H__
#define __GAME_CONTROLLER_H__
#include <cugl/cugl.h>
#include <Box2D/Dynamics/b2WorldCallbacks.h>
#include <vector>
#include "InputController.h"
#include "ResourceController.hpp"
#include "particleShader.hpp"
#include "Plant.hpp"
#include "Cloud.hpp"
#include "Board.hpp"
#include "Particle.hpp"
#include "LevelModel.hpp"
#include <set>
#include <cugl/2d/CUPathNode.h>

//                                       Position       Texcoords
static GLfloat masterParticleQuad[16] ={-10.0f,-10.0f,  0.0f, 0.0f,
                                        -10.0f, 10.0f,  0.0f, 1.0f,
                                         10.0f,-10.0f,  1.0f, 0.0f,
                                         10.0f, 10.0f,  1.0f, 1.0f};
static float particleFactor = 0.0f;

/**
 * This class is the primary gameplay constroller for the demo.
 *
 * A world has its own objects, assets, and input controller.  Thus this is
 * really a mini-GameEngine in its own right.  As in 3152, we separate it out
 * so that we can have a separate mode for the loading screen.
 */
class GameScene : public cugl::Scene {
protected:
    /** The asset manager for this game mode. */
    std::shared_ptr<cugl::AssetManager> _assets;
    int _levelId;

    // CONTROLLERS
    /** Controller for abstracting out input across multiple platforms */
    Size CLOUD_DEFAULT_SIZE = Size(5.1, 2.6);
    
    RagdollInput _input;
    std::vector<std::shared_ptr<Plant>> _plants;
    std::shared_ptr<Board> _board;
    
    std::shared_ptr<Node> _endscreen_nostar;
    std::shared_ptr<Node> _endscreen_1star;
    std::shared_ptr<Node> _endscreen_2star;
    std::shared_ptr<Node> _endscreen_3star;

    std::shared_ptr<Label> _st1plantnum;
    std::shared_ptr<Label> _st2plantnum;
    std::shared_ptr<Label> _st3plantnum;
    std::shared_ptr<Label> _st4plantnum;
    std::shared_ptr<Label> _finalScore;
    bool _tutorialshown = false;
    std::shared_ptr<Node> _tutorialpage = nullptr;
    std::shared_ptr<Button> _tcontinuebutton = nullptr;
    
    std::shared_ptr<cugl::Button> _pauseButton;
    std::shared_ptr<cugl::Button> _pmainbutton;
    std::shared_ptr<cugl::Button> _presetbutton;
    std::shared_ptr<cugl::Button> _continuebutton;
    std::shared_ptr<cugl::Button> _vresetbutton;
    std::shared_ptr<cugl::Button> _vmainbutton;
    std::shared_ptr<cugl::Button> _nlbutton;
    std::shared_ptr<Node> _pauseboard;
    bool _paused;
    bool _mainSelected;
    bool _resetSelected;
    bool _continueSelected;
    bool _nextlevelselected;
    
    
    std::shared_ptr<ParticleNode> _rainNode;
    std::shared_ptr<cugl::FreeList<Particle>> _memory;
    std::set<Particle*> _particles;
    std::shared_ptr<LevelModel> _level;
    cugl::Size dimen;
    Vec3 dimenWithIndicator;
    std::vector<Particle*> _pQ;
    std::vector<Particle*> _pD;

    int _max_cloud_id = 0;

    // VIEW
    /** Reference to the physics root of the scene graph */
    std::shared_ptr<cugl::TexturedNode> _worldnode;
    std::shared_ptr<cugl::TexturedNode> _backgroundNode;
    /** Reference to the debug root of the scene graph */
    std::shared_ptr<cugl::Node> _debugnode;
    std::shared_ptr<cugl::Node> _rootnode;
    std::shared_ptr<cugl::Node> _levelworldnode;
    std::shared_ptr<CloudNode> masterCloudNode;
    
    /** The Box2D world */
    std::shared_ptr<cugl::ObstacleWorld> _world;
    /** The scale between the physics world and the screen (MUST BE UNIFORM) */
    float _scale;

    // Physics objects for the game
    std::vector<std::shared_ptr<Cloud>> _clouds;

	/** Selector to allow mouse control of the ragdoll */
    std::map<long, Obstacle *> _selectors;
    std::map<long, Obstacle *> _shadowSelectors;

    int _curr_bkgd;
    /** Whether we have completed this "game" */
    bool _complete;
    /** Whether or not debug mode is active */
    bool _debug;
	/** Counter to timestamp sound generation */
	unsigned long _counter;

#pragma mark Internal Object Management
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
    void populate();

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
    void addObstacle(const std::shared_ptr<cugl::Node> worldNode, const std::shared_ptr<cugl::Obstacle>& obj, const std::shared_ptr<cugl::Node>& node, int zOrder);
    
    void splitClouds();
    void makeRain(Obstacle * cloud);
    void makeLightning(Obstacle * cloud);
    Obstacle * getSelectedObstacle(Vec2 pos, long touchID);
    void processRemoval();
    /**
     * Returns the active screen size of this scene.
     *
     * This method is for graceful handling of different aspect
     * ratios
     */
    Vec3 computeActiveSize() const;

public:
#pragma mark -
#pragma mark Constructors
    /**
     * Creates a new game world with the default values.
     *
     * This constructor does not allocate any objects or start the controller.
     * This allows us to use a controller without a heap pointer.
     */
    GameScene();

    /**
     * Disposes of all (non-static) resources allocated to this mode.
     *
     * This method is different from dispose() in that it ALSO shuts off any
     * static resources, like the input controller.
     */
    ~GameScene() { dispose(); }

    /**
     * Disposes of all (non-static) resources allocated to this mode.
     */
    void dispose();
    

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
    bool init(const std::shared_ptr<cugl::AssetManager>& assets, bool reset);

    bool init(const std::shared_ptr<AssetManager>& assets, int level, bool reset);
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
    bool init(const std::shared_ptr<cugl::AssetManager>& assets, const cugl::Rect& rect, bool reset);

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
    bool init(const std::shared_ptr<cugl::AssetManager>& assets, const cugl::Rect& rect, const cugl::Vec2& gravity, int level, bool reset);


#pragma mark -
#pragma mark State Access
    /**
     * Returns true if the gameplay controller is currently active
     *
     * @return true if the gameplay controller is currently active
     */
    bool isActive( ) const { return _active; }

    /**
     * Returns true if debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @return true if debug mode is active.
     */
    bool isDebug( ) const { return _debug; }

    /**
     * Sets whether debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @param value whether debug mode is active.
     */
    void setDebug(bool value) { _debug = value; _debugnode->setVisible(value); }

    /**
     * Returns true if the level is completed.
     *
     * If true, the level will advance after a countdown
     *
     * @return true if the level is completed.
     */
    bool isComplete( ) const { return _complete; }

    /**
     * Sets whether the level is completed.
     *
     * If true, the level will advance after a countdown
     *
     * @param value whether the level is completed.
     */
    void setComplete(bool value) { _complete = value; }

    /**
     * Processes the start of a collision
     *
     * This method is called when we first get a collision between two objects.
     * We use this method to test if it is the "right" kind of collision.  In
     * particular, we use it to test if we make it to the win door.
     *
     * @param  contact  The two bodies that collided
     */
    void beginContact(b2Contact* contact);
    void endContact(b2Contact* contact);
    void combineByPinch(Cloud * cind1, Cloud * cind2);
    void checkForCombining(Obstacle * ob);
    void checkForRain(Obstacle * ob, long touchID);
    void checkForLightning(Obstacle * ob, long touchID);
    bool gamePaused() { return _paused; }
//    bool backToLevelSelect() {return _backToLevelSelect;}
//    void setBackToLevelSelect(bool b) {_backToLevelSelect = b;}

    /**
     * Handles any modifications necessary before collision resolution
     *
     * This method is called just before Box2D resolves a collision.  We use
     * this method to implement sound on contact, using the algorithms outlined
     * in Ian Parberry's "Introduction to Game Physics with Box2D".
     *
     * @param  contact  The two bodies that collided
     * @param  contact  The collision manifold before contact
     */
    void beforeSolve(b2Contact* contact, const b2Manifold* oldManifold);    
    void createResourceClouds();
    void updateBackground();

#pragma mark -
#pragma mark Gameplay Handling
    /**
     * The method called to update the game mode.
     *
     * This method contains any gameplay code that is not an OpenGL call.
     *
     * @param timestep  The amount of time (in seconds) since the last frame
     */
    void update(float timestep);

    /**
     * Resets the status of the game so that we can play again.
     */
    void reset();
    bool paused(){return _paused;}
    bool mainSelected() {return _mainSelected;}
    bool resetSelected(){return _resetSelected;}
    bool nextLevelSelected() {return _nextlevelselected;}
    bool continueSelected() {return _continueSelected;}
    
    void resetPause(){_paused = false;}
    
    void resetPauseBool(){
        _mainSelected =false;
        _resetSelected=false;
        _continueSelected=false;
        _paused = false;
        resetOver();
        _nextlevelselected = false;
        _tutorialshown = true;
    }
    
    void resetOver(){
        if (_level != nullptr) _level->resetOver();
    }
    
    void removePauseDisplay() {
        if (_pmainbutton != nullptr){
            _pmainbutton->deactivate();
            _pmainbutton->setVisible(false);
        }
        if (_continuebutton != nullptr){
            _continuebutton->deactivate();
            _continuebutton->setVisible(false);
        }
        if (_presetbutton != nullptr){
            _presetbutton->deactivate();
            _presetbutton->setVisible(false);
        }
        if (_pauseboard) _pauseboard->setVisible(false);
    }
    
    void removeTutorialDisplay() {
        if (_tcontinuebutton != nullptr){
            _tcontinuebutton->deactivate();
            _tcontinuebutton->setVisible(false);
        }
        if (_tutorialpage) _tutorialpage->setVisible(false);
    }
    
    bool tutorialDisplay();
    
    void removeVictoryDisplay(){
        if (_vmainbutton != nullptr){
            _vmainbutton->deactivate();
            _vmainbutton->setVisible(false);
        }
        if (_vresetbutton != nullptr){
            _vresetbutton->deactivate();
            _vresetbutton->setVisible(false);
        }
        if (_nlbutton != nullptr){
            _nlbutton->deactivate();
            _nlbutton->setVisible(false);
        }
        if (_endscreen_nostar) _endscreen_nostar->setVisible(false);
        if (_endscreen_1star) _endscreen_1star->setVisible(false);
        if (_endscreen_2star) _endscreen_2star->setVisible(false);
        if (_endscreen_3star) _endscreen_3star->setVisible(false);
    }
    
//    void setShowTutorial(){
//        _showtutorial = true;
//    }
    
    void displayPause();
    void displayVictory();
    
    int getLevelId(){
        return _levelId;
    }

};

#endif /* __GAME_CONTROLLER_H__ */