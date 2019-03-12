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
#define noNeed 0
#define needRain 1
#define needSun 2
#define needShade 3
#define dead 4

using namespace cugl;

class Plant : public cugl::BoxObstacle {
public:
    bool isShaded;
    int healthLimit = 2000;
private:
    /** This macro disables the copy constructor (not allowed on scene graphs) */
    CU_DISALLOW_COPY_AND_ASSIGN(Plant);
    
protected:
    int _health;
    float _drawscale;
    //std::shared_ptr<cugl::Texture> _texture;
    int _x;
    int _y;
    std::shared_ptr<cugl::Texture> _texture;
    int _state;
    int _type;
    
    
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
    bool init(int x, int y, std::shared_ptr<cugl::Texture> texture, float drawscale);
    
    
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
    static std::shared_ptr<Plant> alloc(int x, int y, std::shared_ptr<cugl::Texture> texture, float drawscale) {
        std::shared_ptr<Plant> result = std::make_shared<Plant>();
        return (result->init(x, y, texture, drawscale) ? result : nullptr);
    }
    
    void setSceneNode(const std::shared_ptr<cugl::Node>& node);

    int getType() {return _type;}
    void setType(int t) {_type = t;}
    
    Vec2 getPosition() {return Vec2(_x,_y);}
    void setPosition(Vec2 z) {_x = z.x; _y = z.y;}
    
    int getHealth() {return _health;}
    void setHealth(int h) {_health = h;}
    //void setTexture(std::shared_ptr<cugl::Texture> texture) {_texture = texture;}
    void decHealth() {
        if (_health > -healthLimit){_health -= 1;}
    }
    void incHealth() {
        if (_health < healthLimit){_health += 2;}
    }
    void updateState();
    void setState(int s);
    int getState() {return _state;}
};


#endif /* Plant_hpp */
