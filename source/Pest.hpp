//
//  Pest.hpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/11/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef Pest_hpp
#define Pest_hpp

#include <stdio.h>
#include <cugl/cugl.h>
#include <vector>

#define ATTACKING 0
#define EATING 1
#define RUNNING 2
#define INACTIVE 3

using namespace cugl;

class Pest {
public:

private:
    /** This macro disables the copy constructor (not allowed on scene graphs) */
    CU_DISALLOW_COPY_AND_ASSIGN(Pest);
    
protected:
    int _health;
    Vec2 _pos;
    Vec2 _target;
    int _status;
    std::string _type;
    float _speed;
    int _damage;
    std::shared_ptr<cugl::Texture> texture;
    std::shared_ptr<ActionManager> _actions;
    std::shared_ptr<Animate> _move;
    std::shared_ptr<Animate> _eat;
    std::shared_ptr<cugl::AssetManager> _assets;
    std::shared_ptr<Node> _node;
    std::shared_ptr<Node> _node_rev;
    std::shared_ptr<Node> _eat_node;
    std::string _side;
    std::string _name;
    int _xside;
    float _drawscale;
    bool _scared;
    float _scaledTargetX;
    bool _active;
    bool _attacked;
    bool _ate;
    int _state;
    
public:
#pragma mark -
#pragma mark Constructors
    /**
     * Creates a new Pest.
     *
     * NEVER USE A CONSTRUCTOR WITH NEW. If you want to allocate a model on
     * the heap, use one of the static constructors instead.
     */
    Pest(void) { }
    
    /**
     * Destroys this Pest, releasing all resources.
     */
    virtual ~Pest(void) { dispose(); }
    
    /**
     * Disposes all resources and assets of this Pest
     *
     * Any assets owned by this object will be immediately released.  Once
     * disposed, a Pest may not be used until it is initialized again.
     */
    void dispose();
    
    /**
     * Initializes a new Pest with the given position and scale
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @param pos   Initial position in world coordinates
     * @param scale The drawing scale to convert world to screen coordinates
     *
     * @return  true if the obstacle is initialized properly, false otherwise.
     */
    bool init(int x, int y, std::string type, std::string side, float drawscale);
    
    
#pragma mark -
#pragma mark Static Constructors
    /**
     * Returns a newly allocated Pest with the given position
     *
     * The Pest is scaled so that 1 pixel = 1 Box2d unit
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @param pos   Initial position in world coordinates
     *
     * @return a newly allocated Pest with the given position
     */
    static std::shared_ptr<Pest> alloc(int x, int y, std::string texture, std::string side, float drawscale) {
        std::shared_ptr<Pest> result = std::make_shared<Pest>();
        return (result->init(x, y, texture, side, drawscale) ? result : nullptr);
    }
    
    void setSceneNode(const std::shared_ptr<cugl::Node>& node, std::string id, float ds);
    
    std::string getType() {return _type;}
    void setType(int t) {_type = t;}
    
    int getHealth() {return _health;}
    void setHealth(int h) {_health = h;}

    void setStatus(int s);
    int getStatus() {return _status;}
    
    Vec2 getPosition() {return _pos;}
    void setPosition(Vec2 p) {_pos = p;}
    
    Vec2 getTarget() {return _target;}
    void setTarget(Vec2 t) {_target = t;}
    
    float getSpeed() {return _speed;}
    void setSpeed(float s) {_speed = s;}
    
    int getDamage() {return _damage;}
    void setDamage(int h) {_damage = h;}

    std::string getName() {return _name;}
    void setName(std::string name) {_name = name;}

    void update(float dt);
    void walk();

    void setScared(bool b);
    
    bool checkTarget(shared_ptr<Node> worldNode, shared_ptr<Node> gridNode);

    void setAssets(std::shared_ptr<cugl::AssetManager> a) { _assets = a; };
};
#endif /* Pest_hpp */
