//
//  Plant.hpp
//  Weather Defender (Mac)
//
//  Created by 김지원 on 2/23/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef Plant_hpp
#define Plant_hpp

#include <stdio.h>
#include <cugl/cugl.h>
#include <vector>
#include "Board.hpp"


using namespace cugl;

class Plant : public cugl::BoxObstacle {
public:
    int healthLimit = 3;
private:
    /** This macro disables the copy constructor (not allowed on scene graphs) */
    CU_DISALLOW_COPY_AND_ASSIGN(Plant);

protected:
    int _health;
    float _drawscale;
    bool _shaded;
    bool _rained;
    bool _attacked;
    int _x;
    int _y;
    std::vector<std::shared_ptr<Texture>> _textures;
//    std::shared_ptr<cugl::Texture> _currTexture;
    int _stage;
    int _maxStage;
    int _defaultHealth;

    int _shadeNeeded;
    int _rainNeeded;

    int _state;
    int _type;
    int _rainProb;
    int _shadeProb;
    int _progress;
    bool _active;
    std::string _ptype;
    

    int _shadeCounter;
    int _rainCounter;
    std::shared_ptr<AnimationNode> _node;
    std::shared_ptr<TexturedNode> _signNode;
    std::shared_ptr<cugl::AssetManager> _assets;

    std::shared_ptr<ActionManager> _actions;
    std::shared_ptr<Animate> _grow;

public:
#pragma mark -
#pragma mark Constructors
    /**
     * Creates a new Ragdoll at the origin.
     *
     * NEVER USE A CONSTRUCTOR WITH NEW. If you want to allocate a model on
     * the heap, use one of the static constructors instead.
     */
    Plant(void) : BoxObstacle() { }

    /**
     * Destroys this Ragdoll, releasing all resources.
     */
    virtual ~Plant(void) { dispose(); }

    /**
     * Disposes all resources and assets of this Ragdoll
     *
     * Any assets owned by this object will be immediately released.  Once
     * disposed, a Ragdoll may not be used until it is initialized again.
     */
    void dispose();

    /**
     * Initializes a new Ragdoll with the given position and scale
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
    bool init(int x, int y, int rain, int shade, float drawscale);


#pragma mark -
#pragma mark Static Constructors
    /**
     * Returns a newly allocated Ragdoll with the given position
     *
     * The Ragdoll is scaled so that 1 pixel = 1 Box2d unit
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @param pos   Initial position in world coordinates
     *
     * @return a newly allocated Ragdoll with the given position
     */
    static std::shared_ptr<Plant> alloc(int x, int y, int rainProb, int shadeProb, float drawscale) {
        std::shared_ptr<Plant> result = std::make_shared<Plant>();
        return (result->init(x, y, rainProb, shadeProb, drawscale) ? result : nullptr);
    }

    void setSceneNode(const std::shared_ptr<cugl::Node>& node, std::string name, float ds);

    int getType() {return _type;}
    void setType(int t) {_type = t;}

    Vec2 getPosition() {return Vec2(_x,_y);}
    void setPosition(Vec2 z) {_x = z.x; _y = z.y;}

    int getHealth() {return _health;}
    void setHealth(int h) {_health = h;}
    //void setTexture(std::shared_ptr<cugl::Texture> texture) {_texture = texture;}
    void decHealth() {
        _health -= 1;
    }
    void incHealth() {
        _health += 3;
    }

    void setAssets(std::shared_ptr<cugl::AssetManager> a) { _assets = a; };

    void setShade(bool f);
    void setRained(bool f);
    void setAttacked(bool f) {_attacked = f;};

    int getX() {return _x;};
    int getY() {return _y;};
    int getStage() {return _stage;};
    void setStage(int s){_stage = s;}
    int getMaxStage() {return _maxStage;}

    void setPlantType(std::string s) {
        if (s == "shadeOnly"){
            _ptype = "tomato";
        }
        else if (s == "rainOnly"){
            _ptype = "eggplant";
        }
        else {
            _ptype = s;
        }
    }
    std::string getPlantType() {return _ptype;}

    void updateState(int ticks);
    void setState(int s);
    int getState() {return _state;}

    void upgradeSprite();
    void update(float dt);

    void changeSign() ;
    
//    Vec2 gridCoordToPosition(cugl::Vec2 p){
//        return gridCoordToPosition(p.x, p.y);
//    }
//    
//    Vec2 gridCoordToPosition(float x, float y){
//        Vec2 a = 32*Vec2((DOWN_LEFT_CORNER_X + GRID_WIDTH*x + GRID_WIDTH/2 + GRID_OFFSET_X * x),
//                            (DOWN_LEFT_CORNER_Y + GRID_HEIGHT*y - GRID_HEIGHT/2 + GRID_OFFSET_Y * y));
//        std::cout << x<< endl;
//        cout << y << endl;
//        std::cout << a.x << endl;
//        std::cout << a.y << endl;
//        
//        return a;
//    }
    
};


#endif /* Plant_hpp */
